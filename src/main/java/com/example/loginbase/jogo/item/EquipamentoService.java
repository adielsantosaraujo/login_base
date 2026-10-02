package com.example.loginbase.jogo.item;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.item.catalogo.ArmaduraCatalogo;
import com.example.loginbase.jogo.item.catalogo.FerramentaCatalogo;
import com.example.loginbase.jogo.modelo.Vila;

/**
 * Regras de equipar/desequipar itens no cidadão.
 * <p>
 * Decisão: um item já equipado no próprio cidadão pode ser movido para outro slot válido (o slot de
 * origem fica livre); item equipado por outro cidadão é rejeitado (precisa voltar ao inventário antes).
 */
@Service
public class EquipamentoService {

	static final int IDADE_MIN_FERRAMENTA_JOIA = 14;
	static final int IDADE_MIN_COMBATE = 16;

	private final CidadaoRepository cidadaoRepository;
	private final CidadaoProfissaoRepository profissaoRepository;
	private final ItemRepository itemRepository;
	private final PeArtesao peArtesao;
	private final ConsultaTropas consultaTropas;

	public EquipamentoService(CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository profissaoRepository,
			ItemRepository itemRepository, PeArtesao peArtesao, ConsultaTropas consultaTropas) {
		this.cidadaoRepository = cidadaoRepository;
		this.profissaoRepository = profissaoRepository;
		this.itemRepository = itemRepository;
		this.peArtesao = peArtesao;
		this.consultaTropas = consultaTropas;
	}

	@Transactional
	public Map<String, ItemDTO> equipar(Vila vila, Long cidadaoId, String slotTxt, Long itemId) {
		Cidadao c = cidadaoDaVila(vila, cidadaoId);
		if (!c.isVivo()) {
			throw erro("Cidadão morto não pode equipar itens");
		}
		if (itemId == null) {
			throw erro("itemId é obrigatório");
		}
		Item item = itemRepository.findById(itemId).filter(i -> i.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Item não encontrado"));
		if (item.isEmAprimoramento()) {
			throw erro("Item em aprimoramento");
		}
		if (item.getCidadaoId() != null && !item.getCidadaoId().equals(c.getId())) {
			throw erro("Item já equipado por outro cidadão");
		}
		if (consultaTropas.emExpedicao(c.getId())) {
			throw erro("Não pode trocar equipamento em expedição");
		}
		boolean aneisGenerico = "ANEL".equalsIgnoreCase(slotTxt == null ? "" : slotTxt.trim());
		SlotEquipamento slot = aneisGenerico ? SlotEquipamento.ANEL_1 : parseSlot(slotTxt);
		validarCategoria(item, slot, aneisGenerico);
		validarIdade(c, item);
		validarPe(c, item);

		if (item.getCidadaoId() != null) {
			// movendo entre slots do próprio cidadão: libera o slot de origem antes
			item.setCidadaoId(null);
			item.setSlot(null);
			itemRepository.saveAndFlush(item);
		}
		if (aneisGenerico) {
			slot = escolherAnel(c);
		}
		itemRepository.findByCidadaoIdAndSlot(c.getId(), slot).ifPresent(anterior -> {
			anterior.setCidadaoId(null);
			anterior.setSlot(null);
			itemRepository.saveAndFlush(anterior);
		});
		item.setCidadaoId(c.getId());
		item.setSlot(slot);
		itemRepository.saveAndFlush(item);
		return mapa(c.getId());
	}

	@Transactional
	public Map<String, ItemDTO> desequipar(Vila vila, Long cidadaoId, String slotTxt) {
		Cidadao c = cidadaoDaVila(vila, cidadaoId);
		if (!c.isVivo()) {
			throw erro("Cidadão morto não pode alterar equipamento");
		}
		if (consultaTropas.emExpedicao(c.getId())) {
			throw erro("Não pode trocar equipamento em expedição");
		}
		SlotEquipamento slot = parseSlot(slotTxt);
		Item item = itemRepository.findByCidadaoIdAndSlot(c.getId(), slot)
				.orElseThrow(() -> erro("Slot vazio"));
		item.setCidadaoId(null);
		item.setSlot(null);
		itemRepository.saveAndFlush(item);
		return mapa(c.getId());
	}

	/** Mapa slot -> item equipado do cidadão. */
	public Map<String, ItemDTO> mapa(Long cidadaoId) {
		Map<String, ItemDTO> r = new LinkedHashMap<>();
		for (Item i : itemRepository.findByCidadaoId(cidadaoId)) {
			if (i.getSlot() != null) {
				r.put(i.getSlot().name(), ItemDTO.de(i));
			}
		}
		return r;
	}

	private SlotEquipamento escolherAnel(Cidadao c) {
		if (itemRepository.findByCidadaoIdAndSlot(c.getId(), SlotEquipamento.ANEL_1).isEmpty()) {
			return SlotEquipamento.ANEL_1;
		}
		if (itemRepository.findByCidadaoIdAndSlot(c.getId(), SlotEquipamento.ANEL_2).isEmpty()) {
			return SlotEquipamento.ANEL_2;
		}
		throw erro("Máximo 2 anéis por pessoa");
	}

	private void validarCategoria(Item item, SlotEquipamento slot, boolean aneisGenerico) {
		ItemSubtipo st = item.getSubtipo();
		boolean ok = switch (item.getCategoria()) {
			case ARMA -> slot == SlotEquipamento.ARMA;
			case FERRAMENTA -> slot == SlotEquipamento.FERRAMENTA;
			case ARMADURA -> ArmaduraCatalogo.de(st).map(a -> a.slot() == slot).orElse(false);
			case JOIA -> st == ItemSubtipo.COLAR ? slot == SlotEquipamento.COLAR
					: slot == SlotEquipamento.ANEL_1 || slot == SlotEquipamento.ANEL_2;
		};
		if (!ok) {
			String esperado = switch (item.getCategoria()) {
				case ARMA -> "Arma";
				case FERRAMENTA -> "Ferramenta";
				case ARMADURA -> st.getNomeExibicao();
				case JOIA -> st == ItemSubtipo.COLAR ? "Colar" : "Anel";
			};
			throw erro(st.getNomeExibicao() + " deve ir em slot " + esperado);
		}
	}

	private void validarIdade(Cidadao c, Item item) {
		int idade = c.getIdadeAnos();
		switch (item.getCategoria()) {
			case FERRAMENTA -> exigirIdade(idade, IDADE_MIN_FERRAMENTA_JOIA, "ferramentas");
			case JOIA -> exigirIdade(idade, IDADE_MIN_FERRAMENTA_JOIA, "joias");
			case ARMA -> exigirIdade(idade, IDADE_MIN_COMBATE, "armas");
			case ARMADURA -> exigirIdade(idade, IDADE_MIN_COMBATE, "armaduras");
		}
	}

	private static void exigirIdade(int idade, int min, String rotulo) {
		if (idade < min) {
			throw erro("Mínimo " + min + " anos para equipar " + rotulo);
		}
	}

	private void validarPe(Cidadao c, Item item) {
		int exigido = item.getNivel() - 1;
		switch (item.getCategoria()) {
			case ARMA, ARMADURA -> {
				if (peArtesao.peEfetivo(c, Profissao.GUERREIRO) < exigido) {
					throw erro("PE efetivo mínimo " + exigido + " necessário");
				}
			}
			case FERRAMENTA -> {
				Profissao prof = FerramentaCatalogo.de(item.getSubtipo()).map(FerramentaCatalogo::profissao)
						.orElseThrow(() -> erro("Ferramenta desconhecida"));
				int base = profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), prof)
						.map(CidadaoProfissao::getPontosBase).orElse(0);
				if (base < exigido) {
					throw erro("PE base mínimo " + exigido + " necessário em " + prof.name());
				}
			}
			case JOIA -> {
			}
		}
	}

	private Cidadao cidadaoDaVila(Vila vila, Long id) {
		return cidadaoRepository.findById(id).filter(c -> c.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Cidadão não encontrado"));
	}

	private static SlotEquipamento parseSlot(String txt) {
		try {
			return SlotEquipamento.valueOf(txt.trim().toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw erro("Slot inválido: " + txt);
		}
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}
}
