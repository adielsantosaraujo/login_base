package com.example.loginbase.jogo.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Grade 4x4 de regiões, com índices de 1 a 16 (linha a linha, de cima para baixo).
 */
public final class GradeRegioes {

	public static final int LADO = 4;
	public static final int TOTAL = LADO * LADO;

	private GradeRegioes() {
	}

	/** Linha (0-3) do índice. */
	public static int linha(int indice) {
		validar(indice);
		return (indice - 1) / LADO;
	}

	/** Coluna (0-3) do índice. */
	public static int coluna(int indice) {
		validar(indice);
		return (indice - 1) % LADO;
	}

	/** Índices dos vizinhos ortogonais (cima, baixo, esquerda, direita), em ordem crescente. */
	public static List<Integer> vizinhos(int indice) {
		int l = linha(indice);
		int c = coluna(indice);
		List<Integer> r = new ArrayList<>(4);
		if (l > 0) {
			r.add(indice - LADO);
		}
		if (c > 0) {
			r.add(indice - 1);
		}
		if (c < LADO - 1) {
			r.add(indice + 1);
		}
		if (l < LADO - 1) {
			r.add(indice + LADO);
		}
		return r;
	}

	/** Se as duas regiões são vizinhas ortogonais. */
	public static boolean adjacente(int a, int b) {
		validar(a);
		validar(b);
		return Math.abs(linha(a) - linha(b)) + Math.abs(coluna(a) - coluna(b)) == 1;
	}

	private static void validar(int indice) {
		if (indice < 1 || indice > TOTAL) {
			throw new IllegalArgumentException("Índice de região inválido: " + indice);
		}
	}

}
