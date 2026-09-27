package com.example.loginbase.jogo.api;

import com.example.loginbase.jogo.catalogo.CategoriaItem;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusItem;

/**
 * Item completo (arma ou armadura) exposto em {@link LootDto#itens()}, com
 * seus atributos derivados já calculados (ver {@link ModeloItem#atributos(int)}).
 * Distinto de {@link VilaDto.ItemDto}: este é usado apenas no contexto de
 * loot de masmorra (ver design.md, seção 17, e task 6.3).
 *
 * <p>
 * O contrato do frontend ({@code frontend/src/api/tipos.ts}, {@code LootDto})
 * só lê {@code modelo}/{@code nivel} de cada item do loot; os demais campos
 * aqui são adicionais (harmless) para consumidores futuros.
 */
public record ItemDto(
		long id,
		ModeloItem modelo,
		CategoriaItem categoria,
		int nivel,
		int ataque,
		int defesa,
		int alcance,
		OrigemItem origem,
		StatusItem status) {
}
