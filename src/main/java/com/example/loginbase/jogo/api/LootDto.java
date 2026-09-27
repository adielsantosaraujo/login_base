package com.example.loginbase.jogo.api;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.TipoRecurso;

/**
 * Loot de uma batalha vencida (ver spec game-dungeon-loot e design.md, seção
 * 11): recursos garantidos + rolagens (já em unidades inteiras, não
 * milésimos), sementes sorteadas e itens forjáveis sorteados. Presente em
 * {@link BatalhaDto#loot()} apenas quando {@code status == VITORIA}.
 */
public record LootDto(
		Map<TipoRecurso, Long> recursos,
		Map<Cultivo, Integer> sementes,
		List<ItemDto> itens) {
}
