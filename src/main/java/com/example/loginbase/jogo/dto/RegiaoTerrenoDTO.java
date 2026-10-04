package com.example.loginbase.jogo.dto;

import com.example.loginbase.jogo.modelo.TipoTerreno;

/** Terreno de uma região com sua posição (1 a 3) e percentual de ladrilhos. */
public record RegiaoTerrenoDTO(TipoTerreno terreno, int posicao, int percentual) {
}
