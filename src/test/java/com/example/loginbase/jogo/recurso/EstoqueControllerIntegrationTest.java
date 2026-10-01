package com.example.loginbase.jogo.recurso;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstoqueControllerIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired EstoqueService estoqueService;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/jogo/estoque")).andExpect(status().isUnauthorized());
	}

	@Test
	void semVilaDevolve404() throws Exception {
		Usuario u = novoUsuario();
		mvc.perform(get("/api/jogo/estoque").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
	}

	@Test
	void devolve20RecursosComQuantidadeECapacidade() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila Teste", 42L, 1));
		estoqueService.inicializar(vila, Map.of(Recurso.MADEIRA, new BigDecimal("250"), Recurso.OURO,
				new BigDecimal("100")));

		mvc.perform(get("/api/jogo/estoque").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.recursos", hasSize(20)))
				.andExpect(jsonPath("$.recursos[0].recurso").value("MADEIRA"))
				.andExpect(jsonPath("$.recursos[0].nome").value("Madeira"))
				.andExpect(jsonPath("$.recursos[0].quantidade").value(250.0))
				.andExpect(jsonPath("$.recursos[0].capacidade").value(500.0))
				.andExpect(jsonPath("$.recursos[0].percentualUsado").value(50.0))
				.andExpect(jsonPath("$.recursos[1].quantidade").value(0.0))
				.andExpect(jsonPath("$.recursos[19].recurso").value("OURO"))
				.andExpect(jsonPath("$.recursos[19].quantidade").value(100.0))
				.andExpect(jsonPath("$.recursos[19].capacidade").value(nullValue()))
				.andExpect(jsonPath("$.recursos[19].percentualUsado").value(nullValue()));
	}

}
