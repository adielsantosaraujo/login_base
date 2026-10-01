package com.example.loginbase.jogo.turno;

import java.util.List;

public record RelatorioTurnoDTO(int turno, List<EventoTurnoDTO> eventos) {
}
