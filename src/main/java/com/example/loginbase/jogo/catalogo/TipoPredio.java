package com.example.loginbase.jogo.catalogo;

import static com.example.loginbase.jogo.catalogo.TipoRecurso.FERRO;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.MADEIRA;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.PEDRA;

/**
 * Tipos de prédio da vila (um de cada tipo por vila, níveis 0 a 100; nível 0 =
 * não construído). Fórmulas em duas faixas:
 * <ul>
 * <li>N ≤ 5: custo {@code round_half_up(custoBase × 1,5^(N-1))}; tempo
 * {@code tempoBase × 2^(N-1)} segundos;</li>
 * <li>N &gt; 5: custo {@code round_half_up(custoBase × 5,0625 × (N/5)^p)} (ver
 * {@link CurvaNiveis}); tempo {@code (tempoBase × 16 × N + 4) / 5} segundos (linear).</li>
 * </ul>
 */
public enum TipoPredio {

	CENTRO_VILA(Custo.de(MADEIRA, 150, PEDRA, 150), 120),
	ARMAZEM(Custo.de(MADEIRA, 100, PEDRA, 60), 60),
	FAZENDA(Custo.de(MADEIRA, 80, PEDRA, 40), 60),
	SERRARIA(Custo.de(MADEIRA, 60, PEDRA, 40), 60),
	PEDREIRA(Custo.de(MADEIRA, 80, PEDRA, 20), 60),
	MINA_FERRO(Custo.de(MADEIRA, 100, PEDRA, 80), 90),
	FORJA(Custo.de(MADEIRA, 120, PEDRA, 100, FERRO, 40), 120),
	QUARTEL(Custo.de(MADEIRA, 150, PEDRA, 120, FERRO, 40), 120);

	public static final int NIVEL_MAXIMO = 100;

	private final Custo custoBase;
	private final int tempoBaseSegundos;

	TipoPredio(Custo custoBase, int tempoBaseSegundos) {
		this.custoBase = custoBase;
		this.tempoBaseSegundos = tempoBaseSegundos;
	}

	public static final int NIVEL_FIM_FAIXA_INICIAL = 5;

	public static final int NUMERO_MAXIMO_CANTEIROS = 24;

	public Custo custo(int nivel, CurvaNiveis curva) {
		validarNivel(nivel);
		return custoBase.paraNivel(nivel, curva);
	}

	public long tempoSegundos(int nivel) {
		validarNivel(nivel);
		if (nivel <= NIVEL_FIM_FAIXA_INICIAL) {
			return (long) tempoBaseSegundos << (nivel - 1);
		}
		return (tempoBaseSegundos * 16L * nivel + 4) / 5;
	}

	/** ARMAZEM: capacidade por recurso no nível informado. */
	public long capacidadeRecurso(int nivel, CurvaNiveis curva) {
		exigirTipo(ARMAZEM);
		validarNivel(nivel);
		return TipoRecurso.capacidadeArmazem(nivel, curva);
	}

	/** SERRARIA/PEDREIRA/MINA_FERRO: produção adicional por hora no nível informado. */
	public long producaoAdicionalPorHora(int nivel) {
		validarNivel(nivel);
		return switch (this) {
			case SERRARIA -> 30L * nivel;
			case PEDREIRA -> 20L * nivel;
			case MINA_FERRO -> 10L * nivel;
			default -> throw new IllegalStateException(this + " não produz recursos");
		};
	}

	/** FAZENDA: número de canteiros no nível informado. */
	public int numeroCanteiros(int nivel) {
		exigirTipo(FAZENDA);
		validarNivel(nivel);
		return nivel <= 5 ? nivel : 5 + (nivel - 5) / 5;
	}

	/** FORJA: nível máximo de item forjável no nível informado. */
	public int nivelMaximoForjavel(int nivel) {
		exigirTipo(FORJA);
		validarNivel(nivel);
		if (nivel <= 10) {
			return nivel;
		}
		return nivel <= 50 ? 10 + (nivel - 10) / 5 : 18 + (nivel - 50) / 10;
	}

	/** QUARTEL: capacidade do exército (unidades + em treino) no nível informado. */
	public int capacidadeExercito(int nivel) {
		exigirTipo(QUARTEL);
		validarNivel(nivel);
		return 3 * nivel;
	}

	private void exigirTipo(TipoPredio esperado) {
		if (this != esperado) {
			throw new IllegalStateException(this + " não é " + esperado);
		}
	}

	private void validarNivel(int nivel) {
		if (nivel < 1 || nivel > NIVEL_MAXIMO) {
			throw new IllegalArgumentException("Nível de prédio inválido: " + nivel);
		}
	}

}
