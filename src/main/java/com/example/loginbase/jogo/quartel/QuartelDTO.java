package com.example.loginbase.jogo.quartel;

import java.util.List;

public record QuartelDTO(Long id, String nivel, String estado, int instrutores, int vagasInstrutor, int capacidade,
		int membrosAtuais, int maxTropas, List<TropaDTO> tropas) {
}
