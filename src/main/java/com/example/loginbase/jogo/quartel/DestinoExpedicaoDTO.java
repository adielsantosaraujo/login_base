package com.example.loginbase.jogo.quartel;

import java.math.BigDecimal;

/** Masmorra ativa como destino possível de uma tropa, com custo de viagem. */
public record DestinoExpedicaoDTO(long masmorraId, int regiao, int nivel, int turnosViagem, int comidaNecessaria,
		BigDecimal comidaDisponivel) {
}
