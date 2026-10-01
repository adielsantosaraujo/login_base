package com.example.loginbase.jogo.servico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Jazida;
import com.example.loginbase.jogo.modelo.LadrilhoJazida;

/**
 * Gerador determinístico (puro, sem acesso a banco) das jazidas dos 100 ladrilhos (10x10)
 * de uma região de Coleta, a partir da semente da vila e do índice da região.
 * <p>
 * Os percentuais da distribuição somam 100%, então cada jazida recebe exatamente
 * {@code percentual} ladrilhos; as garantias mínimas (10 Floresta, 10 Rocha, 8 Barreiro)
 * valem por construção (25, 20 e 15).
 */
@Service
public class GeradorJazidaService {

	public static final int LADO = 10;
	public static final int TOTAL_LADRILHOS = LADO * LADO;

	private static final Map<Jazida, Integer> QUANTIDADES = new EnumMap<>(Jazida.class);

	static {
		QUANTIDADES.put(Jazida.FLORESTA, 25);
		QUANTIDADES.put(Jazida.ROCHA, 20);
		QUANTIDADES.put(Jazida.BARREIRO, 15);
		QUANTIDADES.put(Jazida.VEIO_DE_FERRO, 10);
		QUANTIDADES.put(Jazida.VEIO_DE_CARVAO, 10);
		QUANTIDADES.put(Jazida.SALINA, 7);
		QUANTIDADES.put(Jazida.ENXOFRE, 5);
		QUANTIDADES.put(Jazida.CAMPO, 8);
	}

	/**
	 * Gera as 100 jazidas da região. Chave do mapa: {@code "x,y"} (0 a 9), em ordem
	 * de linha (y) e depois coluna (x).
	 */
	public Map<String, Jazida> gerarJazidasPorRegiao(long semente, int indiceRegiao) {
		if (indiceRegiao < 1 || indiceRegiao > GradeRegioes.TOTAL) {
			throw new IllegalArgumentException("Índice de região inválido: " + indiceRegiao);
		}
		List<Jazida> sorteio = new ArrayList<>(TOTAL_LADRILHOS);
		QUANTIDADES.forEach((jazida, qtd) -> {
			for (int i = 0; i < qtd; i++) {
				sorteio.add(jazida);
			}
		});
		Collections.shuffle(sorteio, new Random(misturar(semente, indiceRegiao)));

		Map<String, Jazida> mapa = new LinkedHashMap<>();
		int i = 0;
		for (int y = 0; y < LADO; y++) {
			for (int x = 0; x < LADO; x++) {
				mapa.put(chave(x, y), sorteio.get(i++));
			}
		}
		return mapa;
	}

	/** Mesma geração, no formato de entidades para persistência pelos consumidores. */
	public List<LadrilhoJazida> gerarLadrilhos(long semente, int indiceRegiao, Long regiaoId) {
		List<LadrilhoJazida> r = new ArrayList<>(TOTAL_LADRILHOS);
		gerarJazidasPorRegiao(semente, indiceRegiao).forEach((k, jazida) -> {
			String[] p = k.split(",");
			r.add(new LadrilhoJazida(regiaoId, Integer.valueOf(p[0]), Integer.valueOf(p[1]), jazida));
		});
		return r;
	}

	public static String chave(int x, int y) {
		return x + "," + y;
	}

	/** Combina semente e índice (splitmix64) para sementes próximas gerarem mapas bem distintos. */
	private static long misturar(long semente, int indiceRegiao) {
		long z = semente + indiceRegiao * 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

}
