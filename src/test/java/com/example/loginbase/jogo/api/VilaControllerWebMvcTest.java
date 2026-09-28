package com.example.loginbase.jogo.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.BatalhaRepository;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.EstadoVila;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.seguranca.RegistroSessaoSuccessHandler;
import com.example.loginbase.seguranca.SecurityConfig;
import com.example.loginbase.seguranca.SessaoService;

/**
 * Testes de fatia web (Task 6.1) de {@link VilaController}: segurança
 * (anônimo 401), respostas 200 com o schema de {@link VilaDto}/
 * {@link CatalogoDto}, e mapeamento de erros feito por {@link ErroApiHandler}
 * (422/409/404/400).
 */
@WebMvcTest(VilaController.class)
@Import({ SecurityConfig.class, RegistroSessaoSuccessHandler.class, ErroApiHandler.class, UsuarioAtual.class,
		JogoMapper.class, JogoProperties.class })
class VilaControllerWebMvcTest {

	private static final Instant AGORA = Instant.parse("2026-01-01T12:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessaoService sessaoService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

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
	}

	@Test
	void getVilaAnonimoRecebe401() throws Exception {
		mockMvc.perform(get("/api/jogo/vila"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getCatalogoAnonimoRecebe401() throws Exception {
		mockMvc.perform(get("/api/jogo/catalogo"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getVilaAutenticadoRetorna200ComEstadoDaVila() throws Exception {
		when(vilaService.consultar(1L)).thenReturn(estadoVilaValido());

		mockMvc.perform(get("/api/jogo/vila").with(user("ana@exemplo.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Vila de Ana")))
				.andExpect(jsonPath("$.recursos.COMIDA", is(300)))
				.andExpect(jsonPath("$.recursos.MADEIRA", is(400)))
				.andExpect(jsonPath("$.capacidade.COMIDA", is(8000)))
				.andExpect(jsonPath("$.producaoPorHora.MADEIRA", is(30)))
				.andExpect(jsonPath("$.masmorraNivelLiberado", is(1)))
				.andExpect(jsonPath("$.batalhaAtivaId").doesNotExist())
				.andExpect(jsonPath("$.predios", org.hamcrest.Matchers.hasSize(2)))
				.andExpect(jsonPath("$.predios[0].tipo", is("ARMAZEM")))
				.andExpect(jsonPath("$.predios[0].nivel", is(5)))
				.andExpect(jsonPath("$.predios[0].proximoNivel").doesNotExist())
				.andExpect(jsonPath("$.predios[1].tipo", is("SERRARIA")))
				.andExpect(jsonPath("$.predios[1].proximoNivel.nivel", is(2)))
				.andExpect(jsonPath("$.predios[1].ordemEmAndamento.tempoRestanteSegundos", is(1800)))
				.andExpect(jsonPath("$.canteiros[0].producaoComidaPorHora", is(20)))
				.andExpect(jsonPath("$.sementes.TRIGO", is(2)))
				.andExpect(jsonPath("$.itens[0].ataque", is(6)))
				.andExpect(jsonPath("$.unidades[0].nome", is("Ana")))
				.andExpect(jsonPath("$.unidades[0].sobrenome", is("Silva")))
				.andExpect(jsonPath("$.unidades[0].ordinalNome", is(2)))
				.andExpect(jsonPath("$.unidades[0].nomeExibicao", is("Ana Silva (2)")))
				.andExpect(jsonPath("$.unidades[0].tipo", is("SOLDADO")))
				.andExpect(jsonPath("$.unidades[0].status", is("DISPONIVEL")))
				.andExpect(jsonPath("$.unidades[0].hp", is(30)))
				.andExpect(jsonPath("$.unidades[0].ataque", is(6)))
				.andExpect(jsonPath("$.unidades[0].defesa", is(3)))
				.andExpect(jsonPath("$.unidades[0].alcance", is(1)))
				.andExpect(jsonPath("$.unidades[0].movimento", is(3)))
				.andExpect(jsonPath("$.unidades[0].equipamento", org.hamcrest.Matchers.aMapWithSize(9)))
				.andExpect(jsonPath("$.unidades[0].equipamento.ARMA.modelo", is("ESPADA")))
				.andExpect(jsonPath("$.unidades[0].equipamento.ARMADURA.modelo", is("ARMADURA_COURO")))
				.andExpect(jsonPath("$.unidades[0].equipamento.CABECA").doesNotExist())
				.andExpect(jsonPath("$.unidades[0].equipamento.BOTA").doesNotExist())
				.andExpect(jsonPath("$.unidades[0].equipamento.LUVA").doesNotExist())
				.andExpect(jsonPath("$.unidades[0].equipamento.COLAR").doesNotExist())
				.andExpect(jsonPath("$.unidades[0].equipamento.ANEL_1").doesNotExist())
				.andExpect(jsonPath("$.unidades[0].equipamento.ANEL_2").doesNotExist())
				.andExpect(jsonPath("$.unidades[0].equipamento.ANEL_3").doesNotExist())
				.andExpect(jsonPath("$.capacidadeExercito", is(0)))
				.andExpect(jsonPath("$.ordens[0].categoria", is("CONSTRUCAO")));
	}

	@Test
	void equipamentoDaUnidadeVemEmOrdemDosSlots() throws Exception {
		when(vilaService.consultar(1L)).thenReturn(estadoVilaValido());

		String corpo = mockMvc.perform(get("/api/jogo/vila").with(user("ana@exemplo.com")))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		String equipamento = corpo.substring(corpo.indexOf("\"equipamento\""));
		List<String> ordemEsperada = List.of("ARMA", "ARMADURA", "CABECA", "BOTA", "LUVA", "COLAR", "ANEL_1",
				"ANEL_2", "ANEL_3");
		int posicaoAnterior = -1;
		for (String slot : ordemEsperada) {
			int posicao = equipamento.indexOf("\"" + slot + "\"");
			org.assertj.core.api.Assertions.assertThat(posicao).isGreaterThan(posicaoAnterior);
			posicaoAnterior = posicao;
		}
	}

	@Test
	void getCatalogoAutenticadoRetorna200() throws Exception {
		mockMvc.perform(get("/api/jogo/catalogo").with(user("ana@exemplo.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.predios.SERRARIA", org.hamcrest.Matchers.hasSize(5)))
				.andExpect(jsonPath("$.cultivos.TRIGO.comidaPorHora", is(20)))
				.andExpect(jsonPath("$.modelosItem.ESPADA.tempoBaseSegundos", is(60)))
				.andExpect(jsonPath("$.tropas.SOLDADO.hp", is(30)))
				.andExpect(jsonPath("$.inimigos.GOBLIN.hp", is(15)))
				.andExpect(jsonPath("$.masmorras['1'].composicaoInimigos", org.hamcrest.Matchers.hasSize(3)));
	}

	@Test
	void regraJogoExceptionRecursosInsuficientesRecebe422ComCodigo() throws Exception {
		when(vilaService.consultar(anyLong()))
				.thenThrow(new RegraJogoException(CodigoErro.RECURSOS_INSUFICIENTES, "Recursos insuficientes."));

		mockMvc.perform(get("/api/jogo/vila").with(user("ana@exemplo.com")))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.codigo", is("RECURSOS_INSUFICIENTES")))
				.andExpect(jsonPath("$.mensagem", is("Recursos insuficientes.")));
	}

	@Test
	void regraJogoExceptionTurnoDesatualizadoRecebe409() throws Exception {
		when(vilaService.consultar(anyLong()))
				.thenThrow(new RegraJogoException(CodigoErro.TURNO_DESATUALIZADO, "Turno desatualizado."));

		mockMvc.perform(get("/api/jogo/vila").with(user("ana@exemplo.com")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo", is("TURNO_DESATUALIZADO")));
	}

	@Test
	void recursoNaoEncontradoExceptionRecebe404() throws Exception {
		when(vilaService.consultar(anyLong())).thenThrow(new RecursoNaoEncontradoException("Vila não encontrada."));

		mockMvc.perform(get("/api/jogo/vila").with(user("ana@exemplo.com")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo", is("NAO_ENCONTRADO")));
	}

	@Test
	void enumInvalidoRecebe400() throws Exception {
		when(vilaService.consultar(anyLong()))
				.thenThrow(new HttpMessageNotReadableException("Enum inválido", (HttpInputMessage) null));

		mockMvc.perform(get("/api/jogo/vila").with(user("ana@exemplo.com")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo", is("REQUISICAO_INVALIDA")));
	}

	// -------------------------------------------------------------- dados --

	private EstadoVila estadoVilaValido() {
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

		Predio armazem = new Predio();
		armazem.setId(1L);
		armazem.setVilaId(10L);
		armazem.setTipo(TipoPredio.ARMAZEM);
		armazem.setNivel(TipoPredio.NIVEL_MAXIMO);

		Predio serraria = new Predio();
		serraria.setId(2L);
		serraria.setVilaId(10L);
		serraria.setTipo(TipoPredio.SERRARIA);
		serraria.setNivel(1);

		Canteiro canteiro = new Canteiro();
		canteiro.setId(1L);
		canteiro.setVilaId(10L);
		canteiro.setPosicao(1);
		canteiro.setCultivo(Cultivo.TRIGO);
		canteiro.setPlantadoEm(AGORA);

		Item espada = new Item();
		espada.setId(100L);
		espada.setVilaId(10L);
		espada.setModelo(ModeloItem.ESPADA);
		espada.setNivel(1);
		espada.setOrigem(OrigemItem.FORJA);
		espada.setStatus(StatusItem.EQUIPADO);

		Item armaduraCouro = new Item();
		armaduraCouro.setId(101L);
		armaduraCouro.setVilaId(10L);
		armaduraCouro.setModelo(ModeloItem.ARMADURA_COURO);
		armaduraCouro.setNivel(1);
		armaduraCouro.setOrigem(OrigemItem.FORJA);
		armaduraCouro.setStatus(StatusItem.EQUIPADO);

		Unidade soldado = new Unidade();
		soldado.setId(200L);
		soldado.setVilaId(10L);
		soldado.setTipo(TipoTropa.SOLDADO);
		soldado.setArmaItemId(100L);
		soldado.setArmaduraItemId(101L);
		soldado.setStatus(StatusUnidade.DISPONIVEL);
		soldado.setNome("Ana");
		soldado.setSobrenome("Silva");
		soldado.setOrdinalNome(2);

		Ordem ordemConstrucao = new Ordem();
		ordemConstrucao.setId(300L);
		ordemConstrucao.setVilaId(10L);
		ordemConstrucao.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordemConstrucao.setAlvo(TipoPredio.SERRARIA.name());
		ordemConstrucao.setNivel(2);
		ordemConstrucao.setQuantidade(1);
		ordemConstrucao.setIniciadaEm(AGORA);
		ordemConstrucao.setConcluiEm(AGORA.plusSeconds(1800));

		EstoqueSemente sementeTrigo = new EstoqueSemente();
		sementeTrigo.setId(1L);
		sementeTrigo.setVilaId(10L);
		sementeTrigo.setCultivo(Cultivo.TRIGO);
		sementeTrigo.setQuantidade(2);

		return new EstadoVila(vila, List.of(armazem, serraria), List.of(canteiro), List.of(sementeTrigo),
				List.of(espada, armaduraCouro), List.of(soldado), List.of(ordemConstrucao));
	}

}
