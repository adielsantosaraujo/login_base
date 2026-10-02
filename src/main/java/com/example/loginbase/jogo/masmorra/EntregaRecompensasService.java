package com.example.loginbase.jogo.masmorra;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.ProgressaoGuerreiroService;
import com.example.loginbase.jogo.item.BonusItem;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.masmorra.Recompensas.ItemSorteado;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.pedra.GeradorPedras.PedraSorteada;
import com.example.loginbase.jogo.pedra.Pedra;
import com.example.loginbase.jogo.pedra.PedraRepository;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/** Gera e entrega (na vitória, no momento da batalha) as recompensas da masmorra; devolve o JSON da batalha. */
@Service
public class EntregaRecompensasService {

	private final GeradorRecompensas gerador;
	private final EstoqueService estoqueService;
	private final ItemRepository itemRepository;
	private final PedraRepository pedraRepository;
	private final CidadaoRepository cidadaoRepository;
	private final ProgressaoGuerreiroService progressao;
	private final RegistroEventoTurnoService registro;

	public EntregaRecompensasService(GeradorRecompensas gerador, EstoqueService estoqueService,
			ItemRepository itemRepository, PedraRepository pedraRepository, CidadaoRepository cidadaoRepository,
			ProgressaoGuerreiroService progressao, RegistroEventoTurnoService registro) {
		this.gerador = gerador;
		this.estoqueService = estoqueService;
		this.itemRepository = itemRepository;
		this.pedraRepository = pedraRepository;
		this.cidadaoRepository = cidadaoRepository;
		this.progressao = progressao;
		this.registro = registro;
	}

	/**
	 * @param guerreirosIds combatentes elegíveis a XP (vivos após as consequências; feridos incluídos)
	 * @return mapa JSON gravado em {@code Batalha.recompensas}
	 */
	@Transactional
	public Map<String, Object> entregar(Vila vila, int turno, int nivel, long semente, List<Long> guerreirosIds) {
		Recompensas r = gerador.gerar(nivel, semente);

		estoqueService.creditar(vila, Recurso.OURO, BigDecimal.valueOf(r.ouro()));
		Map<String, Object> recursos = new LinkedHashMap<>();
		r.recursos().forEach((rec, qtd) -> {
			estoqueService.creditar(vila, rec, BigDecimal.valueOf(qtd));
			recursos.put(rec.name(), qtd);
		});

		Map<String, Object> item = null;
		if (r.item() != null) {
			ItemSorteado s = r.item();
			Item novo = new Item(vila.getId(), s.subtipo(), s.qualidade(), s.nivel(), s.bonus());
			novo.setAtributoEscolhido(s.atributoEscolhido());
			novo = itemRepository.save(novo);
			item = new LinkedHashMap<>();
			item.put("itemId", novo.getId());
			item.put("subtipo", s.subtipo().name());
			item.put("categoria", s.subtipo().getCategoria().name());
			item.put("nivel", s.nivel());
			item.put("qualidade", s.qualidade().name());
			item.put("bonus", s.bonus().stream().map(b -> mapaBonus(b)).toList());
			item.put("atributoEscolhido", s.atributoEscolhido() == null ? null : s.atributoEscolhido().name());
		}

		List<Map<String, Object>> pedras = new ArrayList<>();
		for (PedraSorteada ps : r.pedras()) {
			Pedra p = pedraRepository.save(new Pedra(vila.getId(), ps.tipo(), ps.bonus()));
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("pedraId", p.getId());
			m.put("qualidade", ps.tipo().name());
			m.put("bonus", ps.bonus().stream().map(b -> {
				Map<String, Object> bm = new LinkedHashMap<>();
				bm.put("codigo", b.codigo().name());
				bm.put("magnitude", b.magnitude().name());
				bm.put("valor", b.valor());
				return bm;
			}).toList());
			pedras.add(m);
		}

		List<Long> comXp = new ArrayList<>();
		for (Long id : guerreirosIds) {
			Cidadao c = cidadaoRepository.findById(id).orElse(null);
			if (c == null || !c.isVivo()) {
				continue;
			}
			progressao.adicionarXp(c, BigDecimal.valueOf(r.xpPorGuerreiro()));
			cidadaoRepository.save(c);
			comXp.add(id);
		}

		Map<String, Object> json = new LinkedHashMap<>();
		json.put("ouro", r.ouro());
		json.put("recursos", recursos);
		json.put("item", item);
		json.put("xpPorGuerreiro", r.xpPorGuerreiro());
		json.put("guerreirosXp", comXp);
		json.put("pedras", pedras);

		Map<String, Object> dados = new LinkedHashMap<>(json);
		registro.registrar(vila, turno, TipoEventoTurno.RECOMPENSA_MASMORRA,
				"Recompensas da masmorra: " + r.ouro() + " de ouro, " + recursos.size() + " tipo(s) de recurso"
						+ (item != null ? ", 1 item" : "") + ", " + pedras.size() + " pedra(s), " + r.xpPorGuerreiro()
						+ " XP por guerreiro.",
				dados);
		return json;
	}

	private static Map<String, Object> mapaBonus(BonusItem b) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("codigo", b.codigo().name());
		m.put("valor", b.valor());
		return m;
	}
}
