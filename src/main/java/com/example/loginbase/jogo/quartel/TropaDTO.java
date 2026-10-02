package com.example.loginbase.jogo.quartel;

import java.util.List;

public record TropaDTO(Long id, String nome, String estado, Long quartelId, Long masmorraId, Integer regiaoDestino,
		Integer turnosViagem, Integer turnosRestantes, int totalMembros, List<MembroTropaDTO> membros) {
}
