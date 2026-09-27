package com.example.loginbase.jogo.catalogo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Custo em recursos, representado como um mapa {@link TipoRecurso} →
 * quantidade (unidades inteiras, não milésimos).
 */
public record Custo(Map<TipoRecurso, Long> valores) {

	public Custo {
		valores = Map.copyOf(valores);
	}

	public static Custo de(TipoRecurso recurso, long quantidade) {
		return new Custo(Map.of(recurso, quantidade));
	}

	public static Custo de(TipoRecurso recurso1, long quantidade1, TipoRecurso recurso2, long quantidade2) {
		Map<TipoRecurso, Long> mapa = new EnumMap<>(TipoRecurso.class);
		mapa.put(recurso1, quantidade1);
		mapa.put(recurso2, quantidade2);
		return new Custo(mapa);
	}

	public static Custo de(TipoRecurso recurso1, long quantidade1, TipoRecurso recurso2, long quantidade2,
			TipoRecurso recurso3, long quantidade3) {
		Map<TipoRecurso, Long> mapa = new EnumMap<>(TipoRecurso.class);
		mapa.put(recurso1, quantidade1);
		mapa.put(recurso2, quantidade2);
		mapa.put(recurso3, quantidade3);
		return new Custo(mapa);
	}

	public long quantidade(TipoRecurso recurso) {
		return valores.getOrDefault(recurso, 0L);
	}

	/**
	 * Custo para atingir o nível informado de um prédio:
	 * {@code round_half_up(base × 1,5^(nivel-1))} por recurso.
	 */
	public Custo paraNivel(int nivel) {
		BigDecimal fator = new BigDecimal("1.5").pow(nivel - 1);
		Map<TipoRecurso, Long> resultado = new EnumMap<>(TipoRecurso.class);
		valores.forEach((recurso, base) -> resultado.put(recurso,
				BigDecimal.valueOf(base).multiply(fator).setScale(0, RoundingMode.HALF_UP).longValueExact()));
		return new Custo(resultado);
	}

	/**
	 * Multiplica o custo por um fator inteiro (usado na Forja:
	 * {@code custoBase × nível × quantidade}).
	 */
	public Custo multiplicar(long fator) {
		Map<TipoRecurso, Long> resultado = new EnumMap<>(TipoRecurso.class);
		valores.forEach((recurso, base) -> resultado.put(recurso, base * fator));
		return new Custo(resultado);
	}

}
