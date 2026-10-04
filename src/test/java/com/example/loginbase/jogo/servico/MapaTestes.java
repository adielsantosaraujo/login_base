package com.example.loginbase.jogo.servico;

import java.util.List;

import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.servico.GeradorMapaService.RegiaoGerada;

/** Utilitário de testes para escolher seleções iniciais de 3 regiões conexas. */
public final class MapaTestes {

	private MapaTestes() {
	}

	/** Três índices conexos do mapa, com (ou sem) ao menos uma região Urbana. */
	public static List<Integer> selecioneValidas(List<RegiaoGerada> mapa, boolean comUrbana) {
		return selecionePorTipos(mapa.stream().map(RegiaoGerada::tipo).toList(), comUrbana);
	}

	/** Igual a {@link #selecioneValidas}, a partir dos tipos das regiões 1 a 16 (em ordem). */
	public static List<Integer> selecionePorTipos(List<TipoRegiao> tipos, boolean comUrbana) {
		GeradorMapaService gerador = new GeradorMapaService();
		for (int a = 1; a <= GradeRegioes.TOTAL; a++) {
			for (int b = a + 1; b <= GradeRegioes.TOTAL; b++) {
				for (int c = b + 1; c <= GradeRegioes.TOTAL; c++) {
					List<Integer> candidato = List.of(a, b, c);
					boolean temUrbana = candidato.stream().anyMatch(i -> tipos.get(i - 1) == TipoRegiao.URBANA);
					if (temUrbana == comUrbana && gerador.conectadas(candidato)) {
						return candidato;
					}
				}
			}
		}
		throw new IllegalStateException("Nenhuma seleção válida encontrada");
	}

	/** Primeira região Urbana (menor índice) entre as selecionadas. */
	public static int primeiraUrbana(List<RegiaoGerada> mapa, List<Integer> indices) {
		return indices.stream().filter(i -> mapa.get(i - 1).tipo() == TipoRegiao.URBANA).findFirst().orElseThrow();
	}

}
