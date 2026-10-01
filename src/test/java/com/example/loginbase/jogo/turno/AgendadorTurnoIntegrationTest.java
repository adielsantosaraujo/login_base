package com.example.loginbase.jogo.turno;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.loginbase.jogo.modelo.JogoTurno;
import com.example.loginbase.jogo.modelo.StatusTurno;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;

/** Não transacional: o turno confirma de verdade; os números criados são removidos ao final. */
@SpringBootTest
class AgendadorTurnoIntegrationTest {

	static final CountDownLatch entrou = new CountDownLatch(1);
	static final CountDownLatch liberar = new CountDownLatch(1);
	static final AtomicInteger chamadas = new AtomicInteger();
	static volatile boolean bloquear;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		TurnoProcessor processorDeTeste() {
			return numero -> {
				chamadas.incrementAndGet();
				if (bloquear) {
					entrou.countDown();
					try {
						liberar.await(10, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			};
		}
	}

	@Autowired JogoTurnoRepository repo;
	@Autowired AgendadorTurnoService service;
	@Autowired PlatformTransactionManager tm;

	int base;

	@BeforeEach
	void antes() {
		base = repo.maiorNumero();
		chamadas.set(0);
		bloquear = false;
	}

	@AfterEach
	void depois() {
		bloquear = false;
		repo.findAll().stream().filter(t -> t.getNumero() > base).forEach(repo::delete);
	}

	@Test
	void incrementaNumeroERegistraConclusao() {
		Optional<Integer> n = service.executarTurno();

		assertThat(n).contains(base + 1);
		JogoTurno t = repo.findByNumero(base + 1).orElseThrow();
		assertThat(t.getStatus()).isEqualTo(StatusTurno.CONCLUIDO);
		assertThat(t.getConcluidoEm()).isNotNull();
		assertThat(chamadas.get()).isEqualTo(1);
	}

	@Test
	void duasDisparosSimultaneosIncrementamApenasUmaVez() throws Exception {
		bloquear = true;
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<Optional<Integer>> primeira = pool.submit(service::executarTurno);
			assertThat(entrou.await(10, TimeUnit.SECONDS)).isTrue();
			Future<Optional<Integer>> segunda = pool.submit(service::executarTurno);
			assertThat(segunda.get(10, TimeUnit.SECONDS)).isEmpty();
			liberar.countDown();
			assertThat(primeira.get(10, TimeUnit.SECONDS)).contains(base + 1);
		} finally {
			liberar.countDown();
			pool.shutdownNow();
		}
		assertThat(repo.maiorNumero()).isEqualTo(base + 1);
		assertThat(chamadas.get()).isEqualTo(1);
	}

}
