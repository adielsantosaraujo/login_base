package com.example.loginbase.jogo.recurso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.excecao.RecursosInsuficientesException;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaLimiteArmazenamento;

@SpringBootTest
@Transactional
class EstoqueServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired EstoqueService service;
	@Autowired EtapaLimiteArmazenamento etapa;
	@Autowired EventoTurnoRepository eventos;

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private static BigDecimal bd(String v) {
		return new BigDecimal(v);
	}

	@Test
	void inicializarListarCreditarEQuantidade() {
		Vila v = novaVila();
		service.inicializar(v, Map.of(Recurso.MADEIRA, bd("200"), Recurso.OURO, bd("200")));

		assertThat(service.listar(v)).hasSize(Recurso.values().length);
		assertThat(service.quantidade(v, Recurso.MADEIRA)).isEqualByComparingTo("200");
		assertThat(service.quantidade(v, Recurso.PEDRA)).isEqualByComparingTo("0");

		service.creditar(v, Recurso.MADEIRA, bd("50.5"));
		service.creditar(v, Recurso.LA, bd("3"));
		assertThat(service.quantidade(v, Recurso.MADEIRA)).isEqualByComparingTo("250.5");
		assertThat(service.quantidade(v, Recurso.LA)).isEqualByComparingTo("3");
	}

	@Test
	void debitarEAtomico() {
		Vila v = novaVila();
		service.inicializar(v, Map.of(Recurso.MADEIRA, bd("100"), Recurso.PEDRA, bd("10")));

		assertThatThrownBy(() -> service.debitar(v, Map.of(Recurso.MADEIRA, bd("50"), Recurso.PEDRA, bd("11")),
				"Faltou recurso")).isInstanceOf(RecursosInsuficientesException.class).hasMessage("Faltou recurso");
		assertThat(service.quantidade(v, Recurso.MADEIRA)).isEqualByComparingTo("100");

		service.debitar(v, Map.of(Recurso.MADEIRA, bd("50"), Recurso.PEDRA, bd("10")));
		assertThat(service.quantidade(v, Recurso.MADEIRA)).isEqualByComparingTo("50");
		assertThat(service.quantidade(v, Recurso.PEDRA)).isEqualByComparingTo("0");
	}

	@Test
	void aplicarLimiteReduzExcedenteEOuroNaoTemLimite() {
		Vila v = novaVila();
		service.inicializar(v, Map.of(Recurso.MADEIRA, bd("620"), Recurso.PEDRA, bd("500"),
				Recurso.OURO, bd("999999")));

		Map<Recurso, BigDecimal> perdas = service.aplicarLimiteArmazenamento(v, 7);

		assertThat(perdas).containsOnlyKeys(Recurso.MADEIRA);
		assertThat(perdas.get(Recurso.MADEIRA)).isEqualByComparingTo("120");
		assertThat(service.quantidade(v, Recurso.MADEIRA)).isEqualByComparingTo("500");
		assertThat(service.quantidade(v, Recurso.OURO)).isEqualByComparingTo("999999");
	}

	@Test
	void etapaRegistraEventoEstoquePerdido() {
		Vila v = novaVila();
		service.inicializar(v, Map.of(Recurso.GRAOS, bd("700")));

		etapa.executar(v, 8);

		List<EventoTurno> lista = eventos.findByVilaIdAndTurnoOrderByIdAsc(v.getId(), 8);
		assertThat(lista).hasSize(1);
		assertThat(lista.get(0).getTipo()).isEqualTo(TipoEventoTurno.ESTOQUE_PERDIDO);
		assertThat(lista.get(0).getDados()).containsEntry("recurso", "GRAOS");
		assertThat(service.quantidade(v, Recurso.GRAOS)).isEqualByComparingTo("500");
	}

}
