package com.example.loginbase.jogo.batalha;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Ordem de ação da rodada: iniciativa base + 1d6; desempate VEL desc, id asc, TROPA antes de INIMIGO. */
public final class CalculadorIniciativa {

	private CalculadorIniciativa() {
	}

	/** Combatente com a iniciativa total sorteada na rodada. */
	public record Entrada(Combatente combatente, int total) {
	}

	public static final Comparator<Entrada> ORDEM = Comparator.comparingInt(Entrada::total).reversed()
			.thenComparing(Comparator.comparingInt((Entrada e) -> e.combatente().vel()).reversed())
			.thenComparingLong(e -> e.combatente().id())
			.thenComparing(e -> e.combatente().lado());

	/** Iniciativa base + 1d6 (consome um nextInt(6)). */
	public static int rolar(Combatente c, Random rng) {
		return c.iniciativaBase() + 1 + rng.nextInt(6);
	}

	/** Sorteia 1d6 de cada combatente na ordem recebida e devolve a ordem de ação. */
	public static List<Combatente> ordenar(List<Combatente> vivos, Random rng) {
		List<Entrada> entradas = new ArrayList<>();
		for (Combatente c : vivos) {
			entradas.add(new Entrada(c, rolar(c, rng)));
		}
		return ordenarPorTotais(entradas);
	}

	/** Ordena totais já sorteados (útil para testar desempate). */
	public static List<Combatente> ordenarPorTotais(List<Entrada> entradas) {
		return entradas.stream().sorted(ORDEM).map(Entrada::combatente).toList();
	}
}
