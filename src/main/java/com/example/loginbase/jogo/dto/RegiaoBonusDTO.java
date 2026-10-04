package com.example.loginbase.jogo.dto;

import com.example.loginbase.jogo.modelo.BonusRegiao;

/** Bônus de uma região: tipo, posição (1 a 3) e valor em %. */
public record RegiaoBonusDTO(BonusRegiao bonus, int posicao, int valor) {
}
