package com.example.loginbase.jogo.item;

import java.util.List;

public record OficinaDTO(Long id, String tipo, String nivel, String estado, int nivelMaximoItem,
		List<ArtesaoDTO> artesaos) {
}
