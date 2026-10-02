package com.example.loginbase.jogo.item;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.pedra.PedraDTO;
import com.example.loginbase.jogo.pedra.PedraRepository;
import com.example.loginbase.jogo.turno.TurnoService;

import lombok.RequiredArgsConstructor;

/** Inventário da vila (itens sem dono) e aprimoramento de itens em oficinas. */
@Service
@RequiredArgsConstructor
public class AprimoramentoService {

	private static final int NIVEL_MAXIMO = 10;

	private final ItemRepository itemRepository;
	private final FabricacaoRepository fabricacaoRepository;
	private final FabricacaoService fabricacaoService;
	private final CatalogoItens catalogo;
	private final TurnoService turnoService;
	private final PedraRepository pedraRepository;

	public record InventarioDTO(List<ItemDTO> itens, long total, int page, int pageSize) {
	}

	public record ItemDetalheDTO(ItemDTO item, List<PedraDTO> pedras) {
	}

	public record AprimorarRequest(Long oficinaId, Long artesaoId) {
	}

	@Transactional(readOnly = true)
	public InventarioDTO listar(Vila vila, ItemCategoria categoria, int page, int size) {
		int p = Math.max(0, page);
		int s = Math.min(100, Math.max(1, size));
		PageRequest pr = PageRequest.of(p, s, org.springframework.data.domain.Sort.by("id"));
		Page<Item> r = categoria == null ? itemRepository.findByVilaIdAndCidadaoIdIsNull(vila.getId(), pr)
				: itemRepository.findByVilaIdAndCidadaoIdIsNullAndCategoria(vila.getId(), categoria, pr);
		return new InventarioDTO(r.getContent().stream().map(ItemDTO::de).toList(), r.getTotalElements(), p, s);
	}

	@Transactional(readOnly = true)
	public ItemDetalheDTO detalhe(Vila vila, Long itemId) {
		Item item = itemDaVila(vila, itemId);
		return new ItemDetalheDTO(ItemDTO.de(item), pedraRepository.findByItemId(item.getId()).stream().map(PedraDTO::de).toList());
	}

	@Transactional
	public FabricacaoDTO aprimorar(Vila vila, Long itemId, AprimorarRequest req) {
		Item item = itemDaVila(vila, itemId);
		if (item.getCidadaoId() != null) {
			throw erro("Desequipe o item antes de aprimorar");
		}
		if (item.isEmAprimoramento()) {
			throw erro("Item já em aprimoramento");
		}
		int novo = item.getNivel() + 1;
		if (novo > NIVEL_MAXIMO) {
			throw erro("Nível máximo L10 atingido");
		}
		if (req == null || req.oficinaId() == null) {
			throw erro("Informe a oficina");
		}
		ItemCatalogado cat = catalogo.de(item.getSubtipo()).orElseThrow(() -> erro("Subtipo inválido"));
		Construcao c = fabricacaoService.construcaoDaVila(vila, req.oficinaId());
		fabricacaoService.verificarOficina(c);
		if (cat.oficina() != c.getTipo()) {
			throw erro("Oficina incorreta. " + item.getSubtipo().getNomeExibicao() + " é fabricada na "
					+ FabricacaoService.nomeOficina(cat.oficina()) + ".");
		}
		if (c.getEstado() != EstadoConstrucao.ATIVA) {
			throw erro("A oficina precisa estar ativa");
		}
		int max = RegrasFabricacao.nivelMaximo(c.getNivel());
		if (novo > max) {
			throw erro("Nível máximo L" + max + " para esta oficina");
		}
		Cidadao artesao = fabricacaoService.validarArtesao(vila, c, req.artesaoId(), novo, null,
				" para L" + novo);
		fabricacaoService.debitarCusto(vila, RegrasFabricacao.custoAprimoramento(cat.receitaBase(), novo));

		Fabricacao f = new Fabricacao(vila.getId(), c.getId(), artesao.getId(), item.getSubtipo(), novo,
				RegrasFabricacao.pf(novo), turnoService.getTurnoAtual().numero());
		f.setItemId(item.getId());
		f.setAtributoEscolhido(item.getAtributoEscolhido());
		f = fabricacaoRepository.saveAndFlush(f);
		item.setEmAprimoramento(true);
		itemRepository.saveAndFlush(item);
		return fabricacaoService.dto(f, fabricacaoService.trabalhador(c, vila, artesao.getId()));
	}

	private Item itemDaVila(Vila vila, Long id) {
		return itemRepository.findById(id).filter(i -> i.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Item não encontrado"));
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}
}
