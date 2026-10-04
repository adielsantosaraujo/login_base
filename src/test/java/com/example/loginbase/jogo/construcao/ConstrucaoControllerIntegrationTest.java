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
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConstrucaoControllerIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired com.example.loginbase.jogo.repositorio.LadrilhoRepository ladrilhoRepository;
	@Autowired EstoqueService estoqueService;

	private Usuario usuarioComVila(String madeira) {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 42L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i <= 3) {
				r.setPossuida(true);
				r.setTipo(i == 1 ? TipoRegiao.URBANA : i == 2 ? TipoRegiao.FLORESTA : TipoRegiao.MONTANHA);
			}
			r = regiaoRepository.save(r);
			if (i == 1) {
				ladrilhoRepository.save(new Ladrilho(r.getId(), 2, 3, TipoTerreno.DESENVOLVIMENTO, 0, 0));
			}
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

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/jogo/construcoes/catalogo")).andExpect(status().isUnauthorized());
	}

	@Test
	void criaCasaEConsulta() throws Exception {
		Usuario u = usuarioComVila("100");
		String corpo = criar(u, "CASA", 1, 2, 3).andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("CASA"))
				.andExpect(jsonPath("$.nivel").value("N1"))
				.andExpect(jsonPath("$.estado").value("EM_OBRA"))
				.andExpect(jsonPath("$.poTotal").value(4))
				.andExpect(jsonPath("$.poAtual").value(0))
				.andReturn().getResponse().getContentAsString();
		String id = com.jayway.jsonpath.JsonPath.read(corpo, "$.id").toString();
		mvc.perform(get("/api/jogo/construcoes/" + id).with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.x").value(2));
		mvc.perform(get("/api/jogo/regioes/1").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ladrilhos[?(@.x==2 && @.y==3)].construcao.estado").value("EM_OBRA"))
				.andExpect(jsonPath("$.ladrilhos[?(@.x==2 && @.y==3)].construcao.poTotal").value(4))
				.andExpect(jsonPath("$.ladrilhos[?(@.x==2 && @.y==3)].construcao.poAtual").value(0));
	}

	@Test
	void ladrilhoOcupadoDevolve400() throws Exception {
		Usuario u = usuarioComVila("100");
		criar(u, "CASA", 1, 0, 0).andExpect(status().isCreated());
		criar(u, "CASA", 1, 0, 0).andExpect(status().isBadRequest());
	}

	@Test
	void regiaoErradaDevolve400() throws Exception {
		Usuario u = usuarioComVila("100");
		criar(u, "FAZENDA_PLANTIO", 1, 0, 0).andExpect(status().isBadRequest());
	}

	@Test
	void minaDeFerroSoEmMontanha() throws Exception {
		Usuario u = usuarioComVila("100");
		criar(u, "MINA_FERRO", 3, 0, 0).andExpect(status().isCreated());
		criar(u, "MINA_FERRO", 2, 0, 0).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("Montanha")));
	}

	@Test
	void recursosInsuficientesDevolve409() throws Exception {
		Usuario u = usuarioComVila("5");
		criar(u, "CASA", 1, 0, 0).andExpect(status().isConflict());
	}

	@Test
	void catalogoListaTodosOsTipos() throws Exception {
		Usuario u = usuarioComVila("0");
		mvc.perform(get("/api/jogo/construcoes/catalogo").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(TipoConstrucao.values().length)))
				.andExpect(jsonPath("$[?(@.tipo=='CASA')].custoN1.MADEIRA").value(20))
				.andExpect(jsonPath("$[?(@.tipo=='CASA')].regioes[*]").value(org.hamcrest.Matchers.contains("URBANA")))
				.andExpect(jsonPath("$[?(@.tipo=='CASA')].terreno").value(org.hamcrest.Matchers.contains("DESENVOLVIMENTO")))
				.andExpect(jsonPath("$[?(@.tipo=='FAZENDA_PLANTIO')].regioes[*]")
						.value(org.hamcrest.Matchers.contains("FLORESTA", "PLANICIE")))
				.andExpect(jsonPath("$[?(@.tipo=='FAZENDA_PLANTIO')].terreno").value(org.hamcrest.Matchers.contains("PLANTACOES")));
	}

}
