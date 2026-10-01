package com.example.loginbase.jogo.comercio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

import com.example.loginbase.jogo.recurso.Recurso;

/** Preços base (tabela 3.1 do comércio) e fórmulas de venda/compra. O Ouro não é negociável. */
public final class CatalogoPrecos {

	private static final Map<Recurso, BigDecimal> BASE = new EnumMap<>(Recurso.class);

	static {
		BASE.put(Recurso.MADEIRA, bd(1));
		BASE.put(Recurso.PEDRA, bd(1));
		BASE.put(Recurso.ARGILA, bd(1));
		BASE.put(Recurso.MINERIO_DE_FERRO, bd(2));
		BASE.put(Recurso.CARVAO, bd(2));
		BASE.put(Recurso.SAL, bd(3));
		BASE.put(Recurso.ENXOFRE, bd(4));
		BASE.put(Recurso.GRAOS, bd(1));
		BASE.put(Recurso.FIBRA, bd(1));
		BASE.put(Recurso.CARNE, bd(2));
		BASE.put(Recurso.COURO, bd(2));
		BASE.put(Recurso.LA, bd(2));
		BASE.put(Recurso.TABUA, bd(3));
		BASE.put(Recurso.TIJOLO, bd(3));
		BASE.put(Recurso.FERRO, bd(6));
		BASE.put(Recurso.ACO, bd(20));
		BASE.put(Recurso.TECIDO, bd(4));
		BASE.put(Recurso.COURO_CURTIDO, bd(6));
		BASE.put(Recurso.REFEICAO, bd(1));
	}

	private CatalogoPrecos() {
	}

	private static BigDecimal bd(int v) {
		return BigDecimal.valueOf(v);
	}

	public static boolean negociavel(Recurso recurso) {
		return BASE.containsKey(recurso);
	}

	public static BigDecimal precoBase(Recurso recurso) {
		BigDecimal p = BASE.get(recurso);
		if (p == null) {
			throw new IllegalArgumentException("Recurso não negociável: " + recurso);
		}
		return p;
	}

	/** Venda ao NPC: base x min(1,0; 0,5 + 0,02 x PE), 2 casas. */
	public static BigDecimal precoVenda(Recurso recurso, int pe) {
		BigDecimal fator = new BigDecimal("0.5").add(new BigDecimal("0.02").multiply(bd(pe))).min(BigDecimal.ONE);
		return precoBase(recurso).multiply(fator).setScale(2, RoundingMode.HALF_UP);
	}

	/** Compra do NPC: base x max(1,0; 1,5 - 0,02 x PE), 2 casas. */
	public static BigDecimal precoCompra(Recurso recurso, int pe) {
		BigDecimal fator = new BigDecimal("1.5").subtract(new BigDecimal("0.02").multiply(bd(pe))).max(BigDecimal.ONE);
		return precoBase(recurso).multiply(fator).setScale(2, RoundingMode.HALF_UP);
	}

}
