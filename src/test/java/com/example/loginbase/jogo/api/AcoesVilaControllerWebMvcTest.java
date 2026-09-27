package com.example.loginbase.jogo.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.construcao.ConstrucaoService;
import com.example.loginbase.jogo.dominio.BatalhaRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.EstadoVila;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.fazenda.FazendaService;
import com.example.loginbase.jogo.forja.ForjaService;
import com.example.loginbase.jogo.quartel.QuartelService;
import com.example.loginbase.seguranca.RegistroSessaoSuccessHandler;
import com.example.loginbase.seguranca.SecurityConfig;
import com.example.loginbase.seguranca.SessaoService;

/**
 * Testes de fatia web (Task 6.2) de {@link AcoesVilaController}: segurança
 * (anônimo 401, sem CSRF 403, autenticado com CSRF 200), validação de corpo
 * de requisição (400) e mapeamento de erros de regra de jogo pelo
 * {@link ErroApiHandler} (422/409), replicando para este controller a mesma
 * cobertura de segurança/erros de {@code VilaControllerWebMvcTest} (Task
 * 6.1).
 */
@WebMvcTest(AcoesVilaController.class)
@Import({ SecurityConfig.class, RegistroSessaoSuccessHandler.class, ErroApiHandler.class, UsuarioAtual.class,
		JogoMapper.class, JogoProperties.class })
class AcoesVilaControllerWebMvcTest {

	private static final Instant AGORA = Instant.parse("2026-01-01T12:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessaoService sessaoService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private ConstrucaoService construcaoService;

	@MockitoBean
	private FazendaService fazendaService;

	@MockitoBean
	private ForjaService forjaService;

	@MockitoBean
	private QuartelService quartelService;

	@MockitoBean
	private VilaService vilaService;

	@MockitoBean
	private BatalhaRepository batalhaRepository;

	@MockitoBean
	private Clock clock;

	@BeforeEach
	void configurarUsuario() {
		Usuario ana = new Usuario();
		ana.setId(1L);
		ana.setNome("Ana");
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana));
		when(clock.instant()).thenReturn(AGORA);
		when(vilaService.consultar(anyLong())).thenReturn(estadoVilaMinima());
		when(batalhaRepository.findByVilaId(anyLong())).thenReturn(List.of());
	}

	// -------------------------------------------------------- segurança --

	@Test
	void postMelhorarAnonimoRecebe401() throws Exception {
		// CSRF é verificado antes da autenticação no filter chain (ver
		// SecurityConfig): sem token CSRF, mesmo requisição anônima recebe 403
		// (cenário coberto por postMelhorarSemCsrfRecebe403). Para isolar o
		// comportamento de autenticação (401 anônimo em /api/**), a requisição
		// aqui inclui um token CSRF válido mas nenhum usuário autenticado.
		mockMvc.perform(post("/api/jogo/predios/{tipo}/melhorar", "SERRARIA").with(csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void postMelhorarSemCsrfRecebe403() throws Exception {
		mockMvc.perform(post("/api/jogo/predios/{tipo}/melhorar", "SERRARIA").with(user("ana@exemplo.com")))
				.andExpect(status().isForbidden());
	}

	// -------------------------------------------------- 4 endpoints, 200 --

	@Test
	void postMelhorarComCsrfChamaServicoERetorna200() throws Exception {
		mockMvc.perform(post("/api/jogo/predios/{tipo}/melhorar", "SERRARIA").with(user("ana@exemplo.com"))
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Vila de Ana")));

		verify(construcaoService).melhorar(1L, TipoPredio.SERRARIA);
	}

	@Test
	void postPlantarComCsrfChamaServicoERetorna200() throws Exception {
		mockMvc.perform(post("/api/jogo/canteiros/{posicao}/plantar", 1).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cultivo\":\"MILHO\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Vila de Ana")));

		verify(fazendaService).plantar(1L, 1, Cultivo.MILHO);
	}

	@Test
	void postForjarComCsrfChamaServicoERetorna200() throws Exception {
		mockMvc.perform(post("/api/jogo/forja/ordens").with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"modelo\":\"ESPADA\",\"nivel\":2,\"quantidade\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Vila de Ana")));

		verify(forjaService).forjar(1L, ModeloItem.ESPADA, 2, 1);
	}

	@Test
	void postTreinarComCsrfChamaServicoERetorna200() throws Exception {
		mockMvc.perform(post("/api/jogo/quartel/ordens").with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"SOLDADO\",\"armaId\":1,\"armaduraId\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Vila de Ana")));

		verify(quartelService).treinar(1L, TipoTropa.SOLDADO, 1L, 2L);
	}

	// --------------------------------------------- corpo inválido → 400 --

	@Test
	void postPlantarComCultivoInvalidoRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/canteiros/{posicao}/plantar", 1).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cultivo\":\"INEXISTENTE\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postForjarComQuantidadeZeroRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/forja/ordens").with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"modelo\":\"ESPADA\",\"nivel\":2,\"quantidade\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postForjarComNivelSeisRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/forja/ordens").with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"modelo\":\"ESPADA\",\"nivel\":6,\"quantidade\":1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postTreinarComArmaIdZeroRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/quartel/ordens").with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"SOLDADO\",\"armaId\":0,\"armaduraId\":2}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	// --------------------------------------------- erro de regra → 422/409 --

	@Test
	void regraJogoExceptionFilaOcupadaRecebe422() throws Exception {
		doThrow(new RegraJogoException(CodigoErro.FILA_OCUPADA, "Já existe uma construção em andamento"))
				.when(construcaoService).melhorar(anyLong(), any());

		mockMvc.perform(post("/api/jogo/predios/{tipo}/melhorar", "SERRARIA").with(user("ana@exemplo.com"))
				.with(csrf()))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.codigo", is("FILA_OCUPADA")));
	}

	@Test
	void regraJogoExceptionTurnoDesatualizadoRecebe409() throws Exception {
		doThrow(new RegraJogoException(CodigoErro.TURNO_DESATUALIZADO, "Turno desatualizado"))
				.when(forjaService).forjar(anyLong(), any(), anyInt(), anyInt());

		mockMvc.perform(post("/api/jogo/forja/ordens").with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"modelo\":\"ESPADA\",\"nivel\":2,\"quantidade\":1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo", is("TURNO_DESATUALIZADO")));
	}

	// -------------------------------------------------------------- dados --

	private EstadoVila estadoVilaMinima() {
		Vila vila = new Vila();
		vila.setId(10L);
		vila.setUsuarioId(1L);
		vila.setNome("Vila de Ana");
		vila.setComida(300_000L);
		vila.setMadeira(400_000L);
		vila.setPedra(300_000L);
		vila.setFerro(50_000L);
		vila.setRecursosAtualizadosEm(AGORA);
		vila.setMasmorraNivelLiberado(1);

		return new EstadoVila(vila, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
	}

}
