package com.example.loginbase.jogo.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.servico.AnexacaoService;
import com.example.loginbase.jogo.servico.ConsultaMasmorras;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegiaoControladorIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired LadrilhoJazidaRepository ladrilhoJazidaRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired RegiaoBonusRepository regiaoBonusRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@MockitoBean ConsultaMasmorras consultaMasmorras;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	/** Vila com as regiões 6, 7 e 10 possuídas e o estoque informado (ouro, madeira, pedra). */
	private Vila vila(Usuario u, String ouro, String madeira, String pedra) {
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila Teste", 42L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i == 6 || i == 7 || i == 10) {
				r.setPossuida(true);
			}
			// tipo e bônus são gravados na criação, inclusive nas regiões não possuídas
			r.setTipo(i == 6 ? TipoRegiao.URBANA : i == 3 ? TipoRegiao.MONTANHA : TipoRegiao.FLORESTA);
			r = regiaoRepository.save(r);
			regiaoBonusRepository.save(new RegiaoBonus(r.getId(), BonusRegiao.ROCHA, 1, 40));
			regiaoBonusRepository.save(new RegiaoBonus(r.getId(), BonusRegiao.FERRO, 2, 20));
			regiaoBonusRepository.save(new RegiaoBonus(r.getId(), BonusRegiao.CARVAO, 3, 10));
		}
		regiaoRepository.flush();
		estoqueService.inicializar(vila, Map.of(Recurso.OURO, new BigDecimal(ouro),
				Recurso.MADEIRA, new BigDecimal(madeira), Recurso.PEDRA, new BigDecimal(pedra)));
		return vila;
	}

	private ResultActions anexar(Usuario u, int indice) throws Exception {
		return mvc.perform(post("/api/jogo/regioes/" + indice + "/anexar").with(user(u.getEmail()).roles("USER"))
				.with(csrf()));
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(post("/api/jogo/regioes/2/anexar").with(csrf())).andExpect(status().isUnauthorized());
	}

	@Test
	void anexaRegiaoAdjacenteDebitaEstoqueEPersisteJazidas() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vila(u, "500", "500", "500");
		anexar(u, 3).andExpect(status().isOk())
				.andExpect(jsonPath("$.regiao.indice").value(3))
				.andExpect(jsonPath("$.regiao.tipo").value("MONTANHA"))
				.andExpect(jsonPath("$.regiao.bonus", hasSize(3)))
				.andExpect(jsonPath("$.regiao.bonus[0].bonus").value("ROCHA"))
				.andExpect(jsonPath("$.regiao.possuida").value(true))
				.andExpect(jsonPath("$.custo.ouro").value(150))
				.andExpect(jsonPath("$.custo.madeira").value(50))
				.andExpect(jsonPath("$.custo.pedra").value(50))
				.andExpect(jsonPath("$.estoque.OURO").value(350.0))
				.andExpect(jsonPath("$.estoque.MADEIRA").value(450.0));

		Regiao r3 = regiaoRepository.findByVilaIdAndIndice(vila.getId(), 3).orElseThrow();
		assertThat(r3.isPossuida()).isTrue();
		assertThat(r3.getTipo()).isEqualTo(TipoRegiao.MONTANHA);
		assertThat(estoqueService.quantidade(vila, Recurso.OURO)).isEqualByComparingTo("350");
		assertThat(estoqueService.quantidade(vila, Recurso.PEDRA)).isEqualByComparingTo("450");
		assertThat(ladrilhoJazidaRepository.findAllByRegiaoId(r3.getId())).hasSize(100);
		assertThat(eventoRepository.findAll()).anyMatch(
				e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == TipoEventoTurno.REGIAO_ANEXADA);

		// k=4 agora: próximo custo 203/100/100
		mvc.perform(get("/api/jogo/regioes/4/custo-anexacao").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ouro").value(203))
				.andExpect(jsonPath("$.madeira").value(100))
				.andExpect(jsonPath("$.pedra").value(100));
	}

	@Test
	void anexarSomaBonusDaRegiaoAoMapa() throws Exception {
		Usuario u = novoUsuario();
		vila(u, "500", "500", "500");
		mvc.perform(get("/api/jogo/vila/mapa").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.vila.bonusRegiao.ROCHA").value(120));
		anexar(u, 3).andExpect(status().isOk());
		mvc.perform(get("/api/jogo/vila/mapa").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.vila.bonusRegiao.ROCHA").value(160))
				.andExpect(jsonPath("$.vila.bonusRegiao.length()").value(13));
	}

	@Test
	void anexarUrbanaNaoGeraJazidas() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vila(u, "500", "500", "500");
		Regiao r2 = regiaoRepository.findByVilaIdAndIndice(vila.getId(), 2).orElseThrow();
		r2.setTipo(TipoRegiao.URBANA);
		regiaoRepository.saveAndFlush(r2);
		anexar(u, 2).andExpect(status().isOk()).andExpect(jsonPath("$.regiao.tipo").value("URBANA"));
		assertThat(ladrilhoJazidaRepository.findAllByRegiaoId(r2.getId())).isEmpty();
	}

	@Test
	void regiaoSemTipoDevolve400() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vila(u, "500", "500", "500");
		Regiao r3 = regiaoRepository.findByVilaIdAndIndice(vila.getId(), 3).orElseThrow();
		r3.setTipo(null);
		regiaoRepository.saveAndFlush(r3);
		anexar(u, 3).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Região sem tipo"));
	}

	@Test
	void regiaoNaoAdjacenteDevolve400() throws Exception {
		Usuario u = novoUsuario();
		vila(u, "500", "500", "500");
		anexar(u, 1).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value(containsString("adjacente")));
	}

	@Test
	void regiaoComMasmorraAtivaDevolve400() throws Exception {
		Usuario u = novoUsuario();
		vila(u, "500", "500", "500");
		when(consultaMasmorras.nivelMasmorraAtiva(any(), eq(11))).thenReturn(Optional.of(3));
		anexar(u, 11).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value(containsString("masmorra ativa")));
	}

	@Test
	void recursosInsuficientesNaoDebitaNada() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vila(u, "100", "500", "500");
		anexar(u, 3).andExpect(status().is4xxClientError())
				.andExpect(jsonPath("$.erro").value(containsString("Recursos insuficientes")));
		assertThat(estoqueService.quantidade(vila, Recurso.MADEIRA)).isEqualByComparingTo("500");
		assertThat(regiaoRepository.findByVilaIdAndIndice(vila.getId(), 3).orElseThrow().isPossuida()).isFalse();
	}

	@Test
	void regiaoJaPossuidaDevolve400() throws Exception {
		Usuario u = novoUsuario();
		vila(u, "500", "500", "500");
		anexar(u, 6).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value(containsString("já possuída")));
	}

	@Test
	void indiceForaDaGradeDevolve404() throws Exception {
		Usuario u = novoUsuario();
		vila(u, "500", "500", "500");
		anexar(u, 17).andExpect(status().isNotFound());
	}

	@Test
	void semVilaDevolve404() throws Exception {
		Usuario u = novoUsuario();
		anexar(u, 3).andExpect(status().isNotFound());
	}

	@Test
	// Valores da fórmula round(150 x 1,35^(k-3)) HALF_UP; a tabela de regioes.md diverge da fórmula em k=5, 7, 8 e 9.
	void custoSegueAFormulaDeGameDesign() {
		int[][] esperado = { { 3, 150, 50 }, { 4, 203, 100 }, { 5, 273, 150 }, { 6, 369, 200 }, { 7, 498, 250 },
				{ 8, 673, 300 }, { 9, 908, 350 }, { 10, 1226, 400 }, { 15, 5497, 650 } };
		for (int[] e : esperado) {
			var c = AnexacaoService.custoParaK(e[0]);
			assertThat(c.ouro()).as("ouro k=%d", e[0]).isEqualTo(e[1]);
			assertThat(c.madeira()).as("madeira k=%d", e[0]).isEqualTo(e[2]);
			assertThat(c.pedra()).as("pedra k=%d", e[0]).isEqualTo(e[2]);
		}
	}

}
