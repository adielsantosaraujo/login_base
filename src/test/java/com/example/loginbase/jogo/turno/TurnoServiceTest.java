package com.example.loginbase.jogo.turno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.modelo.JogoTurno;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;

class TurnoServiceTest {

	private final JogoTurnoRepository turnos = mock(JogoTurnoRepository.class);
	private final EventoTurnoRepository eventos = mock(EventoTurnoRepository.class);
	private final JogoTurnoProperties props = new JogoTurnoProperties();
	private final TurnoService service = new TurnoService(turnos, eventos, props);

	@Test
	void calculaContagemRegressiva() {
		Instant inicio = Instant.parse("2026-09-30T12:00:00Z");
		when(turnos.maiorNumero()).thenReturn(15);
		when(turnos.findByNumero(15)).thenReturn(Optional.of(new JogoTurno(15, inicio)));
		TurnoDTO dto = service.getTurnoAtual(inicio.plusSeconds(600));
		assertThat(dto.numero()).isEqualTo(15);
		assertThat(dto.proximoEm()).isEqualTo(inicio.plusSeconds(3600));
		assertThat(dto.segundosRestantes()).isEqualTo(3000);
	}

	@Test
	void atrasadoNaoFicaNegativo() {
		Instant inicio = Instant.parse("2026-09-30T12:00:00Z");
		when(turnos.maiorNumero()).thenReturn(1);
		when(turnos.findByNumero(1)).thenReturn(Optional.of(new JogoTurno(1, inicio)));
		assertThat(service.getTurnoAtual(inicio.plusSeconds(7200)).segundosRestantes()).isZero();
	}

	@Test
	void semTurnoDevolveZero() {
		when(turnos.maiorNumero()).thenReturn(0);
		TurnoDTO dto = service.getTurnoAtual(Instant.now());
		assertThat(dto.numero()).isZero();
		assertThat(dto.proximoEm()).isNull();
		assertThat(dto.segundosRestantes()).isZero();
	}

}
