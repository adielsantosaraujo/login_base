package com.example.loginbase.jogo.cidadao;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.item.BonusEquipamentoService;

/** PE efetivo = PE base + soma de floor(característica ÷ 5) das ligadas + bônus de ferramenta (+L) + PROF de itens. */
@Component
public class CalculadoraPeEfetivo {

	private final BonusEquipamentoService bonusEquipamentoService;

	public CalculadoraPeEfetivo(BonusEquipamentoService bonusEquipamentoService) {
		this.bonusEquipamentoService = bonusEquipamentoService;
	}

	public int calcular(Cidadao cidadao, Profissao profissao, int peBase) {
		int bonus = 0;
		for (Caracteristica c : profissao.getCaracteristicas()) {
			bonus += caracteristicaTotal(cidadao, c) / 5;
		}
		return peBase + bonus + bonusFerramenta(cidadao, profissao) + bonusItens(cidadao, profissao);
	}

	/** Característica total = base + bônus de itens equipados (anéis e intrínsecos); 0 de bônus para transiente. */
	public int caracteristicaTotal(Cidadao cidadao, Caracteristica c) {
		int extra = cidadao.getId() == null ? 0 : bonusEquipamentoService.bonusCaracteristica(cidadao.getId(), c);
		return cidadao.valorCaracteristica(c) + extra;
	}

	/** PE vindo de itens equipados (ferramenta + PROF); 0 para cidadão transiente. */
	public int peItens(Cidadao cidadao, Profissao profissao) {
		return bonusFerramenta(cidadao, profissao) + bonusItens(cidadao, profissao);
	}

	private int bonusFerramenta(Cidadao cidadao, Profissao profissao) {
		return cidadao.getId() == null ? 0 : bonusEquipamentoService.bonusFerramenta(cidadao.getId(), profissao);
	}

	private int bonusItens(Cidadao cidadao, Profissao profissao) {
		return cidadao.getId() == null ? 0 : bonusEquipamentoService.bonusProfItens(cidadao.getId(), profissao);
	}

}
