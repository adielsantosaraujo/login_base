package com.example.loginbase.jogo.quartel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.CategoriaItem;
import com.example.loginbase.jogo.catalogo.SlotEquipamento;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.VilaService;

/**
 * Troca o item equipado nos slots Arma e Armadura de uma unidade da vila do
 * usuário por outro item compatível {@code DISPONIVEL} do próprio inventário
 * (ver spec game-army — Requirement: Troca de equipamento, e design.md,
 * Decisão D12). Com a vila travada ({@link VilaService#obterParaAtualizacao}),
 * valida em série: unidade existente e da própria vila, unidade fora de
 * masmorra, slot já persistido (hoje só ARMA e ARMADURA — os 7 slots futuros
 * de {@link SlotEquipamento} ainda não aceitam itens) e item compatível,
 * {@code DISPONIVEL} e da própria vila. O item retirado volta a
 * {@code DISPONIVEL}; o novo passa a {@code EQUIPADO}. Não há endpoint de
 * remoção: Arma e Armadura permanecem sempre ocupados.
 */
@Service
@Transactional
public class EquipamentoService {

	private final VilaService vilaService;
	private final UnidadeRepository unidadeRepository;
	private final ItemRepository itemRepository;

	public EquipamentoService(VilaService vilaService, UnidadeRepository unidadeRepository,
			ItemRepository itemRepository) {
		this.vilaService = vilaService;
		this.unidadeRepository = unidadeRepository;
		this.itemRepository = itemRepository;
	}

	/**
	 * Troca o item do slot {@code slot} da unidade {@code unidadeId} pelo item
	 * {@code itemId}.
	 *
	 * @throws RecursoNaoEncontradoException  se a unidade não existir ou não for da vila do usuário
	 * @throws RegraJogoException com {@link CodigoErro#UNIDADE_EM_MASMORRA} se a unidade estiver
	 *                            {@code EM_MASMORRA}
	 * @throws RegraJogoException com {@link CodigoErro#ITEM_INDISPONIVEL} se {@code slot} for um
	 *                            slot futuro, ou se {@code itemId} não existir, for de outra vila,
	 *                            não estiver {@code DISPONIVEL} ou for incompatível com o slot (ARMA:
	 *                            modelo exigido pelo tipo da unidade; ARMADURA: categoria ARMADURA)
	 */
	public void trocar(long usuarioId, long unidadeId, SlotEquipamento slot, long itemId) {
		Vila vila = vilaService.obterParaAtualizacao(usuarioId);

		Unidade unidade = unidadeRepository.findById(unidadeId)
				.filter(u -> u.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: " + unidadeId));

		if (unidade.getStatus() == StatusUnidade.EM_MASMORRA) {
			throw new RegraJogoException(CodigoErro.UNIDADE_EM_MASMORRA,
					"A unidade está em uma masmorra; troque o equipamento quando ela voltar.");
		}

		if (slot.getCategoriaAceita() == null) {
			throw new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
					"O slot " + slot.getRotulo() + " ainda não aceita itens.");
		}

		Item novoItem = itemRepository.findById(itemId)
				.filter(item -> item.getVilaId().equals(vila.getId()))
				.filter(item -> item.getStatus() == StatusItem.DISPONIVEL)
				.filter(item -> compativel(slot, unidade, item))
				.orElseThrow(() -> new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
						"Item indisponível ou incompatível com o slot " + slot.getRotulo() + ": " + itemId));

		Long itemAntigoId = itemAtualDoSlot(slot, unidade);
		Item itemAntigo = itemRepository.findById(itemAntigoId)
				.orElseThrow(() -> new IllegalStateException("Item equipado não encontrado: " + itemAntigoId));
		itemAntigo.setStatus(StatusItem.DISPONIVEL);
		itemRepository.save(itemAntigo);

		novoItem.setStatus(StatusItem.EQUIPADO);
		itemRepository.save(novoItem);

		atualizarItemDoSlot(slot, unidade, novoItem.getId());
		unidadeRepository.save(unidade);
	}

	private static boolean compativel(SlotEquipamento slot, Unidade unidade, Item item) {
		return switch (slot) {
			case ARMA -> item.getModelo() == unidade.getTipo().armaExigida();
			case ARMADURA -> item.getModelo().categoria() == CategoriaItem.ARMADURA;
			default -> false;
		};
	}

	private static Long itemAtualDoSlot(SlotEquipamento slot, Unidade unidade) {
		return switch (slot) {
			case ARMA -> unidade.getArmaItemId();
			case ARMADURA -> unidade.getArmaduraItemId();
			default -> throw new IllegalStateException("Slot sem persistência de equipamento: " + slot);
		};
	}

	private static void atualizarItemDoSlot(SlotEquipamento slot, Unidade unidade, Long novoItemId) {
		switch (slot) {
			case ARMA -> unidade.setArmaItemId(novoItemId);
			case ARMADURA -> unidade.setArmaduraItemId(novoItemId);
			default -> throw new IllegalStateException("Slot sem persistência de equipamento: " + slot);
		}
	}

}
