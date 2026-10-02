package com.example.loginbase.jogo.quartel;

import java.math.BigDecimal;

public record MembroTropaDTO(Long cidadaoId, String nome, PosicaoTropa posicao, int idadeAnos, String estado,
		BigDecimal xpGuerreiro, int peGuerreiro) {
}
