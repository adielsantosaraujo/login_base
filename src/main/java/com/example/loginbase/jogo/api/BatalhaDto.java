package com.example.loginbase.jogo.api;

import java.util.List;

import com.example.loginbase.jogo.dominio.StatusBatalha;
import com.example.loginbase.jogo.masmorra.combate.Lado;

/**
 * Snapshot completo de uma batalha de masmorra, retornado por
 * {@code POST /api/jogo/masmorras/{nivel}/batalhas} (201),
 * {@code GET /api/jogo/batalhas/{id}} e {@code POST /api/jogo/batalhas/{id}/acoes}
 * (ver spec game-dungeon-combat e design.md, seções 10 e 17). Espelha
 * {@code BatalhaDto} de {@code frontend/src/api/tipos.ts}.
 */
public record BatalhaDto(
		long id,
		int masmorraNivel,
		StatusBatalha status,
		int turno,
		int turnoMaximo,
		int largura,
		int altura,
		List<ObstaculoDto> obstaculos,
		List<CombatenteDto> combatentes,
		List<String> log,
		LootDto loot) {

	public record ObstaculoDto(int x, int y) {
	}

	/**
	 * Combatente (jogador ou inimigo) no estado tático. {@code unidadeId} é
	 * {@code null} para inimigos; {@code tipo} é o nome do {@code TipoTropa}
	 * (jogador) ou {@code TipoInimigo} (inimigo), já "limpo" do prefixo
	 * {@code "TIPO:idDaUnidade"} usado internamente por
	 * {@code Combatente.tipoOuUnidade()} para combatentes do jogador.
	 */
	public record CombatenteDto(
			String id,
			Lado lado,
			Long unidadeId,
			String tipo,
			int x,
			int y,
			int hp,
			int hpMaximo,
			int ataque,
			int defesa,
			int alcance,
			int movimento,
			boolean defendendo,
			boolean moveu,
			boolean agiu,
			boolean vivo) {
	}

}
