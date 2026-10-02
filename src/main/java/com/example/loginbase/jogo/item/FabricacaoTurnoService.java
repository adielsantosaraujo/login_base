package com.example.loginbase.jogo.item;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

import lombok.RequiredArgsConstructor;

/** Etapa 6 do turno: avança as filas de fabricação da vila e resolve conclusões e aprimoramentos. */
@Service
@RequiredArgsConstructor
public class FabricacaoTurnoService {

	private final FabricacaoRepository fabricacaoRepository;
	private final ItemRepository itemRepository;
	private final CidadaoRepository cidadaoRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final ConsultaTrabalhadores consultaTrabalhadores;
	private final ItemBonusGerador gerador;
	private final PeArtesao peArtesao;
	private final RegistroEventoTurnoService eventos;

	@Transactional
	public void processar(Vila vila, int turno) {
		for (Fabricacao f : fabricacaoRepository.findByVilaId(vila.getId())) {
			processar(vila, turno, f);
		}
	}

	private void processar(Vila vila, int turno, Fabricacao f) {
		Construcao oficina = construcaoRepository.findById(f.getConstrucaoId()).orElse(null);
		Cidadao artesao = cidadaoRepository.findById(f.getArtesaoId()).orElse(null);
		boolean apto = oficina != null && oficina.getEstado() == EstadoConstrucao.ATIVA && artesao != null
				&& artesao.isVivo() && oficina.getId().equals(artesao.getConstrucaoId());
		if (!apto) {
			if (f.getEstado() == EstadoFabricacao.EM_ANDAMENTO) {
				f.setEstado(EstadoFabricacao.PAUSADA);
				fabricacaoRepository.save(f);
				eventos.registrar(vila, turno, TipoEventoTurno.FABRICACAO_PAUSADA,
						"Fabricação de " + f.getSubtipo() + " pausada: artesão indisponível ou oficina inativa.",
						Map.of("fabricacaoId", f.getId(), "subtipo", f.getSubtipo().name()));
			}
			return;
		}
		f.setEstado(EstadoFabricacao.EM_ANDAMENTO);
		double ef = consultaTrabalhadores.trabalhadores(oficina, vila).stream()
				.filter(t -> t.cidadao().getId().equals(artesao.getId())).mapToDouble(t -> t.eficiencia()).sum();
		if (ef > 0) {
			f.setPfAtual(f.getPfAtual().add(BigDecimal.valueOf(ef)));
		}
		if (f.getPfAtual().compareTo(BigDecimal.valueOf(f.getPfTotal())) < 0) {
			fabricacaoRepository.save(f);
			return;
		}
		if (f.getItemId() == null) {
			concluirNovo(vila, turno, f, oficina, artesao);
		} else {
			concluirAprimoramento(vila, turno, f);
		}
	}

	private void concluirNovo(Vila vila, int turno, Fabricacao f, Construcao oficina, Cidadao artesao) {
		Profissao prof = ConstrucaoCatalogo.profissoes(oficina.getTipo()).get(0);
		long semente = f.getId() * 1_000_003L + turno;
		Qualidade q = gerador.gerarQualidade(peArtesao.peEfetivo(artesao, prof), f.getNivel(), oficina.getNivel(),
				semente);
		var bonus = gerador.gerarBonusIntrinsecos(q, f.getSubtipo().getCategoria(), f.getNivel(), semente * 31 + 7);
		Item item = new Item(vila.getId(), f.getSubtipo(), q, f.getNivel(), bonus);
		item.setAtributoEscolhido(f.getAtributoEscolhido());
		item = itemRepository.save(item);
		fabricacaoRepository.delete(f);
		eventos.registrar(vila, turno, TipoEventoTurno.FABRICACAO_CONCLUIDA,
				"Item fabricado: " + f.getSubtipo() + " nível " + f.getNivel() + " (" + q + ").",
				Map.of("itemId", item.getId(), "subtipo", f.getSubtipo().name(), "nivel", f.getNivel(),
						"qualidade", q.name()));
	}

	private void concluirAprimoramento(Vila vila, int turno, Fabricacao f) {
		Item item = itemRepository.findById(f.getItemId()).orElse(null);
		if (item != null) {
			item.setNivel(f.getNivel());
			item.setBonus(new java.util.ArrayList<>(gerador.recalcularMagnitudes(item.getBonus(), f.getNivel())));
			item.setEmAprimoramento(false);
			itemRepository.save(item);
		}
		fabricacaoRepository.delete(f);
		eventos.registrar(vila, turno, TipoEventoTurno.APRIMORAMENTO_CONCLUIDO,
				"Item aprimorado: " + f.getSubtipo() + " agora nível " + f.getNivel() + ".",
				Map.of("itemId", f.getItemId(), "subtipo", f.getSubtipo().name(), "nivel", f.getNivel()));
	}
}
