package com.example.loginbase.jogo.construcao;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.TipoRegiao;

/** Dados do catálogo expostos ao front. */
public record CatalogoConstrucaoDTO(TipoConstrucao tipo, String nome, List<TipoRegiao> regioes, TipoTerreno terreno, Map<String, Integer> custoN1,
		int tamanho, int poN1, List<Profissao> profissoes) {
}
