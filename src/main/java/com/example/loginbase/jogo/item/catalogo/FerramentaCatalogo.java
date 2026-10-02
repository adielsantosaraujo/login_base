package com.example.loginbase.jogo.item.catalogo;

import static com.example.loginbase.jogo.construcao.TipoConstrucao.ALFAIATARIA;
import static com.example.loginbase.jogo.construcao.TipoConstrucao.CARPINTARIA;
import static com.example.loginbase.jogo.construcao.TipoConstrucao.FERRARIA;
import static com.example.loginbase.jogo.recurso.Recurso.COURO_CURTIDO;
import static com.example.loginbase.jogo.recurso.Recurso.FERRO;
import static com.example.loginbase.jogo.recurso.Recurso.TABUA;
import static com.example.loginbase.jogo.recurso.Recurso.TECIDO;

import java.util.Map;
import java.util.Optional;

import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemCatalogado;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.recurso.Recurso;

/** Catálogo das 11 ferramentas (tabela 7.9): profissão, oficina e receita base (x L). */
public enum FerramentaCatalogo implements ItemCatalogado {
	MARTELO(ItemSubtipo.MARTELO, Profissao.CONSTRUTOR, FERRARIA, Map.of(FERRO, 1, TABUA, 1)),
	CARRINHO_DE_MAO(ItemSubtipo.CARRINHO_DE_MAO, Profissao.CARREGADOR, CARPINTARIA, Map.of(TABUA, 3, FERRO, 1)),
	ENXADA(ItemSubtipo.ENXADA, Profissao.AGRICULTOR, FERRARIA, Map.of(FERRO, 1, TABUA, 1)),
	FORCADO(ItemSubtipo.FORCADO, Profissao.FAZENDEIRO, FERRARIA, Map.of(FERRO, 1, TABUA, 1)),
	PICARETA(ItemSubtipo.PICARETA, Profissao.MINEIRO, FERRARIA, Map.of(FERRO, 2, TABUA, 1)),
	MACHADO(ItemSubtipo.MACHADO, Profissao.MADEIREIRO, FERRARIA, Map.of(FERRO, 2, TABUA, 1)),
	MALHO(ItemSubtipo.MALHO, Profissao.FERREIRO, FERRARIA, Map.of(FERRO, 2)),
	CUTELO(ItemSubtipo.CUTELO, Profissao.COZINHEIRO, FERRARIA, Map.of(FERRO, 1)),
	KIT_DE_COSTURA(ItemSubtipo.KIT_DE_COSTURA, Profissao.COSTUREIRO, ALFAIATARIA, Map.of(FERRO, 1, TECIDO, 1)),
	FACA_DE_CACA(ItemSubtipo.FACA_DE_CACA, Profissao.CACADOR, FERRARIA, Map.of(FERRO, 1, COURO_CURTIDO, 1)),
	BALANCA(ItemSubtipo.BALANCA, Profissao.COMERCIANTE, CARPINTARIA, Map.of(TABUA, 2, FERRO, 1));

	private final ItemSubtipo subtipo;
	private final Profissao profissao;
	private final TipoConstrucao oficina;
	private final Map<Recurso, Integer> receitaBase;

	FerramentaCatalogo(ItemSubtipo subtipo, Profissao profissao, TipoConstrucao oficina,
			Map<Recurso, Integer> receitaBase) {
		this.subtipo = subtipo;
		this.profissao = profissao;
		this.oficina = oficina;
		this.receitaBase = receitaBase;
	}

	@Override
	public ItemSubtipo subtipo() {
		return subtipo;
	}

	public Profissao profissao() {
		return profissao;
	}

	@Override
	public TipoConstrucao oficina() {
		return oficina;
	}

	@Override
	public Map<Recurso, Integer> receitaBase() {
		return receitaBase;
	}

	public static Optional<FerramentaCatalogo> de(ItemSubtipo subtipo) {
		for (FerramentaCatalogo f : values()) {
			if (f.subtipo == subtipo) {
				return Optional.of(f);
			}
		}
		return Optional.empty();
	}
}
