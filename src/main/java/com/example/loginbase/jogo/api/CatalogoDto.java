package com.example.loginbase.jogo.api;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.api.VilaDto.CustoDto;
import com.example.loginbase.jogo.api.VilaDto.ProximoNivelDto;
import com.example.loginbase.jogo.catalogo.CategoriaItem;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoInimigo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoTropa;

/**
 * Regras estáticas do jogo, retornadas por {@code GET /api/jogo/catalogo} (ver
 * design.md, seção 17, e spec game-village — Catálogo de regras). Espelha (na
 * medida do compatível com os nomes reais do backend) {@code CatalogoDto} de
 * {@code frontend/src/api/tipos.ts}; o campo {@code inimigos} é adicional (não
 * previsto em {@code tipos.ts} hoje), exigido pela spec game-village.
 */
public record CatalogoDto(
		Map<TipoPredio, List<ProximoNivelDto>> predios,
		Map<Cultivo, CultivoCatalogoDto> cultivos,
		Map<ModeloItem, ModeloItemCatalogoDto> modelosItem,
		Map<TipoTropa, TropaCatalogoDto> tropas,
		Map<TipoInimigo, InimigoCatalogoDto> inimigos,
		Map<Integer, MasmorraCatalogoDto> masmorras) {

	public record CultivoCatalogoDto(long comidaPorHora, boolean exigeSemente, int nivelMasmorraParaSemente) {
	}

	/**
	 * @param custoBase       custo de forjar 1 unidade no nível 1 (custo por unidade no nível L = {@code custoBase × L})
	 * @param tempoBaseSegundos tempo-base de forja (tempo total = {@code ceil(tempoBase × L × quantidade / velocidade)})
	 */
	public record ModeloItemCatalogoDto(CategoriaItem categoria, List<CustoDto> custoBase, long tempoBaseSegundos) {
	}

	public record TropaCatalogoDto(
			ModeloItem armaExigida,
			int hp,
			int defesaBase,
			int movimento,
			long comida,
			int tempoTreinoSegundos,
			int nivelMinimoQuartel) {
	}

	public record InimigoCatalogoDto(int hp, int ataque, int defesa, int alcance, int movimento) {
	}

	public record PosicaoDto(int x, int y) {
	}

	public record MapaMasmorraDto(
			int largura,
			int altura,
			List<PosicaoDto> obstaculos,
			List<PosicaoDto> posicoesJogador,
			Map<String, PosicaoDto> spawnsInimigos) {
	}

	public record MasmorraCatalogoDto(int nivel, MapaMasmorraDto mapa, List<TipoInimigo> composicaoInimigos) {
	}

}
