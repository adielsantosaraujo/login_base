package com.example.loginbase.jogo.construcao;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConstrucaoUpgradeIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired ConstrucaoRepository construcaoRepository;

	private Usuario usuarioComVila(String madeira) {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 42L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i == 1) {
				r.setPossuida(true);
				r.setTipo(TipoRegiao.URBANA);
			}
			regiaoRepository.save(r);
		}
		regiaoRepository.flush();
		estoqueService.inicializar(vila, Map.of(Recurso.MADEIRA, new BigDecimal(madeira),
				Recurso.PEDRA, new BigDecimal("100"), Recurso.ARGILA, new BigDecimal("100")));
		return u;
	}

	private ResultActions criar(Usuario u, String tipo, int regiao, int x, int y) throws Exception {
		return mvc.perform(post("/api/jogo/construcoes").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"%s\",\"regiaoIndice\":%d,\"x\":%d,\"y\":%d}".formatted(tipo, regiao, x, y)));
	}

	private Long casaAtiva(Usuario u, int x, int y) throws Exception {
		String corpo = criar(u, "CASA", 1, x, y).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		Long id = Long.valueOf(com.jayway.jsonpath.JsonPath.read(corpo, "$.id").toString());
		Construcao c = construcaoRepository.findById(id).orElseThrow();
		c.setEstado(EstadoConstrucao.ATIVA);
		construcaoRepository.saveAndFlush(c);
		return id;
	}

	private ResultActions upgrade(Usuario u, Long id, String json) throws Exception {
		return mvc.perform(post("/api/jogo/construcoes/" + id + "/upgrade").with(user(u.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	@Test
	void upgradeN1ParaN2Sucesso() throws Exception {
		Usuario u = usuarioComVila("100");
		Long id = casaAtiva(u, 3, 3);
		upgrade(u, id, "{\"novoNivel\":\"N2\",\"novaX\":3,\"novaY\":3}").andExpect(status().isOk())
				.andExpect(jsonPath("$.nivel").value("N2")).andExpect(jsonPath("$.estado").value("EM_UPGRADE"))
				.andExpect(jsonPath("$.tamanho").value(2)).andExpect(jsonPath("$.poAtual").value(0))
				.andExpect(jsonPath("$.poTotal").value(10));
	}

	@Test
	void upgradeSemCorpoUsaPosicaoAtualEDebitaCusto() throws Exception {
		Usuario u = usuarioComVila("100");
		Long id = casaAtiva(u, 0, 0);
		upgrade(u, id, "").andExpect(status().isOk()).andExpect(jsonPath("$.nivel").value("N2"));
		// custo N1 = 20 madeira, N2 = 50: sobra 100-20-50 = 30; novo upgrade nao cabe (estado EM_UPGRADE)
		upgrade(u, id, "{}").andExpect(status().isBadRequest());
	}

	@Test
	void upgradeComEspacoOcupadoDevolve400() throws Exception {
		Usuario u = usuarioComVila("200");
		Long id = casaAtiva(u, 3, 3);
		criar(u, "CASA", 1, 4, 4).andExpect(status().isCreated());
		upgrade(u, id, "{\"novoNivel\":\"N2\"}").andExpect(status().isBadRequest());
		// deslocar a area para a esquerda/cima (contendo o ladrilho atual) fica livre
		upgrade(u, id, "{\"novoNivel\":\"N2\",\"novaX\":2,\"novaY\":2}").andExpect(status().isOk());
	}

	@Test
	void upgradePulandoNivelDevolve400() throws Exception {
		Usuario u = usuarioComVila("100");
		Long id = casaAtiva(u, 3, 3);
		upgrade(u, id, "{\"novoNivel\":\"N3\"}").andExpect(status().isBadRequest());
	}

	@Test
	void upgradeEmPredioNaoAtivoDevolve400() throws Exception {
		Usuario u = usuarioComVila("100");
		String corpo = criar(u, "CASA", 1, 0, 0).andReturn().getResponse().getContentAsString();
		Long id = Long.valueOf(com.jayway.jsonpath.JsonPath.read(corpo, "$.id").toString());
		upgrade(u, id, "{}").andExpect(status().isBadRequest());
	}

	@Test
	void upgradeRecursoInsuficienteDevolve409() throws Exception {
		Usuario u = usuarioComVila("30");
		Long id = casaAtiva(u, 0, 0);
		upgrade(u, id, "{}").andExpect(status().isConflict());
	}

	@Test
	void upgradeForaDaRegiaoOuAreaNaoContendoAtualDevolve400() throws Exception {
		Usuario u = usuarioComVila("200");
		Long id = casaAtiva(u, 9, 9);
		upgrade(u, id, "{}").andExpect(status().isBadRequest());
		Long id2 = casaAtiva(u, 3, 3);
		upgrade(u, id2, "{\"novaX\":5,\"novaY\":5}").andExpect(status().isBadRequest());
	}

	@Test
	void upgradeDeOutraVilaDevolve403EInexistente404() throws Exception {
		Usuario a = usuarioComVila("100");
		Usuario b = usuarioComVila("100");
		Long id = casaAtiva(a, 0, 0);
		upgrade(b, id, "{}").andExpect(status().isForbidden());
		upgrade(b, 999999L, "{}").andExpect(status().isNotFound());
	}

}
