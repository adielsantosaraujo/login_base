package com.example.loginbase.jogo.item;

import java.util.Map;

import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.recurso.Recurso;

public interface ItemCatalogado {

	ItemSubtipo subtipo();

	TipoConstrucao oficina();

	Map<Recurso, Integer> receitaBase();

	default Map<Recurso, Integer> receita(int nivel) {
		return RegrasFabricacao.custo(receitaBase(), nivel);
	}
}
