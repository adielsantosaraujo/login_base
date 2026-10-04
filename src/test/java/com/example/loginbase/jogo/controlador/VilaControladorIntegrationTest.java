package com.example.loginbase.jogo.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
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
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;
import com.example.loginbase.jogo.repositorio.VilaPreviaRepository;
import com.example.loginbase.jogo.servico.MapaTestes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VilaControladorIntegrationTest {

	private static final ObjectMapper JSON = new ObjectMapper();

	@Autowired MockMvc mvc;
	@Autowired VilaPreviaRepository previaRepository;
	@Autowired RegiaoBonusRepository regiaoBonusRepository;
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

	/** Gera a prévia e devolve {previaId, tipos das 16 regiões}. */
	private Previa gerarPrevia(Usuario u) throws Exception {
		String corpo = mvc.perform(post("/api/jogo/vila/previa").with(user(u.getEmail()).roles("USER")).with(csrf()))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		JsonNode no = JSON.readTree(corpo);
		List<TipoRegiao> tipos = new ArrayList<>();
		no.get("regioes").forEach(r -> tipos.add(TipoRegiao.valueOf(r.get("tipo").asText())));
		return new Previa(no.get("previaId").asText(), tipos);
	}

	private record Previa(String id, List<TipoRegiao> tipos) {
		String corpo(List<Integer> indices) {
			return "{\"previaId\":\"" + id + "\",\"indices\":" + indices + "}";
		}
	}

	@Test
	void criaVilaComSelecaoValidaApagaPreviaEPoeCasasNaUrbana() throws Exception {
		Usuario u = novoUsuario();
		Previa previa = gerarPrevia(u);
		List<Integer> indices = MapaTestes.selecionePorTipos(previa.tipos(), true);
		int urbana = indices.stream().filter(i -> previa.tipos().get(i - 1) == TipoRegiao.URBANA).findFirst()
				.orElseThrow();
		criar(u, previa.corpo(indices)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.vilaId").exists())
				.andExpect(jsonPath("$.proximaEtapa").value("DISTRIBUIR_POPULACAO"));

		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		assertFalse(vila.isPopulacaoConfirmada());
		assertFalse(previaRepository.existsById(u.getId()));

		var regioes = regiaoRepository.findAllByVilaId(vila.getId());
		assertEquals(16, regioes.size());
		assertEquals(3, regioes.stream().filter(Regiao::isPossuida).count());
		for (Regiao r : regioes) {
			assertEquals(previa.tipos().get(r.getIndice() - 1), r.getTipo());
			assertEquals(3, regiaoBonusRepository.findByRegiaoIdOrderByPosicao(r.getId()).size());
			if (r.isPossuida()) {
				assertEquals(100, ladrilhoRepository.findAllByRegiaoId(r.getId()).size());
			}
		}

		List<Construcao> casas = construcaoRepository.findByVilaId(vila.getId());
		assertEquals(4, casas.size());
		assertEquals(List.of(0, 2, 4, 6), casas.stream().map(Construcao::getX).sorted().toList());
		casas.forEach(c -> {
			assertEquals(urbana, c.getRegiaoIndice());
			assertEquals(0, c.getY());
		});

		List<Familia> familias = familiaRepository.findByVilaId(vila.getId());
		assertEquals(4, familias.size());
		assertEquals(4, familias.stream().map(Familia::getCasaId).distinct().count());
		assertEquals(16, cidadaoRepository.findByVilaId(vila.getId()).size());

		mvc.perform(get("/api/jogo/vila").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.vilaId").value(vila.getId()))
				.andExpect(jsonPath("$.estoque.MADEIRA").value(200))
				.andExpect(jsonPath("$.regioes.length()").value(16))
				.andExpect(jsonPath("$.regioes[0].bonus.length()").value(3))
				.andExpect(jsonPath("$.bonusRegiao.length()").value(13));
	}

	@Test
	void selecaoComQuatroRegioesDevolve400() throws Exception {
		Usuario u = novoUsuario();
		Previa previa = gerarPrevia(u);
		criar(u, previa.corpo(List.of(1, 2, 3, 4))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("SELECAO_INVALIDA"));
		assertFalse(vilaRepository.existsByUsuarioId(u.getId()));
		assertTrue(previaRepository.existsById(u.getId()));
	}

	@Test
	void indicesRepetidosOuForaDaFaixaDevolve400() throws Exception {
		Usuario u = novoUsuario();
		Previa previa = gerarPrevia(u);
		criar(u, previa.corpo(List.of(1, 1, 2))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("SELECAO_INVALIDA"));
		criar(u, previa.corpo(List.of(1, 2, 17))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("SELECAO_INVALIDA"));
	}

	@Test
	void selecaoDesconexaDevolve400() throws Exception {
		Usuario u = novoUsuario();
		Previa previa = gerarPrevia(u);
		criar(u, previa.corpo(List.of(1, 3, 6))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("REGIAO_NAO_ADJACENTE"))
				.andExpect(jsonPath("$.erro").value("As regiões escolhidas precisam ser vizinhas entre si"));
	}

	@Test
	void selecaoSemUrbanaDevolve400() throws Exception {
		Usuario u = novoUsuario();
		Previa previa = gerarPrevia(u);
		List<Integer> indices = MapaTestes.selecionePorTipos(previa.tipos(), false);
		criar(u, previa.corpo(indices)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("SEM_REGIAO_URBANA"))
				.andExpect(jsonPath("$.erro").value("Ao menos uma região deve ser Urbana"));
		assertFalse(vilaRepository.existsByUsuarioId(u.getId()));
	}

	@Test
	void previaExpiradaDevolve409() throws Exception {
		Usuario u = novoUsuario();
		Previa antiga = gerarPrevia(u);
		Previa nova = gerarPrevia(u);
		List<Integer> indices = MapaTestes.selecionePorTipos(nova.tipos(), true);
		criar(u, antiga.corpo(indices)).andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("PREVIA_EXPIRADA"))
				.andExpect(jsonPath("$.erro").value("O mapa mudou. Escolha as regiões novamente"));
		criar(u, "{\"previaId\":null,\"indices\":" + indices + "}").andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("PREVIA_EXPIRADA"))
				.andExpect(jsonPath("$.erro").value("O mapa mudou. Escolha as regiões novamente"));
		criar(novoUsuario(), antiga.corpo(indices)).andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("PREVIA_EXPIRADA"))
				.andExpect(jsonPath("$.erro").value("O mapa mudou. Escolha as regiões novamente"));
	}

	@Test
	void ordemDeValidacaoPreviaAntesDeSelecao() throws Exception {
		Usuario u = novoUsuario();
		Previa antiga = gerarPrevia(u);
		gerarPrevia(u);
		criar(u, antiga.corpo(List.of(1, 3, 6))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("PREVIA_EXPIRADA"))
				.andExpect(jsonPath("$.erro").value("O mapa mudou. Escolha as regiões novamente"));
	}

	@Test
	void segundaCriacaoDevolve409() throws Exception {
		Usuario u = novoUsuario();
		Previa previa = gerarPrevia(u);
		String corpo = previa.corpo(MapaTestes.selecionePorTipos(previa.tipos(), true));
		criar(u, corpo).andExpect(status().isCreated());
		criar(u, corpo).andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("VILA_JA_EXISTE"))
				.andExpect(jsonPath("$.erro").value("Usuário já possui uma vila"));
	}

	@Test
	void semVilaGetDevolve404() throws Exception {
		mvc.perform(get("/api/jogo/vila").with(user(novoUsuario().getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/jogo/vila")).andExpect(status().isUnauthorized());
	}

}
