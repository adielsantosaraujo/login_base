package com.example.loginbase.jogo.item.catalogo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemCatalogado;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.recurso.Recurso;

/**
 * Catálogo de joias (colar e anel). O efeito principal é derivado do nível, não persistido.
 */
public enum JoiaCatalogo implements ItemCatalogado {
	COLAR(ItemSubtipo.COLAR, Map.of(Recurso.FERRO, 1, Recurso.OURO, 20)),
	ANEL(ItemSubtipo.ANEL, Map.of(Recurso.FERRO, 1, Recurso.OURO, 15));

	private static final Set<Caracteristica> CARACTERISTICAS_PERMITIDAS = Set.of(
			Caracteristica.VIT, Caracteristica.FOR, Caracteristica.VEL, Caracteristica.INT, Caracteristica.CAR);

	private final ItemSubtipo subtipo;
	private final Map<Recurso, Integer> receitaBase;

	JoiaCatalogo(ItemSubtipo subtipo, Map<Recurso, Integer> receitaBase) {
		this.subtipo = subtipo;
		this.receitaBase = receitaBase;
	}

	@Override
	public ItemSubtipo subtipo() {
		return subtipo;
	}

	@Override
	public TipoConstrucao oficina() {
		return TipoConstrucao.FERRARIA;
	}

	@Override
	public Map<Recurso, Integer> receitaBase() {
		return receitaBase;
	}

	public boolean exigeAtributo() {
		return this == ANEL;
	}

	public static int vidaColar(int nivel) {
		return 5 * nivel;
	}

	public static int bonusAnel(int nivel) {
		if (nivel <= 3) {
			return 1;
		}
		if (nivel <= 6) {
			return 2;
		}
		if (nivel <= 9) {
			return 3;
		}
		return 4;
	}

	public static boolean caracteristicaPermitida(Caracteristica caracteristica) {
		return caracteristica != null && CARACTERISTICAS_PERMITIDAS.contains(caracteristica);
	}

	public static Optional<JoiaCatalogo> de(ItemSubtipo subtipo) {
		for (JoiaCatalogo j : values()) {
			if (j.subtipo == subtipo) {
				return Optional.of(j);
			}
		}
		return Optional.empty();
	}
}
