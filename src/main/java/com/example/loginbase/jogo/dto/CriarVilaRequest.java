package com.example.loginbase.jogo.dto;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.modelo.TipoRegiao;

/** Corpo do POST /api/jogo/vila. A semente é opcional (vem da prévia). */
public record CriarVilaRequest(List<Integer> regioesEscolhidas, Map<Integer, TipoRegiao> tipos, Long semente) {
}
