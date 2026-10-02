package com.example.loginbase.jogo.batalha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.example.loginbase.jogo.batalha.ConsequenciasBatalhaService.Consequencias;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.MorteService;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

class ConsequenciasBatalhaServiceTest {

	private static final long SALT = 0x9E3779B97F4A7C15L;

	private final CidadaoRepository cidadaos = mock(CidadaoRepository.class);
	private final ItemRepository itens = mock(ItemRepository.class);
	private final MorteService morte = mock(MorteService.class);
	private final RegistroEventoTurnoService registro = mock(RegistroEventoTurnoService.class);
	private final ConsequenciasBatalhaService servico = new ConsequenciasBatalhaService(cidadaos, itens, morte, registro);
	private final Vila vila = new Vila(1L, "V", 1L, 1);

	private List<Cidadao> abatidos(int n) {
		List<Cidadao> lista = new ArrayList<>();
		for (long i = 1; i <= n; i++) {
			Cidadao c = new Cidadao(1L, 1L, "C" + i, Sexo.M, 480);
			c.setId(i);
			c.setTropaId(9L);
			when(cidadaos.findById(i)).thenReturn(Optional.of(c));
			lista.add(c);
		}
		return lista;
	}

	private ResultadoBatalha resultado(ResultadoCombate r, List<Cidadao> abatidos) {
		return new ResultadoBatalha(r, 3, List.of(), abatidos.stream().map(Cidadao::getId).toList(), List.of());
	}

	private List<Boolean> esperado(long semente, boolean vitoria, int n) {
		Random rng = new Random(semente ^ SALT);
		List<Boolean> l = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			l.add(ConsequenciasBatalhaService.sortearMorte(rng, vitoria));
		}
		return l;
	}

	@Test
	void sorteioRespeitaProporcoes() {
		Random rng = new Random(42);
		int mortosVitoria = 0;
		int mortosDerrota = 0;
		for (int i = 0; i < 20000; i++) {
			if (ConsequenciasBatalhaService.sortearMorte(rng, true)) {
				mortosVitoria++;
			}
			if (ConsequenciasBatalhaService.sortearMorte(rng, false)) {
				mortosDerrota++;
			}
		}
		assertThat(mortosVitoria / 20000.0).isBetween(0.18, 0.22);
		assertThat(mortosDerrota / 20000.0).isBetween(0.48, 0.52);
	}

	@Test
	void vitoriaAplicaSorteioDeterministicoEFeridoPermaneceNaTropa() {
		List<Cidadao> lista = abatidos(10);
		long semente = 12345L;
		List<Boolean> esp = esperado(semente, true, 10);
		Consequencias c = servico.aplicar(resultado(ResultadoCombate.VITORIA, lista), vila, 20, semente);
		for (int i = 0; i < 10; i++) {
			Cidadao cid = lista.get(i);
			if (esp.get(i)) {
				assertThat(c.mortos()).contains(cid.getId());
				verify(morte).morrer(vila, cid, "batalha", 20);
			} else {
				assertThat(c.feridos()).contains(cid.getId());
				assertThat(cid.getEstado()).isEqualTo(EstadoCidadao.FERIDO);
				assertThat(cid.getFeridoAteTurno()).isEqualTo(26);
				assertThat(cid.getTropaId()).isEqualTo(9L);
			}
		}
		assertThat(c.mortos().size() + c.feridos().size()).isEqualTo(10);
		verify(itens, never()).deleteAll(any(Iterable.class));
		verify(registro, org.mockito.Mockito.times(c.feridos().size())).registrar(any(), eq(20),
				eq(TipoEventoTurno.FERIDO), anyString(), any());
	}

	@Test
	void derrotaDestroiItensDoMortoAntesDeMorrerEFeridoNaoPerdeItens() {
		List<Cidadao> lista = abatidos(8);
		long semente = 777L;
		List<Boolean> esp = esperado(semente, false, 8);
		servico.aplicar(resultado(ResultadoCombate.DERROTA, lista), vila, 5, semente);
		for (int i = 0; i < 8; i++) {
			Cidadao cid = lista.get(i);
			if (esp.get(i)) {
				InOrder ordem = inOrder(itens, morte);
				ordem.verify(itens).findByCidadaoId(cid.getId());
				ordem.verify(morte).morrer(vila, cid, "batalha", 5);
			} else {
				verify(itens, never()).findByCidadaoId(cid.getId());
				verify(morte, never()).morrer(any(Vila.class), eq(cid), anyString(), anyInt());
				assertThat(cid.getEstado()).isEqualTo(EstadoCidadao.FERIDO);
				assertThat(cid.getFeridoAteTurno()).isEqualTo(11);
			}
		}
	}

	@Test
	void mesmaSementeMesmoResultado() {
		assertThat(esperado(5L, true, 20)).isEqualTo(esperado(5L, true, 20));
		assertThat(BatalhaExpedicaoService.semente(1, 2, 3)).isEqualTo(BatalhaExpedicaoService.semente(1, 2, 3));
		assertThat(BatalhaExpedicaoService.semente(1, 2, 3)).isNotEqualTo(BatalhaExpedicaoService.semente(1, 2, 4));
	}
}
