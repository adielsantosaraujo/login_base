package com.example.loginbase.jogo.dto;

import java.util.List;
import java.util.UUID;

/** Corpo do POST /api/jogo/vila: prévia vigente e os 3 índices (1 a 16) das regiões iniciais. */
public record CriarVilaRequest(UUID previaId, List<Integer> indices) {
}
