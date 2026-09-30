package com.example.loginbase.jogo.catalogo;

import static com.example.loginbase.jogo.catalogo.TipoRecurso.COMIDA;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.FERRO;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.MADEIRA;

/**
 * Modelos de item (armas e armaduras), forjáveis na Forja com nível próprio
 * (L, 1 a 23) e status {@code DISPONIVEL}, {@code RESERVADO} ou
 * {@code EQUIPADO}. Origem: {@code FORJA} ou {@code MASMORRA}.
 */
public enum ModeloItem {

	ESPADA(CategoriaItem.ARMA, Custo.de(MADEIRA, 20, FERRO, 30), 60),
	LANCA(CategoriaItem.ARMA, Custo.de(MADEIRA, 40, FERRO, 20), 60),
	ARCO(CategoriaItem.ARMA, Custo.de(MADEIRA, 50, FERRO, 5), 60),
	ARMADURA_COURO(CategoriaItem.ARMADURA, Custo.de(COMIDA, 20, MADEIRA, 10, FERRO, 5), 45),
	ARMADURA_FERRO(CategoriaItem.ARMADURA, Custo.de(MADEIRA, 10, FERRO, 40), 90);

	public static final int NIVEL_MAXIMO = 23;
	public static final int QUANTIDADE_MAXIMA_ORDEM = 5;

	private final CategoriaItem categoria;
	private final Custo custoBasePorNivel;
	private final int tempoBaseSegundos;

	ModeloItem(CategoriaItem categoria, Custo custoBasePorNivel, int tempoBaseSegundos) {
		this.categoria = categoria;
		this.custoBasePorNivel = custoBasePorNivel;
		this.tempoBaseSegundos = tempoBaseSegundos;
	}

	public CategoriaItem categoria() {
		return categoria;
	}

	/** Atributos (ataque/defesa/alcance) do item no nível informado. */
	public AtributosItem atributos(int nivel) {
		validarNivel(nivel);
		return switch (this) {
			case ESPADA -> new AtributosItem(6 + 2 * (nivel - 1), 0, 1);
			case LANCA -> new AtributosItem(5 + 2 * (nivel - 1), 0, 1);
			case ARCO -> new AtributosItem(4 + 2 * (nivel - 1), 0, 3);
			case ARMADURA_COURO -> new AtributosItem(0, 2 + (nivel - 1), 0);
			case ARMADURA_FERRO -> new AtributosItem(0, 3 + 2 * (nivel - 1), 0);
		};
	}

	/** Custo total de uma ordem de forja: {@code custoBase × nível × quantidade}. */
	public Custo custoTotal(int nivel, int quantidade) {
		validarNivel(nivel);
		validarQuantidade(quantidade);
		return custoBasePorNivel.multiplicar((long) nivel * quantidade);
	}

	/**
	 * Tempo total de uma ordem de forja, em segundos:
	 * {@code ceil(tempoBase × nível × quantidade / velocidade)}.
	 */
	public long tempoTotalSegundos(int nivel, int quantidade, int velocidade) {
		validarNivel(nivel);
		validarQuantidade(quantidade);
		if (velocidade < 1) {
			throw new IllegalArgumentException("Velocidade inválida: " + velocidade);
		}
		long numerador = (long) tempoBaseSegundos * nivel * quantidade;
		return (numerador + velocidade - 1) / velocidade;
	}

	private void validarNivel(int nivel) {
		if (nivel < 1 || nivel > NIVEL_MAXIMO) {
			throw new IllegalArgumentException("Nível de item inválido: " + nivel);
		}
	}

	private void validarQuantidade(int quantidade) {
		if (quantidade < 1 || quantidade > QUANTIDADE_MAXIMA_ORDEM) {
			throw new IllegalArgumentException("Quantidade de ordem inválida: " + quantidade);
		}
	}

	/** Ataque, defesa e alcance calculados de um {@link ModeloItem} em um nível. */
	public record AtributosItem(int ataque, int defesa, int alcance) {
	}

}
