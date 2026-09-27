package com.example.loginbase.jogo.economia;

import java.util.EnumMap;
import java.util.Map;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.TipoRecurso;

/**
 * Estoque imutável de recursos de uma vila, guardado em milésimos
 * ({@code long}, 1 unidade = 1000) para preservar produção fracionária sem
 * perda de precisão. Recursos ausentes do mapa equivalem a zero.
 */
public record Estoque(Map<TipoRecurso, Long> milesimos) {

	public Estoque {
		milesimos.values().forEach(valor -> {
			if (valor < 0) {
				throw new IllegalArgumentException("Estoque não pode ter valor negativo: " + valor);
			}
		});
		milesimos = Map.copyOf(milesimos);
	}

	/** Estoque vazio (todos os recursos em zero). */
	public static Estoque vazio() {
		return new Estoque(Map.of());
	}

	/** Quantidade do recurso, em milésimos (0 se ausente do mapa). */
	public long get(TipoRecurso recurso) {
		return milesimos.getOrDefault(recurso, 0L);
	}

	/** Verdadeiro se o estoque tem ao menos {@code quantidadeMilesimos} do recurso informado. */
	public boolean tem(TipoRecurso recurso, long quantidadeMilesimos) {
		return get(recurso) >= quantidadeMilesimos;
	}

	/** Novo estoque com {@code quantidadeMilesimos} somados ao recurso informado. */
	public Estoque adicionar(TipoRecurso recurso, long quantidadeMilesimos) {
		if (quantidadeMilesimos < 0) {
			throw new IllegalArgumentException(
					"Quantidade a adicionar não pode ser negativa: " + quantidadeMilesimos);
		}
		Map<TipoRecurso, Long> novo = new EnumMap<>(TipoRecurso.class);
		novo.putAll(milesimos);
		novo.merge(recurso, quantidadeMilesimos, Long::sum);
		return new Estoque(novo);
	}

	/**
	 * Novo estoque com {@code quantidadeMilesimos} subtraídos do recurso informado.
	 *
	 * @throws RegraJogoException com {@link CodigoErro#RECURSOS_INSUFICIENTES} se o estoque não
	 *                            tiver saldo suficiente do recurso
	 */
	public Estoque subtrair(TipoRecurso recurso, long quantidadeMilesimos) {
		if (quantidadeMilesimos < 0) {
			throw new IllegalArgumentException(
					"Quantidade a subtrair não pode ser negativa: " + quantidadeMilesimos);
		}
		if (!tem(recurso, quantidadeMilesimos)) {
			throw new RegraJogoException(CodigoErro.RECURSOS_INSUFICIENTES,
					"Estoque insuficiente de " + recurso + " para subtrair " + quantidadeMilesimos + " milésimos");
		}
		Map<TipoRecurso, Long> novo = new EnumMap<>(TipoRecurso.class);
		novo.putAll(milesimos);
		novo.merge(recurso, -quantidadeMilesimos, Long::sum);
		return new Estoque(novo);
	}

}
