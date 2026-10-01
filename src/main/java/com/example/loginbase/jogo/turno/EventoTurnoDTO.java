package com.example.loginbase.jogo.turno;

import java.time.Instant;
import java.util.Map;

public record EventoTurnoDTO(Long id, TipoEventoTurno tipo, String mensagem, Map<String, Object> dados,
		Instant timestamp) {

	static EventoTurnoDTO de(EventoTurno e) {
		return new EventoTurnoDTO(e.getId(), e.getTipo(), e.getMensagem(), e.getDados(), e.getCriadoEm());
	}

}
