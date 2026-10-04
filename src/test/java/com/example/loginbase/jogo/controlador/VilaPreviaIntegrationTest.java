package com.example.loginbase.jogo.controlador;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.FaixaBonusRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VilaPreviaIntegrationTest {

	private static final String URL = "/api/jogo/vila/previa";

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;

	private final ObjectMapper json = new ObjectMapper();

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private String postar(Usuario u) throws Exception {
		MvcResult r = mvc.perform(post(URL).with(user(u.getEmail()).roles("USER")).with(csrf()))
				.andExpect(status().isOk()).andReturn();
		return r.getResponse().getContentAsString();
	}

	private String obter(Usuario u) throws Exception {
		return mvc.perform(get(URL).with(user(u.getEmail()).roles("USER"))).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();
	}

	@Test
	void rodadasIncrementamEMudamPreviaId() throws Exception {
		Usuario u = novoUsuario();
		JsonNode p1 = json.readTree(postar(u));
		JsonNode p2 = json.readTree(postar(u));
		JsonNode p3 = json.readTree(postar(u));
		assertEquals(1, p1.get("rodada").asInt());
		assertEquals(2, p2.get("rodada").asInt());
		assertEquals(3, p3.get("rodada").asInt());
		assertNotEquals(p1.get("previaId").asText(), p2.get("previaId").asText());
		assertNotEquals(p2.get("previaId").asText(), p3.get("previaId").asText());
	}

	@Test
	void getDevolveMesmaPreviaIdentica() throws Exception {
		Usuario u = novoUsuario();
		String criada = postar(u);
		String a = obter(u);
		String b = obter(u);
		assertEquals(criada, a);
		assertEquals(a, b);
	}

	@Test
	void dezesseisRegioesComTresBonusEmFaixasValidas() throws Exception {
		Usuario u = novoUsuario();
		JsonNode p = json.readTree(postar(u));
		JsonNode regioes = p.get("regioes");
		assertEquals(16, regioes.size());
		for (int i = 0; i < 16; i++) {
			JsonNode r = regioes.get(i);
			assertEquals(i + 1, r.get("indice").asInt());
			assertTrue(r.get("tipo").isTextual());
			JsonNode bonus = r.get("bonus");
			assertEquals(3, bonus.size());
			for (int k = 0; k < 3; k++) {
				JsonNode b = bonus.get(k);
				FaixaBonusRegiao faixa = FaixaBonusRegiao.de(b.get("posicao").asInt());
				assertEquals(k + 1, b.get("posicao").asInt());
				int valor = b.get("valor").asInt();
				assertTrue(valor >= faixa.getMin() && valor <= faixa.getMax());
				assertTrue(b.get("bonus").isTextual());
			}
		}
	}

	@Test
	void semPreviaGetDevolve404() throws Exception {
		Usuario u = novoUsuario();
		mvc.perform(get(URL).with(user(u.getEmail()).roles("USER"))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("PREVIA_NAO_ENCONTRADA"));
	}

	@Test
	void comVilaPostEGetDevolvem409() throws Exception {
		Usuario u = novoUsuario();
		vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		mvc.perform(post(URL).with(user(u.getEmail()).roles("USER")).with(csrf()))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("VILA_JA_EXISTE"));
		mvc.perform(get(URL).with(user(u.getEmail()).roles("USER"))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("VILA_JA_EXISTE"));
	}

	@Test
	void jsonTemEstruturaEsperada() throws Exception {
		Usuario u = novoUsuario();
		postar(u);
		mvc.perform(get(URL).with(user(u.getEmail()).roles("USER"))).andExpect(jsonPath("$.regioes", hasSize(16)))
				.andExpect(jsonPath("$.previaId").exists()).andExpect(jsonPath("$.rodada").value(1));
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get(URL)).andExpect(status().isUnauthorized());
		mvc.perform(post(URL).with(csrf())).andExpect(status().isUnauthorized());
	}

}
