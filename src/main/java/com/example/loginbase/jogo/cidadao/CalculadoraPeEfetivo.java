package com.example.loginbase.jogo.cidadao;

import org.springframework.stereotype.Component;

/** PE efetivo = PE base + soma de floor(característica ÷ 5) das ligadas + bônus de ferramenta/itens (0 por enquanto). */
@Component
public class CalculadoraPeEfetivo {

	public int calcular(Cidadao cidadao, Profissao profissao, int peBase) {
		int bonus = 0;
		for (Caracteristica c : profissao.getCaracteristicas()) {
			bonus += cidadao.valorCaracteristica(c) / 5;
		}
		return peBase + bonus + bonusFerramenta(cidadao, profissao) + bonusItens(cidadao, profissao);
	}

	private int bonusFerramenta(Cidadao cidadao, Profissao profissao) {
		return 0;
	}

	private int bonusItens(Cidadao cidadao, Profissao profissao) {
		return 0;
	}

}
