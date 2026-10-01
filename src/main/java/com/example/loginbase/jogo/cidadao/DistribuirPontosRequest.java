package com.example.loginbase.jogo.cidadao;

import java.util.Map;

/** Chaves: nomes de Caracteristica (VIT, FOR, VEL, INT, CAR) e de Profissao (CONSTRUTOR...), sem diferenciar maiúsculas. */
public record DistribuirPontosRequest(Map<String, Integer> caracteristicas, Map<String, Integer> profissoes) {
}
