package com.example.loginbase.jogo.turno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.example.loginbase.jogo.modelo.JogoTurno;
import com.example.loginbase.jogo.modelo.StatusTurno;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;

class AgendadorTurnoServiceTest {

	JogoTurnoRepository repo = mock(JogoTurnoRepository.class);
	TurnoProcessor processor = mock(TurnoProcessor.class);
	PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
	AgendadorTurnoService service;

	AgendadorTurnoServiceTest() {
		when(tm.getTransaction(org.mockito.ArgumentMatchers.any())).thenReturn(new SimpleTransactionStatus());
		when(repo.saveAndFlush(org.mockito.ArgumentMatchers.any(JogoTurno.class))).thenAnswer(i -> i.getArgument(0));
		service = new AgendadorTurnoService(repo, processor, tm);
	}

	@Test
	void incrementaNumeroDelegaEConclui() {
		when(repo.tentarTravaDoTurno(anyLong())).thenReturn(true);
		when(repo.maiorNumero()).thenReturn(4);

		Optional<Integer> n = service.executarTurno();

		assertThat(n).contains(5);
		verify(processor).processarTurno(5);
	}

	@Test
	void semTravaNaoProcessa() {
		when(repo.tentarTravaDoTurno(anyLong())).thenReturn(false);

		assertThat(service.executarTurno()).isEmpty();
		verify(processor, never()).processarTurno(anyInt());
		verify(repo, never()).saveAndFlush(org.mockito.ArgumentMatchers.any(JogoTurno.class));
	}

	@Test
	void excecaoNoProcessadorNaoPropagaNoAgendador() {
		when(repo.tentarTravaDoTurno(anyLong())).thenReturn(true);
		when(repo.maiorNumero()).thenReturn(0);
		doThrow(new IllegalStateException("boom")).when(processor).processarTurno(1);

		service.processarTurnoGlobal();

		verify(processor).processarTurno(1);
	}

	@Test
	void statusPadraoDeNovoTurnoEProcessando() {
		assertThat(new JogoTurno(1, java.time.Instant.now()).getStatus()).isEqualTo(StatusTurno.PROCESSANDO);
	}

}
