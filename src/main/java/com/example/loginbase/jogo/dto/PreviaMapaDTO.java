package com.example.loginbase.jogo.dto;

import java.util.List;
import java.util.UUID;

/** Prévia vigente do mapa: identificador, rodada e as 16 regiões. */
public record PreviaMapaDTO(UUID previaId, int rodada, List<RegiaoPreviaDTO> regioes) {
}
