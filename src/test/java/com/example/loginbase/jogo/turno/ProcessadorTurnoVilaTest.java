package com.example.loginbase.jogo.turno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

class ProcessadorTurnoVilaTest {

	VilaRepository repo = mock(VilaRepository.class);

	private EtapaTurno etapa(int ordem, List<Integer> chamadas) {
		EtapaTurno e = mock(EtapaTurno.class);
		when(e.ordem()).thenReturn(ordem);
		org.mockito.Mockito.doAnswer(i -> chamadas.add(ordem)).when(e).executar(any(), org.mockito.ArgumentMatchers.anyInt());
		return e;
	}

	@Test
	void executaEtapasEmOrdemCrescenteEMarcaTurno() {
		List<Integer> chamadas = new ArrayList<>();
		List<EtapaTurno> desordenadas = new ArrayList<>(List.of(etapa(3, chamadas), etapa(1, chamadas), etapa(2, chamadas)));
		Vila vila = new Vila(1L, "V", 1L, 1);
		when(repo.findById(5L)).thenReturn(Optional.of(vila));

		boolean processou = new ProcessadorTurnoVila(repo, desordenadas).processarVila(5L, 7);

		assertThat(processou).isTrue();
		assertThat(chamadas).containsExactly(1, 2, 3);
		assertThat(vila.getTurnoProcessado()).isEqualTo(7);
	}

	@Test
	void naoReprocessaTurnoJaProcessado() {
		List<Integer> chamadas = new ArrayList<>();
		Vila vila = new Vila(1L, "V", 1L, 1);
		vila.setTurnoProcessado(7);
		when(repo.findById(5L)).thenReturn(Optional.of(vila));

		boolean processou = new ProcessadorTurnoVila(repo, List.of(etapa(1, chamadas))).processarVila(5L, 7);

		assertThat(processou).isFalse();
		assertThat(chamadas).isEmpty();
	}

}
