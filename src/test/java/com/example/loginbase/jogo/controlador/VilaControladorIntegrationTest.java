package com.example.loginbase.jogo.controlador;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VilaControladorIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired LadrilhoJazidaRepository ladrilhoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private ResultActions criar(Usuario u, String corpo) throws Exception {
		return mvc.perform(post("/api/jogo/vila").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private static final String VALIDO = """
			{"regioesEscolhidas":[6,7,2],"tipos":{"6":"URBANA","7":"URBANA","2":"COLETA"},"semente":123}""";

	@Test
	void criaVilaGeraQuatroFamiliasEmCasasDistintas() throws Exception {
		Usuario u = novoUsuario();
		criar(u, VALIDO).andExpect(status().isCreated());
		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		assertEquals(false, vila.isPopulacaoConfirmada());
		var casaIds = construcaoRepository.findAll().stream()
				.filter(c -> c.getVilaId().equals(vila.getId())).map(Construcao::getId).toList();
		List<Familia> familias = familiaRepository.findByVilaId(vila.getId());
		assertEquals(4, familias.size());
		assertEquals(4, familias.stream().map(Familia::getCasaId).distinct().count());
		familias.forEach(f -> org.junit.jupiter.api.Assertions.assertTrue(casaIds.contains(f.getCasaId())));
		List<Cidadao> cidadaos = cidadaoRepository.findByVilaId(vila.getId());
		assertEquals(16, cidadaos.size());
		cidadaos.forEach(c -> {
			assertEquals(20, c.getPontosCarPendentes());
			assertEquals(10, c.getPontosProfPendentes());
		});
		assertEquals(null, vila.getFamiliaLiderId());
	}

	@Test
	void criaVilaComRecursosCasasERegioes() throws Exception {
		Usuario u = novoUsuario();
		criar(u, VALIDO)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.vilaId").exists())
				.andExpect(jsonPath("$.semente").value(123))
				.andExpect(jsonPath("$.regioes", hasSize(16)))
				.andExpect(jsonPath("$.regioes[?(@.possuida == true)]", hasSize(3)))
				.andExpect(jsonPath("$.estoque.MADEIRA").value(200))
				.andExpect(jsonPath("$.estoque.PEDRA").value(100))
				.andExpect(jsonPath("$.estoque.ARGILA").value(50))
				.andExpect(jsonPath("$.estoque.TABUA").value(20))
				.andExpect(jsonPath("$.estoque.GRAOS").value(200))
				.andExpect(jsonPath("$.estoque.CARNE").value(40))
				.andExpect(jsonPath("$.estoque.OURO").value(200));

		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		assertEquals(1, vila.getTurnoCriacao());
		assertEquals(16, regiaoRepository.findAllByVilaId(vila.getId()).size());
		regiaoRepository.findAllByVilaId(vila.getId()).stream().filter(r -> r.isPossuida())
				.forEach(r -> assertEquals(100, ladrilhoRepository.findAllByRegiaoId(r.getId()).size()));
		List<Construcao> casas = construcaoRepository.findByVilaId(vila.getId());
		assertEquals(4, casas.size());
		assertEquals(List.of(0, 2, 4, 6), casas.stream().map(Construcao::getX).sorted().toList());
		casas.forEach(c -> {
			assertEquals(6, c.getRegiaoIndice());
			assertEquals(0, c.getY());
		});

		mvc.perform(get("/api/jogo/vila").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.vilaId").value(vila.getId()));
	}

	@Test
	void regioesNaoAdjacentesDevolve400() throws Exception {
		criar(novoUsuario(), """
				{"regioesEscolhidas":[1,3,2],"tipos":{"1":"URBANA","3":"RURAL","2":"COLETA"}}""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("A segunda região não é adjacente à primeira"));
	}

	@Test
	void semUrbanaDevolve400() throws Exception {
		criar(novoUsuario(), """
				{"regioesEscolhidas":[6,7,2],"tipos":{"6":"RURAL","7":"RURAL","2":"COLETA"}}""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Ao menos 1 região Urbana é obrigatória"));
	}

	@Test
	void usuarioComVilaDevolve409() throws Exception {
		Usuario u = novoUsuario();
		criar(u, VALIDO).andExpect(status().isCreated());
		criar(u, VALIDO).andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("Usuário já tem vila"));
	}

	@Test
	void semVilaGetDevolve404() throws Exception {
		mvc.perform(get("/api/jogo/vila").with(user(novoUsuario().getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
	}

	@Test
	void previewDevolveContagemPorRegiaoEDeterministica() throws Exception {
		Usuario u = novoUsuario();
		mvc.perform(get("/api/jogo/vila/preview").param("semente", "77").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.semente").value(77))
				.andExpect(jsonPath("$.regioes", hasSize(16)))
				.andExpect(jsonPath("$.regioes[0].jazidas.FLORESTA").value(25));
		mvc.perform(get("/api/jogo/vila/preview").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.semente").exists());
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/jogo/vila")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/jogo/vila/preview")).andExpect(status().isUnauthorized());
	}

}
