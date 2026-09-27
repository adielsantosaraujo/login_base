package com.example.loginbase.jogo.masmorra.combate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Busca em largura (BFS) sobre o grid tático: casas ortogonais (sem
 * diagonais), livres de obstáculo e de combatentes vivos.
 */
public final class Caminhos {

	private static final int[][] DIRECOES = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };

	private Caminhos() {
	}

	/**
	 * Menor caminho ortogonal de {@code inicio} até {@code fim} (sem incluir
	 * {@code inicio}), respeitando a grade {@code largura x altura},
	 * obstáculos e combatentes vivos. Vazio se {@code fim} não for
	 * alcançável em até {@code movimento} passos.
	 */
	public static List<Posicao> caminhoMaisCurto(Posicao inicio, Posicao fim, int movimento, int largura, int altura,
			Set<Posicao> obstaculos, Collection<Combatente> combatentes) {
		if (inicio.equals(fim)) {
			return List.of();
		}
		Set<Posicao> ocupadas = ocupadasPor(combatentes);
		Map<Posicao, Posicao> veioDe = new HashMap<>();
		Map<Posicao, Integer> distancia = new HashMap<>();
		Queue<Posicao> fila = new ArrayDeque<>();
		veioDe.put(inicio, null);
		distancia.put(inicio, 0);
		fila.add(inicio);
		while (!fila.isEmpty()) {
			Posicao atual = fila.poll();
			int distanciaAtual = distancia.get(atual);
			if (atual.equals(fim)) {
				break;
			}
			if (distanciaAtual >= movimento) {
				continue;
			}
			for (Posicao vizinha : vizinhas(atual, largura, altura, obstaculos, ocupadas)) {
				if (veioDe.containsKey(vizinha)) {
					continue;
				}
				veioDe.put(vizinha, atual);
				distancia.put(vizinha, distanciaAtual + 1);
				fila.add(vizinha);
			}
		}
		if (!distancia.containsKey(fim)) {
			return List.of();
		}
		List<Posicao> caminho = new ArrayList<>();
		Posicao atual = fim;
		while (veioDe.get(atual) != null) {
			caminho.add(0, atual);
			atual = veioDe.get(atual);
		}
		return caminho;
	}

	/**
	 * Todas as casas alcançáveis a partir de {@code origem} em até
	 * {@code movimento} passos, incluindo a própria {@code origem} (ficar
	 * parado é sempre uma opção).
	 */
	public static Set<Posicao> casasAlcancaveis(Posicao origem, int movimento, int largura, int altura,
			Set<Posicao> obstaculos, Collection<Combatente> combatentes) {
		Set<Posicao> ocupadas = ocupadasPor(combatentes);
		Map<Posicao, Integer> distancia = new HashMap<>();
		Queue<Posicao> fila = new ArrayDeque<>();
		distancia.put(origem, 0);
		fila.add(origem);
		while (!fila.isEmpty()) {
			Posicao atual = fila.poll();
			int distanciaAtual = distancia.get(atual);
			if (distanciaAtual >= movimento) {
				continue;
			}
			for (Posicao vizinha : vizinhas(atual, largura, altura, obstaculos, ocupadas)) {
				if (distancia.containsKey(vizinha)) {
					continue;
				}
				distancia.put(vizinha, distanciaAtual + 1);
				fila.add(vizinha);
			}
		}
		return distancia.keySet();
	}

	private static List<Posicao> vizinhas(Posicao atual, int largura, int altura, Set<Posicao> obstaculos,
			Set<Posicao> ocupadas) {
		List<Posicao> vizinhas = new ArrayList<>(4);
		for (int[] direcao : DIRECOES) {
			Posicao vizinha = new Posicao(atual.x() + direcao[0], atual.y() + direcao[1]);
			if (vizinha.x() < 0 || vizinha.x() >= largura || vizinha.y() < 0 || vizinha.y() >= altura) {
				continue;
			}
			if (obstaculos.contains(vizinha) || ocupadas.contains(vizinha)) {
				continue;
			}
			vizinhas.add(vizinha);
		}
		return vizinhas;
	}

	private static Set<Posicao> ocupadasPor(Collection<Combatente> combatentes) {
		Set<Posicao> ocupadas = new HashSet<>();
		for (Combatente combatente : combatentes) {
			if (combatente.vivo()) {
				ocupadas.add(combatente.posicao());
			}
		}
		return ocupadas;
	}

}
