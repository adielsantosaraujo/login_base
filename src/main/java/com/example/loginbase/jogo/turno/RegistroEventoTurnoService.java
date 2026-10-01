package com.example.loginbase.jogo.turno;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Vila;

@Service
public class RegistroEventoTurnoService {

	private final EventoTurnoRepository repository;

	public RegistroEventoTurnoService(EventoTurnoRepository repository) {
		this.repository = repository;
	}

	/** Registra o evento na transação corrente (a da vila em processamento). */
	@Transactional
	public EventoTurno registrar(Vila vila, int turno, TipoEventoTurno tipo, String mensagem,
			Map<String, Object> dados) {
		return repository.save(new EventoTurno(vila.getId(), turno, tipo, mensagem, dados));
	}

	/** Registra o evento em transação própria, independente da transação chamadora. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public EventoTurno registrarEmNovaTransacao(Long vilaId, int turno, TipoEventoTurno tipo, String mensagem,
			Map<String, Object> dados) {
		return repository.save(new EventoTurno(vilaId, turno, tipo, mensagem, dados));
	}

}
