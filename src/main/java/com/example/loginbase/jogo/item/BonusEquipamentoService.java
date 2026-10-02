package com.example.loginbase.jogo.item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.item.catalogo.FerramentaCatalogo;
import com.example.loginbase.jogo.item.catalogo.JoiaCatalogo;
import com.example.loginbase.jogo.pedra.BonusPedra;
import com.example.loginbase.jogo.pedra.PedraRepository;

/** Bônus de trabalho vindos dos itens equipados de um cidadão (ferramenta +L PE, PROF em PE, PROD em %). */
@Component
public class BonusEquipamentoService {

	private final ItemRepository itemRepository;

	private final PedraRepository pedraRepository;

	public BonusEquipamentoService(ItemRepository itemRepository, PedraRepository pedraRepository) {
		this.itemRepository = itemRepository;
		this.pedraRepository = pedraRepository;
	}

	/** +L do item no slot FERRAMENTA quando a ferramenta é da profissão informada; senão 0. */
	public int bonusFerramenta(Long cidadaoId, Profissao profissao) {
		return bonusFerramenta(equipados(cidadaoId), profissao);
	}

	/**
	 * Soma dos intrínsecos PROF: em ferramenta valem para a profissão da ferramenta; em
	 * arma/armadura/joia valem para GUERREIRO.
	 */
	public int bonusProfItens(Long cidadaoId, Profissao profissao) {
		return bonusProfItens(equipados(cidadaoId), profissao);
	}

	/** PE total vindo de itens (ferramenta + PROF) na profissão. */
	public int peItens(Long cidadaoId, Profissao profissao) {
		Equip eq = equipados(cidadaoId);
		return bonusFerramenta(eq, profissao) + bonusProfItens(eq, profissao);
	}

	/** Soma dos intrínsecos PROD (%) de todos os itens equipados (vale em qualquer profissão). */
	public int prodItens(Long cidadaoId) {
		int total = 0;
		Equip eq = equipados(cidadaoId);
		for (Item item : eq.itens()) {
			total += soma(eq, item, CodigoBonus.PROD);
		}
		return total;
	}

	/** Bônus na característica: anéis com atributo escolhido igual a c (bonusAnel(L)) + intrínsecos da característica em todos os itens equipados. */
	public int bonusCaracteristica(Long cidadaoId, Caracteristica c) {
		CodigoBonus codigo = CodigoBonus.valueOf(c.name());
		int total = 0;
		Equip eq = equipados(cidadaoId);
		for (Item item : eq.itens()) {
			if (item.getSubtipo() == ItemSubtipo.ANEL && item.getAtributoEscolhido() == codigo) {
				total += JoiaCatalogo.bonusAnel(item.getNivel());
			}
			total += soma(eq, item, codigo);
		}
		return total;
	}

	/** Vida vinda de itens: colar equipado (5 x L) + intrínsecos VIDA de todos os itens equipados. */
	public int vidaItens(Long cidadaoId) {
		int total = 0;
		Equip eq = equipados(cidadaoId);
		for (Item item : eq.itens()) {
			if (item.getSubtipo() == ItemSubtipo.COLAR) {
				total += JoiaCatalogo.vidaColar(item.getNivel());
			}
			total += soma(eq, item, CodigoBonus.VIDA);
		}
		return total;
	}

	/** Soma do intrínseco informado em todos os itens equipados (ATK%, DEF%, INI, CRIT...). */
	public int somaBonus(Long cidadaoId, CodigoBonus codigo) {
		int total = 0;
		Equip eq = equipados(cidadaoId);
		for (Item item : eq.itens()) {
			total += soma(eq, item, codigo);
		}
		return total;
	}

	private record Equip(List<Item> itens, Map<Long, List<BonusPedra>> pedras) {
	}

	private Equip equipados(Long cidadaoId) {
		if (cidadaoId == null) {
			return new Equip(List.of(), Map.of());
		}
		List<Item> itens = itemRepository.findByCidadaoId(cidadaoId).stream().filter(i -> i.getSlot() != null)
				.toList();
		if (itens.isEmpty()) {
			return new Equip(itens, Map.of());
		}
		Map<Long, List<BonusPedra>> pedras = pedraRepository.findByItemIdIn(itens.stream().map(Item::getId).toList())
				.stream().collect(Collectors.groupingBy(pd -> pd.getItemId(),
						Collectors.flatMapping(pd -> pd.getBonus().stream(), Collectors.toList())));
		return new Equip(itens, pedras);
	}

	private int bonusFerramenta(Equip eq, Profissao profissao) {
		int total = 0;
		for (Item item : eq.itens()) {
			if (item.getSlot() == SlotEquipamento.FERRAMENTA && profissaoDaFerramenta(item) == profissao) {
				total += item.getNivel();
			}
		}
		return total;
	}

	private int bonusProfItens(Equip eq, Profissao profissao) {
		int total = 0;
		for (Item item : eq.itens()) {
			if (profissaoDoBonusProf(item) == profissao) {
				total += soma(eq, item, CodigoBonus.PROF);
			}
		}
		return total;
	}

	private Profissao profissaoDaFerramenta(Item item) {
		return FerramentaCatalogo.de(item.getSubtipo()).map(FerramentaCatalogo::profissao).orElse(null);
	}

	private Profissao profissaoDoBonusProf(Item item) {
		if (item.getCategoria() == ItemCategoria.FERRAMENTA) {
			return profissaoDaFerramenta(item);
		}
		return Profissao.GUERREIRO;
	}

	private int soma(Equip eq, Item item, CodigoBonus codigo) {
		int total = 0;
		if (item.getBonus() != null) {
			for (BonusItem b : item.getBonus()) {
				if (b.codigo() == codigo) {
					total += b.valor();
				}
			}
		}
		for (BonusPedra b : eq.pedras().getOrDefault(item.getId(), List.of())) {
			if (b.codigo() == codigo) {
				total += b.valor();
			}
		}
		return total;
	}

}
