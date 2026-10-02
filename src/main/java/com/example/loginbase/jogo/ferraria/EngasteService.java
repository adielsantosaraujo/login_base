package com.example.loginbase.jogo.ferraria;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemDTO;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.pedra.BonusPedra;
import com.example.loginbase.jogo.pedra.Pedra;
import com.example.loginbase.jogo.pedra.PedraDTO;
import com.example.loginbase.jogo.pedra.PedraRepository;
import com.example.loginbase.jogo.pedra.TipoPedra;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;

import lombok.RequiredArgsConstructor;

/** Engaste e remoção de pedras de bônus em itens, na Ferraria. */
@Service
@RequiredArgsConstructor
public class EngasteService {

	private final PedraRepository pedraRepository;
	private final ItemRepository itemRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final EstoqueService estoqueService;

	public record EngasteRequest(Long pedraId, Long itemId) {
	}

	public record RemoverPedraRequest(Long itemId, Long pedraId) {
	}

	public record EngasteResultadoDTO(Long pedraId, Long itemId, int custoOuro, TipoPedra qualidadePedra,
			List<BonusPedra> bonus, int slotsLivresApos) {
	}

	public record RemocaoResultadoDTO(Long itemId, Long pedraRemovidaId, int slotsLivresApos,
			boolean pedraDestruida) {
	}

	public record ItemCompativelDTO(ItemDTO item, int slotsTotais, int slotsLivres) {
	}

	@Transactional(readOnly = true)
	public List<PedraDTO> pedras(Vila vila) {
		return pedraRepository.findByVilaIdAndItemIdIsNullOrderByIdAsc(vila.getId()).stream().map(PedraDTO::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ItemCompativelDTO> itensCompativeis(Vila vila) {
		return itemRepository.findByVilaId(vila.getId()).stream()
				.filter(i -> i.getCidadaoId() == null && !i.isEmAprimoramento() && i.getQualidade() != Qualidade.SIMPLES)
				.map(i -> new ItemCompativelDTO(ItemDTO.de(i), i.getQualidade().getSlotsPedra(), slotsLivres(i)))
				.filter(d -> d.slotsLivres() > 0).toList();
	}

	@Transactional
	public EngasteResultadoDTO engastar(Vila vila, EngasteRequest req) {
		if (req == null || req.pedraId() == null || req.itemId() == null) {
			throw erro("Informe a pedra e o item");
		}
		Pedra pedra = pedraRepository.findById(req.pedraId()).filter(p -> p.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Pedra não encontrada"));
		Item item = itemDaVila(vila, req.itemId());
		if (pedra.getItemId() != null) {
			throw erro("A pedra já está engastada em outro item");
		}
		if (item.getQualidade() == Qualidade.SIMPLES) {
			throw erro("Itens Simples não aceitam pedras");
		}
		if (item.getCidadaoId() != null) {
			throw erro("Desequipe o item antes de engastar");
		}
		if (item.isEmAprimoramento()) {
			throw erro("Item em aprimoramento");
		}
		if (slotsLivres(item) <= 0) {
			throw erro("Sem slots de pedra disponíveis");
		}
		boolean ferraria = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA).stream()
				.anyMatch(c -> c.getTipo() == TipoConstrucao.FERRARIA);
		if (!ferraria) {
			throw erro("É necessária uma Ferraria ativa");
		}
		int custo = pedra.getQualidade().getCustoEngaste();
		estoqueService.debitar(vila, Map.of(Recurso.OURO, BigDecimal.valueOf(custo)), "Ouro insuficiente");
		pedra.setItemId(item.getId());
		pedraRepository.saveAndFlush(pedra);
		return new EngasteResultadoDTO(pedra.getId(), item.getId(), custo, pedra.getQualidade(),
				List.copyOf(pedra.getBonus()), slotsLivres(item));
	}

	@Transactional
	public RemocaoResultadoDTO remover(Vila vila, RemoverPedraRequest req) {
		if (req == null || req.itemId() == null || req.pedraId() == null) {
			throw erro("Informe o item e a pedra");
		}
		Item item = itemDaVila(vila, req.itemId());
		Pedra pedra = pedraRepository.findById(req.pedraId())
				.filter(p -> item.getId().equals(p.getItemId()))
				.orElseThrow(() -> erro("O item não contém esta pedra"));
		if (item.getCidadaoId() != null) {
			throw erro("Desequipe o item antes de remover a pedra");
		}
		if (item.isEmAprimoramento()) {
			throw erro("Item em aprimoramento");
		}
		pedraRepository.delete(pedra);
		pedraRepository.flush();
		return new RemocaoResultadoDTO(item.getId(), pedra.getId(), slotsLivres(item), true);
	}

	private int slotsLivres(Item item) {
		return Math.max(0, item.getQualidade().getSlotsPedra() - (int) pedraRepository.countByItemId(item.getId()));
	}

	private Item itemDaVila(Vila vila, Long id) {
		return itemRepository.findById(id).filter(i -> i.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Item não encontrado"));
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}
}
