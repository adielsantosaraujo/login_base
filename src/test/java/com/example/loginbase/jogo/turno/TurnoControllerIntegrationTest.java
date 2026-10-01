package com.example.loginbase.jogo.turno;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.JogoTurno;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurnoControllerIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired EntityManager em;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired JogoTurnoRepository jogoTurnoRepository;
	@Autowired EventoTurnoRepository eventoRepository;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private Vila novaVila(Usuario u) {
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private void evento(Vila v, int turno, TipoEventoTurno tipo, String msg) {
		eventoRepository.saveAndFlush(new EventoTurno(v.getId(), turno, tipo, msg, Map.of()));
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/jogo/turno")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/jogo/turno/eventos")).andExpect(status().isUnauthorized());
	}

	@Test
	void turnoAtualTemNumeroEProximoEm() throws Exception {
		Usuario u = novoUsuario();
		int numero = jogoTurnoRepository.maiorNumero() + 1000;
		jogoTurnoRepository.saveAndFlush(new JogoTurno(numero, Instant.now()));
		mvc.perform(get("/api/jogo/turno").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numero").value(numero))
				.andExpect(jsonPath("$.proximoEm").isNotEmpty())
				.andExpect(jsonPath("$.segundosRestantes").isNumber());
	}

	@Test
	void eventosSemVilaDevolve404() throws Exception {
		Usuario u = novoUsuario();
		mvc.perform(get("/api/jogo/turno/eventos").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
	}

	@Test
	void eventosDoUltimoTurnoEFiltroEIsolamentoEntreVilas() throws Exception {
		Usuario u = novoUsuario();
		Vila v = novaVila(u);
		Vila outra = novaVila(novoUsuario());
		evento(v, 10, TipoEventoTurno.NASCIMENTO, "antigo");
		evento(v, 15, TipoEventoTurno.NASCIMENTO, "Joao nasceu");
		evento(v, 15, TipoEventoTurno.MORTE, "Maria faleceu");
		evento(outra, 20, TipoEventoTurno.MORTE, "de outra vila");
		em.clear(); // criado_em é preenchido pelo banco: força releitura

		mvc.perform(get("/api/jogo/turno/eventos").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.turno").value(15))
				.andExpect(jsonPath("$.eventos", hasSize(2)))
				.andExpect(jsonPath("$.eventos[0].tipo").value("NASCIMENTO"))
				.andExpect(jsonPath("$.eventos[0].timestamp").isNotEmpty())
				.andExpect(jsonPath("$.eventos[1].mensagem").value("Maria faleceu"));

		mvc.perform(get("/api/jogo/turno/eventos?turno=10").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.turno").value(10))
				.andExpect(jsonPath("$.eventos", hasSize(1)));

		mvc.perform(get("/api/jogo/turno/eventos?turno=20").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.eventos", hasSize(0)));
	}

}
