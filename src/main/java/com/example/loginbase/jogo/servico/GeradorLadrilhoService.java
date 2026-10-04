package com.example.loginbase.jogo.servico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.TipoTerreno;

/**
 * Gerador determinístico (puro, sem acesso a banco) dos 100 ladrilhos (10x10) de uma região:
 * terreno pelo percentual, {@code bonusBase} sorteado (0 a 100) e {@code bonusAdjacente}
 * calculado (25 por vizinho ortogonal com o mesmo terreno).
 */
@Service
public class GeradorLadrilhoService {

	public static final int LADO = 10;
	public static final int TOTAL_LADRILHOS = LADO * LADO;

	private static final int TERRENOS_POR_REGIAO = 3;
	private static final int BONUS_POR_VIZINHO = 25;
	private static final int[][] VIZINHOS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

	public record PercentualTerreno(TipoTerreno terreno, int percentual) {
	}

	public record LadrilhoGerado(int x, int y, TipoTerreno terreno, int bonusBase, int bonusAdjacente) {
		public int bonusTotal() {
			return bonusBase + bonusAdjacente;
		}
	}

	/**
	 * Gera os 100 ladrilhos da região, em ordem de varredura (y = 0..9; x = 0..9).
	 */
	public List<LadrilhoGerado> gerar(long semente, int indiceRegiao, List<PercentualTerreno> percentuais) {
		if (indiceRegiao < 1 || indiceRegiao > GradeRegioes.TOTAL) {
			throw new IllegalArgumentException("Índice de região inválido: " + indiceRegiao);
		}
		validar(percentuais);

		Random rng = new Random(misturar(semente, indiceRegiao));
		List<TipoTerreno> sorteio = new ArrayList<>(TOTAL_LADRILHOS);
		for (PercentualTerreno p : percentuais) {
			for (int i = 0; i < p.percentual(); i++) {
				sorteio.add(p.terreno());
			}
		}
		Collections.shuffle(sorteio, rng);

		TipoTerreno[][] grade = new TipoTerreno[LADO][LADO];
		int[][] base = new int[LADO][LADO];
		for (int y = 0; y < LADO; y++) {
			for (int x = 0; x < LADO; x++) {
				grade[y][x] = sorteio.get(y * LADO + x);
			}
		}
		for (int y = 0; y < LADO; y++) {
			for (int x = 0; x < LADO; x++) {
				base[y][x] = rng.nextInt(101);
			}
		}

		List<LadrilhoGerado> r = new ArrayList<>(TOTAL_LADRILHOS);
		for (int y = 0; y < LADO; y++) {
			for (int x = 0; x < LADO; x++) {
				int iguais = 0;
				for (int[] d : VIZINHOS) {
					int nx = x + d[0];
					int ny = y + d[1];
					if (nx >= 0 && nx < LADO && ny >= 0 && ny < LADO && grade[ny][nx] == grade[y][x]) {
						iguais++;
					}
				}
				r.add(new LadrilhoGerado(x, y, grade[y][x], base[y][x], BONUS_POR_VIZINHO * iguais));
			}
		}
		return List.copyOf(r);
	}

	private static void validar(List<PercentualTerreno> percentuais) {
		if (percentuais == null || percentuais.size() != TERRENOS_POR_REGIAO) {
			throw new IllegalArgumentException("A região deve ter exatamente " + TERRENOS_POR_REGIAO + " terrenos");
		}
		Set<TipoTerreno> vistos = EnumSet.noneOf(TipoTerreno.class);
		int soma = 0;
		for (PercentualTerreno p : percentuais) {
			if (p == null || p.terreno() == null) {
				throw new IllegalArgumentException("Terreno não informado");
			}
			if (p.percentual() < 0) {
				throw new IllegalArgumentException("Percentual negativo para " + p.terreno());
			}
			if (!vistos.add(p.terreno())) {
				throw new IllegalArgumentException("Terreno repetido: " + p.terreno());
			}
			soma += p.percentual();
		}
		if (soma != 100) {
			throw new IllegalArgumentException("Os percentuais devem somar 100, mas somam " + soma);
		}
	}

	/** Combina semente e índice (splitmix64) para sementes próximas gerarem mapas bem distintos. */
	private static long misturar(long semente, int indiceRegiao) {
		long z = semente + indiceRegiao * 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

}
