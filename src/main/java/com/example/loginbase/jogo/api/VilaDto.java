package com.example.loginbase.jogo.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.SlotEquipamento;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;

/**
 * Estado completo da vila do usuário, retornado por {@code GET /api/jogo/vila}
 * (ver design.md, seção 17, e spec game-village — Consulta do estado da
 * vila). Espelha (na medida do compatível com os nomes reais do backend)
 * {@code VilaDto} de {@code frontend/src/api/tipos.ts}.
 */
public record VilaDto(
		Instant agora,
		String nome,
		Map<TipoRecurso, Long> recursos,
		Map<TipoRecurso, Long> capacidade,
		Map<TipoRecurso, Long> producaoPorHora,
		int masmorraNivelLiberado,
		Long batalhaAtivaId,
		List<PredioDto> predios,
		List<CanteiroDto> canteiros,
		Map<Cultivo, Integer> sementes,
		List<ItemDto> itens,
		List<UnidadeDto> unidades,
		int capacidadeExercito,
		int nivelMaximoForjavel,
		List<OrdemDto> ordens) {

	/** Custo em uma unidade de recurso (não milésimos). */
	public record CustoDto(TipoRecurso recurso, long quantidade) {
	}

	/** Próximo nível de um prédio (ausente quando já está no nível máximo). */
	public record ProximoNivelDto(int nivel, List<CustoDto> custo, long tempoSegundos) {
	}

	/** Ordem de construção em andamento para um prédio. */
	public record OrdemEmAndamentoDto(Instant concluiEm, long tempoRestanteSegundos) {
	}

	public record PredioDto(
			TipoPredio tipo,
			int nivel,
			int nivelMaximo,
			ProximoNivelDto proximoNivel,
			OrdemEmAndamentoDto ordemEmAndamento) {
	}

	public record CanteiroDto(int posicao, Cultivo cultivo, long producaoComidaPorHora) {
	}

	public record ItemDto(
			long id,
			ModeloItem modelo,
			int nivel,
			OrigemItem origem,
			StatusItem status,
			int ataque,
			int defesa,
			Integer alcance) {
	}

	public record UnidadeDto(
			long id,
			String nome,
			String sobrenome,
			int ordinalNome,
			String nomeExibicao,
			TipoTropa tipo,
			StatusUnidade status,
			int hp,
			int ataque,
			int defesa,
			int alcance,
			int movimento,
			LinkedHashMap<SlotEquipamento, ItemDto> equipamento) {
	}

	public record OrdemDto(
			long id,
			CategoriaOrdem categoria,
			String alvo,
			Integer nivel,
			int quantidade,
			Instant iniciadaEm,
			Instant concluiEm) {
	}

}
