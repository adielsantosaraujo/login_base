package com.example.loginbase.jogo.servico;

import java.util.EnumSet;
import java.util.Set;

import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.TipoTerreno;

/** Grupos de prédios cujo bônus é a média das âncoras no terreno do grupo. */
public enum GrupoBonusVila {

	COMERCIO(TipoTerreno.COMERCIO, TipoConstrucao.MERCADO, TipoConstrucao.ESTALAGEM),
	DESENVOLVIMENTO(TipoTerreno.DESENVOLVIMENTO, TipoConstrucao.CASA),
	MILITAR(TipoTerreno.MILITAR, TipoConstrucao.QUARTEL);

	private final TipoTerreno terreno;
	private final Set<TipoConstrucao> tipos;

	GrupoBonusVila(TipoTerreno terreno, TipoConstrucao primeiro, TipoConstrucao... demais) {
		this.terreno = terreno;
		this.tipos = EnumSet.of(primeiro, demais);
	}

	public TipoTerreno terreno() {
		return terreno;
	}

	public Set<TipoConstrucao> tipos() {
		return tipos;
	}

}
