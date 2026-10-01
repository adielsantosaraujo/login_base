package com.example.loginbase.jogo.turno;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.loginbase.jogo.modelo.JogoTurno;
import com.example.loginbase.jogo.modelo.StatusTurno;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;

/**
 * Dispara o turno global a cada intervalo. A execução única entre instâncias é garantida por
 * {@code pg_try_advisory_xact_lock} mantido durante toda a transação do turno (inserção do
 * próximo {@code numero} como PROCESSANDO, processamento e marcação como CONCLUIDO).
 */
@Service
@ConditionalOnProperty(name = "jogo.turno.habilitado", havingValue = "true", matchIfMissing = true)
public class AgendadorTurnoService {

	static final long CHAVE_TRAVA_TURNO = 7_400_901L;

	private static final Logger log = LoggerFactory.getLogger(AgendadorTurnoService.class);

	private final JogoTurnoRepository turnoRepository;
	private final TurnoProcessor turnoProcessor;
	private final TransactionTemplate transacao;

	public AgendadorTurnoService(JogoTurnoRepository turnoRepository, TurnoProcessor turnoProcessor,
			PlatformTransactionManager transactionManager) {
		this.turnoRepository = turnoRepository;
		this.turnoProcessor = turnoProcessor;
		this.transacao = new TransactionTemplate(transactionManager);
	}

	@Scheduled(fixedDelayString = "${jogo.turno.intervalo-minutos:60}",
			initialDelayString = "${jogo.turno.intervalo-minutos:60}", timeUnit = TimeUnit.MINUTES)
	public void processarTurnoGlobal() {
		try {
			Optional<Integer> numero = executarTurno();
			if (numero.isPresent()) {
				log.info("Turno {} concluído", numero.get());
			} else {
				log.info("Turno global já em processamento por outra instância; execução ignorada");
			}
		} catch (RuntimeException e) {
			log.error("Falha ao processar o turno global; tentará novamente no próximo intervalo", e);
		}
	}

	/**
	 * @return o número do turno processado, ou vazio se outra instância detém a trava.
	 */
	public Optional<Integer> executarTurno() {
		return transacao.execute(status -> {
			if (!turnoRepository.tentarTravaDoTurno(CHAVE_TRAVA_TURNO)) {
				return Optional.<Integer>empty();
			}
			int numero = turnoRepository.maiorNumero() + 1;
			JogoTurno turno = turnoRepository.saveAndFlush(new JogoTurno(numero, Instant.now()));
			turnoProcessor.processarTurno(numero);
			turno.setStatus(StatusTurno.CONCLUIDO);
			turno.setConcluidoEm(Instant.now());
			turnoRepository.saveAndFlush(turno);
			return Optional.of(numero);
		});
	}

}
