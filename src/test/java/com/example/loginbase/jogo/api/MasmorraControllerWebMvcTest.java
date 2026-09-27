package com.example.loginbase.jogo.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.MapaMasmorra;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Batalha;
import com.example.loginbase.jogo.dominio.BatalhaRepository;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusBatalha;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.masmorra.Loot;
import com.example.loginbase.jogo.masmorra.MasmorraService;
import com.example.loginbase.jogo.masmorra.combate.Combatente;
import com.example.loginbase.jogo.masmorra.combate.EstadoBatalha;
import com.example.loginbase.jogo.masmorra.combate.Lado;
import com.example.loginbase.jogo.masmorra.combate.Posicao;
import com.example.loginbase.seguranca.RegistroSessaoSuccessHandler;
import com.example.loginbase.seguranca.SecurityConfig;
import com.example.loginbase.seguranca.SessaoService;

/**
 * Testes de fatia web (Task 6.3) de {@link MasmorraController}: 201 na
 * criação, 200 na consulta/ação, mapeamento de erros pelo
 * {@link ErroApiHandler} (409 turno desatualizado, 404, 422, 400) e segurança
 * (401 anônimo, 403 sem CSRF) — mesma cobertura das demais fatias web do
 * pacote {@code jogo.api} (Tasks 6.1/6.2). {@link JogoMapper} é um bean real
 * aqui (não mockado): as respostas 201/200 desserializam JSON de verdade
 * gravado em {@link Batalha#getEstado()}/{@link Batalha#getLoot()}, validando
 * o par serialização/desserialização de {@code EstadoBatalha}/{@code Loot}.
 */
@WebMvcTest(MasmorraController.class)
@Import({ SecurityConfig.class, RegistroSessaoSuccessHandler.class, ErroApiHandler.class, UsuarioAtual.class,
		JogoMapper.class, JogoProperties.class })
class MasmorraControllerWebMvcTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessaoService sessaoService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private MasmorraService masmorraService;

	@MockitoBean
	private BatalhaRepository batalhaRepository;

	@BeforeEach
	void configurarUsuario() {
		Usuario ana = new Usuario();
		ana.setId(1L);
		ana.setNome("Ana");
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana));
	}

	// -------------------------------------------------------- segurança --

	@Test
	void postIniciarAnonimoRecebe401() throws Exception {
		// CSRF é verificado antes da autenticação no filter chain: com token CSRF
		// válido mas sem usuário autenticado, isola o comportamento de 401
		// anônimo em /api/** (ver mesmo padrão em AcoesVilaControllerWebMvcTest).
		mockMvc.perform(post("/api/jogo/masmorras/{nivel}/batalhas", 1).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidadeIds\":[3]}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void postIniciarSemCsrfRecebe403() throws Exception {
		mockMvc.perform(post("/api/jogo/masmorras/{nivel}/batalhas", 1).with(user("ana@exemplo.com"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidadeIds\":[3]}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void getConsultarAnonimoRecebe401() throws Exception {
		mockMvc.perform(get("/api/jogo/batalhas/{id}", 50))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void postAgirSemCsrfRecebe403() throws Exception {
		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ENCERRAR_TURNO\",\"turno\":1}"))
				.andExpect(status().isForbidden());
	}

	// ---------------------------------------------------- 201 ao iniciar --

	@Test
	void postIniciarComCsrfRetorna201ComBatalhaDtoValido() throws Exception {
		when(masmorraService.iniciar(1L, 1, List.of(3L, 4L, 5L))).thenReturn(batalhaEmAndamento());

		mockMvc.perform(post("/api/jogo/masmorras/{nivel}/batalhas", 1).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidadeIds\":[3,4,5]}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(50)))
				.andExpect(jsonPath("$.masmorraNivel", is(1)))
				.andExpect(jsonPath("$.status", is("EM_ANDAMENTO")))
				.andExpect(jsonPath("$.turno", is(1)))
				.andExpect(jsonPath("$.turnoMaximo", is(30)))
				.andExpect(jsonPath("$.largura", is(8)))
				.andExpect(jsonPath("$.altura", is(8)))
				.andExpect(jsonPath("$.obstaculos", hasSize(8)))
				.andExpect(jsonPath("$.combatentes", hasSize(2)))
				.andExpect(jsonPath("$.combatentes[0].id", is("J1")))
				.andExpect(jsonPath("$.combatentes[0].lado", is("JOGADOR")))
				.andExpect(jsonPath("$.combatentes[0].unidadeId", is(200)))
				.andExpect(jsonPath("$.combatentes[0].tipo", is("SOLDADO")))
				.andExpect(jsonPath("$.combatentes[0].x", is(2)))
				.andExpect(jsonPath("$.combatentes[0].y", is(7)))
				.andExpect(jsonPath("$.combatentes[0].hp", is(30)))
				.andExpect(jsonPath("$.combatentes[0].hpMaximo", is(30)))
				.andExpect(jsonPath("$.combatentes[0].vivo", is(true)))
				.andExpect(jsonPath("$.combatentes[1].id", is("I1")))
				.andExpect(jsonPath("$.combatentes[1].lado", is("INIMIGO")))
				.andExpect(jsonPath("$.combatentes[1].unidadeId").doesNotExist())
				.andExpect(jsonPath("$.combatentes[1].tipo", is("GOBLIN")))
				.andExpect(jsonPath("$.log", hasSize(2)))
				.andExpect(jsonPath("$.loot").doesNotExist());
	}

	@Test
	void postIniciarComListaVaziaRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/masmorras/{nivel}/batalhas", 1).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidadeIds\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postIniciarComCincoUnidadesRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/masmorras/{nivel}/batalhas", 1).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidadeIds\":[1,2,3,4,5]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postIniciarMasmorraBloqueadaRecebe422() throws Exception {
		when(masmorraService.iniciar(anyLong(), anyInt(), any()))
				.thenThrow(new RegraJogoException(CodigoErro.MASMORRA_BLOQUEADA, "Masmorra de nível 2 não liberada."));

		mockMvc.perform(post("/api/jogo/masmorras/{nivel}/batalhas", 2).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidadeIds\":[3]}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.codigo", is("MASMORRA_BLOQUEADA")));
	}

	// -------------------------------------------------------- 200 ao consultar --

	@Test
	void getConsultarAutenticadoRetorna200ComBatalhaDto() throws Exception {
		when(masmorraService.consultar(1L, 50L)).thenReturn(batalhaEmAndamento());

		mockMvc.perform(get("/api/jogo/batalhas/{id}", 50).with(user("ana@exemplo.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(50)))
				.andExpect(jsonPath("$.status", is("EM_ANDAMENTO")))
				.andExpect(jsonPath("$.combatentes", hasSize(2)));
	}

	@Test
	void getConsultarInexistenteRecebe404() throws Exception {
		when(masmorraService.consultar(1L, 999L))
				.thenThrow(new RecursoNaoEncontradoException("Batalha não encontrada: 999"));

		mockMvc.perform(get("/api/jogo/batalhas/{id}", 999).with(user("ana@exemplo.com")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo", is("NAO_ENCONTRADO")));
	}

	@Test
	void getConsultarBatalhaDeOutraVilaRecebe404() throws Exception {
		when(masmorraService.consultar(1L, 77L))
				.thenThrow(new RecursoNaoEncontradoException("Batalha não encontrada: 77"));

		mockMvc.perform(get("/api/jogo/batalhas/{id}", 77).with(user("ana@exemplo.com")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo", is("NAO_ENCONTRADO")));
	}

	// -------------------------------------------------------------- ações --

	@Test
	void postAgirMoverComCsrfRetorna200ComEstadoAtualizado() throws Exception {
		when(masmorraService.agir(anyLong(), anyLong(), any())).thenReturn(batalhaAposMover());

		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"MOVER\",\"combatenteId\":\"J1\",\"x\":2,\"y\":4,\"turno\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.combatentes[0].x", is(2)))
				.andExpect(jsonPath("$.combatentes[0].y", is(4)))
				.andExpect(jsonPath("$.combatentes[0].moveu", is(true)));
	}

	@Test
	void postAgirComTurnoAntigoRecebe409() throws Exception {
		when(masmorraService.agir(anyLong(), anyLong(), any()))
				.thenThrow(new RegraJogoException(CodigoErro.TURNO_DESATUALIZADO,
						"Turno informado (1) não corresponde ao turno atual (2)."));

		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ATACAR\",\"combatenteId\":\"J1\",\"alvoId\":\"I1\",\"turno\":1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo", is("TURNO_DESATUALIZADO")));
	}

	@Test
	void postAgirAcaoInvalidaRecebe422() throws Exception {
		when(masmorraService.agir(anyLong(), anyLong(), any()))
				.thenThrow(new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Alvo fora de alcance."));

		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ATACAR\",\"combatenteId\":\"J1\",\"alvoId\":\"I1\",\"turno\":1}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.codigo", is("ACAO_INVALIDA")));
	}

	@Test
	void postAgirBatalhaEncerradaRecebe422() throws Exception {
		when(masmorraService.agir(anyLong(), anyLong(), any()))
				.thenThrow(new RegraJogoException(CodigoErro.BATALHA_ENCERRADA, "A batalha já terminou."));

		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ENCERRAR_TURNO\",\"turno\":5}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.codigo", is("BATALHA_ENCERRADA")));
	}

	@Test
	void postAgirBatalhaDeOutroUsuarioRecebe404() throws Exception {
		when(masmorraService.agir(anyLong(), anyLong(), any()))
				.thenThrow(new RecursoNaoEncontradoException("Batalha não encontrada: 50"));

		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ENCERRAR_TURNO\",\"turno\":1}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo", is("NAO_ENCONTRADO")));
	}

	@Test
	void postAgirComTipoInvalidoRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"VOAR\",\"combatenteId\":\"J1\",\"turno\":1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postAgirSemTurnoRecebe400() throws Exception {
		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ENCERRAR_TURNO\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	@Test
	void postAgirVitoriaRetorna200ComLootValido() throws Exception {
		when(masmorraService.agir(anyLong(), anyLong(), any())).thenReturn(batalhaVitoriaComLoot());

		mockMvc.perform(post("/api/jogo/batalhas/{id}/acoes", 50).with(user("ana@exemplo.com")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"ATACAR\",\"combatenteId\":\"J1\",\"alvoId\":\"I1\",\"turno\":3}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("VITORIA")))
				.andExpect(jsonPath("$.combatentes", hasSize(1)))
				.andExpect(jsonPath("$.loot.recursos.COMIDA", is(40)))
				.andExpect(jsonPath("$.loot.recursos.FERRO", is(20)))
				.andExpect(jsonPath("$.loot.sementes.MILHO", is(1)))
				.andExpect(jsonPath("$.loot.itens", hasSize(1)))
				.andExpect(jsonPath("$.loot.itens[0].modelo", is("ESPADA")))
				.andExpect(jsonPath("$.loot.itens[0].nivel", is(2)));
	}

	// -------------------------------------------------------------- dados --

	private Batalha batalhaEmAndamento() {
		EstadoBatalha estado = new EstadoBatalha(
				List.of(combatenteJogador(), combatenteInimigo()),
				1, 30, MapaMasmorra.PADRAO, EstadoBatalha.Resultado.NULO,
				List.of("Batalha iniciada na masmorra nível 1.", "Turno 1 começou."));
		return batalhaComEstado(50L, StatusBatalha.EM_ANDAMENTO, estado, null);
	}

	private Batalha batalhaAposMover() {
		Combatente movido = combatenteJogador().moverPara(new Posicao(2, 4));
		EstadoBatalha estado = new EstadoBatalha(
				List.of(movido, combatenteInimigo()),
				1, 30, MapaMasmorra.PADRAO, EstadoBatalha.Resultado.NULO,
				List.of("Batalha iniciada na masmorra nível 1.", "Turno 1 começou.", "J1 moveu para (2,4)."));
		return batalhaComEstado(50L, StatusBatalha.EM_ANDAMENTO, estado, null);
	}

	private Batalha batalhaVitoriaComLoot() {
		EstadoBatalha estado = new EstadoBatalha(
				List.of(combatenteJogador()),
				3, 30, MapaMasmorra.PADRAO, EstadoBatalha.Resultado.VITORIA,
				List.of("Batalha iniciada na masmorra nível 1.", "I1 morreu.", "Vitória!"));
		return batalhaComEstado(50L, StatusBatalha.VITORIA, estado, serializarLootValido());
	}

	private Batalha batalhaComEstado(long id, StatusBatalha status, EstadoBatalha estado, String lootJson) {
		Batalha batalha = new Batalha();
		batalha.setId(id);
		batalha.setVilaId(10L);
		batalha.setMasmorraNivel(1);
		batalha.setStatus(status);
		batalha.setTurno(estado.turno());
		batalha.setEstado(OBJECT_MAPPER.writeValueAsString(estado));
		batalha.setLog(String.join("\n", estado.log()));
		batalha.setLoot(lootJson);
		return batalha;
	}

	private Combatente combatenteJogador() {
		return new Combatente("J1", Lado.JOGADOR, "SOLDADO:200", new Posicao(2, 7), 30, 30, 8, 3, 1, 3, false, false,
				false, true);
	}

	private Combatente combatenteInimigo() {
		return new Combatente("I1", Lado.INIMIGO, "GOBLIN", new Posicao(3, 0), 15, 15, 5, 1, 1, 3, false, false, false,
				true);
	}

	/** JSON de um {@link Loot} de vitória no nível 1 (ver spec game-dungeon-loot). */
	private String serializarLootValido() {
		Map<TipoRecurso, Long> recursos = new EnumMap<>(TipoRecurso.class);
		recursos.put(TipoRecurso.COMIDA, 40_000L);
		recursos.put(TipoRecurso.MADEIRA, 50_000L);
		recursos.put(TipoRecurso.PEDRA, 50_000L);
		recursos.put(TipoRecurso.FERRO, 20_000L);

		Map<Cultivo, Integer> sementes = new EnumMap<>(Cultivo.class);
		sementes.put(Cultivo.MILHO, 1);
		sementes.put(Cultivo.BATATA, 0);
		sementes.put(Cultivo.ABOBORA_DOURADA, 0);

		Item item = new Item();
		item.setId(500L);
		item.setModelo(ModeloItem.ESPADA);
		item.setNivel(2);
		item.setOrigem(OrigemItem.MASMORRA);
		item.setStatus(StatusItem.DISPONIVEL);

		Loot loot = new Loot(recursos, sementes, List.of(item));
		return OBJECT_MAPPER.writeValueAsString(loot);
	}

}
