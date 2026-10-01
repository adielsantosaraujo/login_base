package com.example.loginbase.jogo.turno;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.JogoTurno;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;

@Service
@Transactional(readOnly = true)
public class TurnoService {

	private final JogoTurnoRepository jogoTurnoRepository;
	private final EventoTurnoRepository eventoTurnoRepository;
	private final JogoTurnoProperties properties;

	public TurnoService(JogoTurnoRepository jogoTurnoRepository, EventoTurnoRepository eventoTurnoRepository,
			JogoTurnoProperties properties) {
		this.jogoTurnoRepository = jogoTurnoRepository;
		this.eventoTurnoRepository = eventoTurnoRepository;
		this.properties = properties;
	}

	public TurnoDTO getTurnoAtual() {
		return getTurnoAtual(Instant.now());
	}

	TurnoDTO getTurnoAtual(Instant agora) {
		int numero = jogoTurnoRepository.maiorNumero();
		JogoTurno turno = numero == 0 ? null : jogoTurnoRepository.findByNumero(numero).orElse(null);
		if (turno == null) {
			return new TurnoDTO(0, null, null, 0);
		}
		Instant proximoEm = turno.getIniciadoEm().plus(Duration.ofMinutes(properties.getIntervaloMinutos()));
		long restantes = Math.max(0, Duration.between(agora, proximoEm).getSeconds());
		return new TurnoDTO(turno.getNumero(), turno.getIniciadoEm(), proximoEm, restantes);
	}

	/**
	 * Eventos da vila. Sem filtro: último turno que gerou eventos para a vila (ou o
	 * turno atual, com lista vazia, se a vila ainda não tem eventos).
	 */
	public RelatorioTurnoDTO getEventos(Vila vila, Integer filtroTurno) {
		int turno = filtroTurno != null ? filtroTurno
				: eventoTurnoRepository.maiorTurnoDaVila(vila.getId()).orElseGet(jogoTurnoRepository::maiorNumero);
		List<EventoTurnoDTO> eventos = eventoTurnoRepository.findByVilaIdAndTurnoOrderByIdAsc(vila.getId(), turno)
				.stream().map(EventoTurnoDTO::de).toList();
		return new RelatorioTurnoDTO(turno, eventos);
	}

}
