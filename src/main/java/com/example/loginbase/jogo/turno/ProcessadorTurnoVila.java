package com.example.loginbase.jogo.turno;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

/** Resolve um turno para uma única vila, em transação própria e de forma idempotente. */
@Service
public class ProcessadorTurnoVila {

	private final VilaRepository vilaRepository;
	private final List<EtapaTurno> etapas;

	public ProcessadorTurnoVila(VilaRepository vilaRepository, List<EtapaTurno> etapas) {
		this.vilaRepository = vilaRepository;
		this.etapas = etapas.stream().sorted(Comparator.comparingInt(EtapaTurno::ordem)).toList();
	}

	/**
	 * @return {@code true} se processou; {@code false} se a vila já havia sido processada neste turno.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean processarVila(Long vilaId, int numeroTurno) {
		Vila vila = vilaRepository.findById(vilaId).orElseThrow();
		if (vila.getTurnoProcessado() != null && vila.getTurnoProcessado() >= numeroTurno) {
			return false;
		}
		for (EtapaTurno etapa : etapas) {
			etapa.executar(vila, numeroTurno);
		}
		vila.setTurnoProcessado(numeroTurno);
		vilaRepository.saveAndFlush(vila);
		return true;
	}

	List<EtapaTurno> etapas() {
		return etapas;
	}

}
