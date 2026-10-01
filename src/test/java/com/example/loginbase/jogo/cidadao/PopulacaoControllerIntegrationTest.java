package com.example.loginbase.jogo.cidadao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PopulacaoControllerIntegrationTest {

	private static final String VALIDO = """
			{"regioesEscolhidas":[6,7,2],"tipos":{"6":"URBANA","7":"URBANA","2":"COLETA"},"semente":123}""";

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;

	private Usuario usuarioComVila() throws Exception {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		mvc.perform(post("/api/jogo/vila").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(VALIDO)).andExpect(status().isCreated());
		return u;
	}

	private ResultActions obter(Usuario u) throws Exception {
		return mvc.perform(get("/api/jogo/vila/populacao").with(user(u.getEmail()).roles("USER")));
	}

	private ResultActions confirmar(Usuario u, String corpo) throws Exception {
		return mvc.perform(post("/api/jogo/vila/populacao").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private String corpo(long familiaId, long cidadaoId, String car, String prof) {
		return "{\"familiaLiderId\":" + familiaId + ",\"cidadaos\":[{\"cidadaoId\":" + cidadaoId
				+ ",\"caracteristicas\":" + car + ",\"profissoes\":" + prof + "}]}";
	}

	@Test
	void obterDevolveFamiliasECidadaosComPendentes() throws Exception {
		Usuario u = usuarioComVila();
		obter(u).andExpect(status().isOk())
				.andExpect(jsonPath("$.populacaoConfirmada").value(false))
				.andExpect(jsonPath("$.familias.length()").value(4))
				.andExpect(jsonPath("$.familias[0].cidadaos.length()").value(4))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].pontosCarPendentes").value(20))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].pontosProfPendentes").value(10));
		mvc.perform(get("/api/jogo/vila").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.populacaoConfirmada").value(false));
	}

	@Test
	void confirmaDistribuicaoELider() throws Exception {
		Usuario u = usuarioComVila();
		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		Familia f = familiaRepository.findByVilaId(vila.getId()).get(0);
		Cidadao c = cidadaoRepository.findByFamiliaIdAndVivoTrue(f.getId()).get(0);
		confirmar(u, corpo(f.getId(), c.getId(), "{\"VIT\":10,\"CAR\":8}", "{\"MINEIRO\":5,\"CONSTRUTOR\":3}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.populacaoConfirmada").value(true));
		Cidadao salvo = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertEquals(10, salvo.getVit());
		assertEquals(8, salvo.getCar());
		assertEquals(2, salvo.getPontosCarPendentes());
		assertEquals(2, salvo.getPontosProfPendentes());
		assertEquals(5, profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), Profissao.MINEIRO)
				.orElseThrow().getPontosBase());
		var v = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		assertTrue(v.isPopulacaoConfirmada());
		assertEquals(f.getId(), v.getFamiliaLiderId());
		mvc.perform(get("/api/jogo/vila").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.populacaoConfirmada").value(true));
	}

	@Test
	void recusaLimitesEMantemNaoConfirmada() throws Exception {
		Usuario u = usuarioComVila();
		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		Familia f = familiaRepository.findByVilaId(vila.getId()).get(0);
		Cidadao c = cidadaoRepository.findByFamiliaIdAndVivoTrue(f.getId()).get(0);
		confirmar(u, corpo(f.getId(), c.getId(), "{\"VIT\":11}", "{}")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Máximo 10 por característica"));
		confirmar(u, corpo(f.getId(), c.getId(), "{\"VIT\":10,\"FOR\":10,\"CAR\":1}", "{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Máximo 20 pontos de característica"));
		confirmar(u, corpo(f.getId(), c.getId(), "{}", "{\"MINEIRO\":6}")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Máximo 5 por profissão"));
		confirmar(u, corpo(f.getId(), c.getId(), "{}", "{\"MINEIRO\":5,\"FERREIRO\":5,\"CONSTRUTOR\":1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Máximo 10 pontos de profissão"));
		confirmar(u, "{\"cidadaos\":[]}").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Escolha uma família líder"));
		assertEquals(false, vilaRepository.findByUsuarioId(u.getId()).orElseThrow().isPopulacaoConfirmada());
	}

	@Test
	void recusaSegundaConfirmacao() throws Exception {
		Usuario u = usuarioComVila();
		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		List<Familia> familias = familiaRepository.findByVilaId(vila.getId());
		String corpo = "{\"familiaLiderId\":" + familias.get(0).getId() + ",\"cidadaos\":[]}";
		confirmar(u, corpo).andExpect(status().isOk());
		confirmar(u, corpo).andExpect(status().isBadRequest());
	}

}
