package com.example.loginbase.jogo.item;

import java.math.BigDecimal;

public record FabricacaoDTO(Long id, String subtipo, int nivel, Long artesaoId, String artesaoNome,
		BigDecimal pfAtual, int pfTotal, String estado, Integer turnosEstimados, Long itemId) {
}
