package com.example.loginbase.jogo.turno;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.repositorio.VilaRepository;

/**
 * Percorre as vilas com população confirmada (as pendentes são ignoradas) e processa cada uma em transação própria (REQUIRES_NEW). Atenção: o
 * agendador roda tudo numa transação externa, mas o resultado de cada vila é confirmado
 * (commit) de forma independente. Falha em uma vila gera o evento FALHA_PROCESSAMENTO e as
 * demais continuam.
 */
@Component
public class TurnoProcessorPorVila implements TurnoProcessor {

	private static final Logger log = LoggerFactory.getLogger(TurnoProcessorPorVila.class);

	private final VilaRepository vilaRepository;
	private final ProcessadorTurnoVila processadorVila;
	private final RegistroEventoTurnoService registroEventos;

	public TurnoProcessorPorVila(VilaRepository vilaRepository, ProcessadorTurnoVila processadorVila,
			RegistroEventoTurnoService registroEventos) {
		this.vilaRepository = vilaRepository;
		this.processadorVila = processadorVila;
		this.registroEventos = registroEventos;
	}

	@Override
	public void processarTurno(int numero) {
		for (Long vilaId : vilaRepository.findIdsComPopulacaoConfirmada()) {
			try {
				processadorVila.processarVila(vilaId, numero);
			} catch (RuntimeException e) {
				log.error("Falha ao processar a vila {} no turno {}", vilaId, numero, e);
				try {
					String msg = String.valueOf(e.getMessage());
					registroEventos.registrarEmNovaTransacao(vilaId, numero, TipoEventoTurno.FALHA_PROCESSAMENTO,
							msg.length() > 500 ? msg.substring(0, 500) : msg,
							Map.of("excecao", e.getClass().getName()));
				} catch (RuntimeException e2) {
					log.error("Não foi possível registrar a falha da vila {} no turno {}", vilaId, numero, e2);
				}
			}
		}
	}

}
