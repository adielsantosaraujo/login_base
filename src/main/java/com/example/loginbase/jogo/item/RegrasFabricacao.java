package com.example.loginbase.jogo.item;

import java.util.EnumMap;
import java.util.Map;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.recurso.Recurso;

/** Regras puras de fabricação de itens. */
public final class RegrasFabricacao {

	private RegrasFabricacao() {
	}

	/** Custo no nível L: quantidades x L; FERRO vira ACO a partir do nível 6. */
	public static Map<Recurso, Integer> custo(Map<Recurso, Integer> receitaBase, int nivel) {
		Map<Recurso, Integer> r = new EnumMap<>(Recurso.class);
		receitaBase.forEach((rec, qtd) -> {
			Recurso destino = (rec == Recurso.FERRO && nivel >= 6) ? Recurso.ACO : rec;
			r.merge(destino, qtd * nivel, Integer::sum);
		});
		return r;
	}

	/** Custo de aprimorar para novoL: 50% do custo cheio, arredondado para cima. */
	public static Map<Recurso, Integer> custoAprimoramento(Map<Recurso, Integer> receitaBase, int novoNivel) {
		Map<Recurso, Integer> r = new EnumMap<>(Recurso.class);
		custo(receitaBase, novoNivel).forEach((rec, qtd) -> r.put(rec, (qtd + 1) / 2));
		return r;
	}

	public static int pf(int nivel) {
		return 1 + nivel;
	}

	public static int peMinimo(int nivel) {
		return 2 * nivel - 2;
	}

	public static int nivelMaximo(NivelConstrucao nivel) {
		return switch (nivel) {
			case N1 -> 3;
			case N2 -> 6;
			case N3 -> 10;
		};
	}

	public static double multiplicadorAtributo(int nivel) {
		return 1 + 0.2 * (nivel - 1);
	}

	public static FaixaBonus faixa(int nivel) {
		if (nivel <= 4) {
			return FaixaBonus.BAIXA;
		}
		return nivel <= 7 ? FaixaBonus.MEDIA : FaixaBonus.ALTA;
	}
}
