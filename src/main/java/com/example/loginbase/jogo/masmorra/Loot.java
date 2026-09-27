package com.example.loginbase.jogo.masmorra;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.dominio.Item;

/**
 * Loot resultante da vitória em um nível de masmorra (design.md §11):
 * recursos garantidos mais extras de rolagens, sementes sorteadas e itens
 * forjáveis sorteados.
 *
 * <p>
 * Os valores de {@link #recursos()} estão em milésimos ({@code long}, 1
 * unidade = 1000, mesma convenção de {@link TipoRecurso}) — dividir por 1000
 * antes de exibir ou aplicar ao estoque.
 *
 * <p>
 * Record imutável: as coleções recebidas são copiadas defensivamente no
 * construtor.
 */
public record Loot(Map<TipoRecurso, Long> recursos, Map<Cultivo, Integer> sementes, List<Item> itens) {

	public Loot {
		recursos = Map.copyOf(recursos);
		sementes = Map.copyOf(sementes);
		itens = List.copyOf(itens);
	}

}
