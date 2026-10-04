package com.example.loginbase.jogo.servico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.TipoRegiao;

/**
 * Gera o mapa 4x4 de regiões (tipos e percentuais de terreno) de forma determinística a partir de uma semente.
 * Segue o algoritmo de referência geracao-mapa.js (regras R8 a R16), usando {@link Random}.
 */
@Service
public class GeradorMapaService {

	private static final int MIN_POR_TIPO = 2;
	private static final int MAX_POR_TIPO = 4;
	private static final int MAX_TIPOS_COM_MAX = 2;

	public record TerrenoGerado(TipoTerreno terreno, int posicao, int percentual) {
	}

	public record RegiaoGerada(int indice, TipoRegiao tipo, List<TerrenoGerado> terrenos) {
	}

	/** Gera as 16 regiões (índices 1 a 16) da semente; os terrenos de cada região vêm ordenados por posição. */
	public List<RegiaoGerada> gerar(long semente) {
		Random rng = new Random(semente);
		List<TipoRegiao> tipos = TipoRegiao.atuais();
		int[] quantidades = sortearQuantidades(rng, tipos.size());

		List<TipoRegiao> layout = new ArrayList<>(GradeRegioes.TOTAL);
		for (int i = 0; i < tipos.size(); i++) {
			for (int k = 0; k < quantidades[i]; k++) {
				layout.add(tipos.get(i));
			}
		}
		embaralhar(rng, layout);

		List<RegiaoGerada> mapa = new ArrayList<>(GradeRegioes.TOTAL);
		for (int i = 0; i < layout.size(); i++) {
			TipoRegiao tipo = layout.get(i);
			List<TipoTerreno> ordem = new ArrayList<>(tipo.terrenos());
			embaralhar(rng, ordem);
			int b1 = inteiro(rng, 20, 60);
			int b2 = inteiro(rng, 20, 90 - b1);
			int b3 = 100 - b1 - b2;
			int[] percentuais = { b1, b2, b3 };
			List<TerrenoGerado> terrenos = new ArrayList<>(ordem.size());
			for (int pos = 0; pos < ordem.size(); pos++) {
				terrenos.add(new TerrenoGerado(ordem.get(pos), pos + 1, percentuais[pos]));
			}
			mapa.add(new RegiaoGerada(i + 1, tipo, List.copyOf(terrenos)));
		}
		return List.copyOf(mapa);
	}

	/** Se o conjunto de regiões forma um grupo conexo (vizinhança ortogonal). */
	public boolean conectadas(List<Integer> indices) {
		if (indices == null || indices.isEmpty()) {
			return false;
		}
		Set<Integer> pendentes = new HashSet<>(indices);
		Deque<Integer> fila = new ArrayDeque<>();
		Integer inicio = indices.get(0);
		pendentes.remove(inicio);
		fila.add(inicio);
		while (!fila.isEmpty()) {
			int atual = fila.poll();
			for (Integer outro : new ArrayList<>(pendentes)) {
				if (GradeRegioes.adjacente(atual, outro)) {
					pendentes.remove(outro);
					fila.add(outro);
				}
			}
		}
		return pendentes.isEmpty();
	}

	/** Amostragem por rejeição: soma 16 e no máximo 2 tipos com a quantidade máxima. */
	private int[] sortearQuantidades(Random rng, int numTipos) {
		while (true) {
			int[] q = new int[numTipos];
			int soma = 0;
			int comMax = 0;
			for (int i = 0; i < numTipos; i++) {
				q[i] = inteiro(rng, MIN_POR_TIPO, MAX_POR_TIPO);
				soma += q[i];
				if (q[i] == MAX_POR_TIPO) {
					comMax++;
				}
			}
			if (soma == GradeRegioes.TOTAL && comMax <= MAX_TIPOS_COM_MAX) {
				return q;
			}
		}
	}

	private static int inteiro(Random rng, int min, int max) {
		return min + rng.nextInt(max - min + 1);
	}

	/** Fisher-Yates idêntico ao da referência. */
	private static <T> void embaralhar(Random rng, List<T> lista) {
		for (int i = lista.size() - 1; i > 0; i--) {
			int j = inteiro(rng, 0, i);
			T tmp = lista.get(i);
			lista.set(i, lista.get(j));
			lista.set(j, tmp);
		}
	}
}
