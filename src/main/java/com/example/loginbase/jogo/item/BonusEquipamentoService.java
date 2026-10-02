package com.example.loginbase.jogo.item;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.item.catalogo.FerramentaCatalogo;
import com.example.loginbase.jogo.item.catalogo.JoiaCatalogo;

/** Bônus de trabalho vindos dos itens equipados de um cidadão (ferramenta +L PE, PROF em PE, PROD em %). */
@Component
public class BonusEquipamentoService {

	private final ItemRepository itemRepository;

	public BonusEquipamentoService(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
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
		List<Item> itens = equipados(cidadaoId);
		return bonusFerramenta(itens, profissao) + bonusProfItens(itens, profissao);
	}

	/** Soma dos intrínsecos PROD (%) de todos os itens equipados (vale em qualquer profissão). */
	public int prodItens(Long cidadaoId) {
		int total = 0;
		for (Item item : equipados(cidadaoId)) {
			total += soma(item, CodigoBonus.PROD);
		}
		return total;
	}

	/** Bônus na característica: anéis com atributo escolhido igual a c (bonusAnel(L)) + intrínsecos da característica em todos os itens equipados. */
	public int bonusCaracteristica(Long cidadaoId, Caracteristica c) {
		CodigoBonus codigo = CodigoBonus.valueOf(c.name());
		int total = 0;
		for (Item item : equipados(cidadaoId)) {
			if (item.getSubtipo() == ItemSubtipo.ANEL && item.getAtributoEscolhido() == codigo) {
				total += JoiaCatalogo.bonusAnel(item.getNivel());
			}
			total += soma(item, codigo);
		}
		return total;
	}

	/** Vida vinda de itens: colar equipado (5 x L) + intrínsecos VIDA de todos os itens equipados. */
	public int vidaItens(Long cidadaoId) {
		int total = 0;
		for (Item item : equipados(cidadaoId)) {
			if (item.getSubtipo() == ItemSubtipo.COLAR) {
				total += JoiaCatalogo.vidaColar(item.getNivel());
			}
			total += soma(item, CodigoBonus.VIDA);
		}
		return total;
	}

	/** Soma do intrínseco informado em todos os itens equipados (ATK%, DEF%, INI, CRIT...). */
	public int somaBonus(Long cidadaoId, CodigoBonus codigo) {
		int total = 0;
		for (Item item : equipados(cidadaoId)) {
			total += soma(item, codigo);
		}
		return total;
	}

	private List<Item> equipados(Long cidadaoId) {
		if (cidadaoId == null) {
			return List.of();
		}
		return itemRepository.findByCidadaoId(cidadaoId).stream().filter(i -> i.getSlot() != null).toList();
	}

	private int bonusFerramenta(List<Item> itens, Profissao profissao) {
		int total = 0;
		for (Item item : itens) {
			if (item.getSlot() == SlotEquipamento.FERRAMENTA && profissaoDaFerramenta(item) == profissao) {
				total += item.getNivel();
			}
		}
		return total;
	}

	private int bonusProfItens(List<Item> itens, Profissao profissao) {
		int total = 0;
		for (Item item : itens) {
			if (profissaoDoBonusProf(item) == profissao) {
				total += soma(item, CodigoBonus.PROF);
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

	private int soma(Item item, CodigoBonus codigo) {
		int total = 0;
		if (item.getBonus() != null) {
			for (BonusItem b : item.getBonus()) {
				if (b.codigo() == codigo) {
					total += b.valor();
				}
			}
		}
		return total;
	}

}
