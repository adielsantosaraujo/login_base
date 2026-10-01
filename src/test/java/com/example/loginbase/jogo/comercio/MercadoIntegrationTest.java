package com.example.loginbase.jogo.comercio;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MercadoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired MercadoService mercadoService;
	@Autowired EventoTurnoRepository eventoRepository;

	private Usuario usuario;
	private Vila vila;
	private Familia familia;
	private int seq;

	private void novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		usuario = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		estoqueService.inicializar(vila, Map.of());
	}

	private Construcao mercado(EstadoConstrucao estado) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.MERCADO);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	/** Aloca um Comerciante com o PE base informado (características zeradas: PE efetivo = pe). */
	private void comerciante(Construcao mercado, int pe) {
		Cidadao c = cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, 30 * 12));
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.COMERCIANTE, pe));
		c.setConstrucaoId(mercado.getId());
		c.setProfissaoTrabalho(Profissao.COMERCIANTE);
		cidadaoRepository.saveAndFlush(c);
	}

	private ResultActions ordem(String recurso, String tipo, int qtd) throws Exception {
		return mvc.perform(post("/api/jogo/mercado/ordens").with(user(usuario.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"recurso\":\"%s\",\"tipo\":\"%s\",\"quantidade\":%d}".formatted(recurso, tipo, qtd)));
	}

	private BigDecimal qtd(Recurso r) {
		return estoqueService.quantidade(vila, r);
	}

	@Test
	void vendeMadeiraComPeAlto() throws Exception {
		novaVila();
		comerciante(mercado(EstadoConstrucao.ATIVA), 20);
		estoqueService.creditar(vila, Recurso.MADEIRA, BigDecimal.valueOf(10));
		ordem("MADEIRA", "VENDA", 10).andExpect(status().isOk()).andExpect(jsonPath("$.sucesso").value(true))
				.andExpect(jsonPath("$.precoUnitario").value(0.9)).andExpect(jsonPath("$.ouroNovo").value(9.0))
				.andExpect(jsonPath("$.recursoNovo").value(0.0));
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("9");
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
	}

	@Test
	void compraAcoComPeBaixo() throws Exception {
		novaVila();
		comerciante(mercado(EstadoConstrucao.ATIVA), 0);
		estoqueService.creditar(vila, Recurso.OURO, BigDecimal.valueOf(100));
		ordem("ACO", "COMPRA", 3).andExpect(status().isOk()).andExpect(jsonPath("$.precoUnitario").value(30.0))
				.andExpect(jsonPath("$.ouroTotal").value(90.0));
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("10");
		assertThat(qtd(Recurso.ACO)).isEqualByComparingTo("3");
	}

	@Test
	void vendaSemRecursoECompraSemOuro() throws Exception {
		novaVila();
		comerciante(mercado(EstadoConstrucao.ATIVA), 20);
		ordem("MADEIRA", "VENDA", 1).andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("Recurso insuficiente"));
		ordem("MADEIRA", "COMPRA", 1).andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("Ouro insuficiente"));
	}

	@Test
	void volumeLimitadoESomaMercadosECompraEVenda() throws Exception {
		novaVila();
		Construcao m1 = mercado(EstadoConstrucao.ATIVA);
		comerciante(m1, 0); // eficiência 0,5 -> volume 10
		estoqueService.creditar(vila, Recurso.MADEIRA, BigDecimal.valueOf(100));
		estoqueService.creditar(vila, Recurso.OURO, BigDecimal.valueOf(100));
		ordem("MADEIRA", "VENDA", 11).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Volume diário excedido"));
		ordem("MADEIRA", "VENDA", 6).andExpect(status().isOk());
		ordem("MADEIRA", "COMPRA", 5).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Volume diário excedido"));
		ordem("MADEIRA", "COMPRA", 4).andExpect(status().isOk()).andExpect(jsonPath("$.volumeUsado").value(10));
		// segundo Mercado soma volume
		comerciante(mercado(EstadoConstrucao.ATIVA), 0);
		ordem("MADEIRA", "COMPRA", 10).andExpect(status().isOk());
		assertThat(mercadoService.precos(vila).volumeMaximo()).isEqualTo(20);
	}

	@Test
	void semMercadoAtivoOuRecursoInvalido() throws Exception {
		novaVila();
		ordem("MADEIRA", "VENDA", 1).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Mercado não disponível"));
		comerciante(mercado(EstadoConstrucao.EM_OBRA), 10);
		ordem("MADEIRA", "VENDA", 1).andExpect(status().isBadRequest());
		comerciante(mercado(EstadoConstrucao.ATIVA), 10);
		ordem("OURO", "VENDA", 1).andExpect(status().isBadRequest());
		ordem("XYZ", "VENDA", 1).andExpect(status().isBadRequest());
		ordem("MADEIRA", "TROCA", 1).andExpect(status().isBadRequest());
		ordem("MADEIRA", "VENDA", 0).andExpect(status().isBadRequest());
	}

	@Test
	void registraEventoEOrdem() throws Exception {
		novaVila();
		comerciante(mercado(EstadoConstrucao.ATIVA), 20);
		estoqueService.creditar(vila, Recurso.MADEIRA, BigDecimal.valueOf(5));
		ordem("MADEIRA", "VENDA", 5).andExpect(status().isOk());
		assertThat(eventoRepository.findAll()).anyMatch(e -> e.getVilaId().equals(vila.getId())
				&& e.getTipo() == TipoEventoTurno.MERCADO_VENDA);
		assertThat(mercadoService.historico(vila)).hasSize(1);
	}

	@Test
	void compraRespeitaCapacidade() throws Exception {
		novaVila();
		comerciante(mercado(EstadoConstrucao.ATIVA), 100);
		estoqueService.creditar(vila, Recurso.MADEIRA, BigDecimal.valueOf(499));
		estoqueService.creditar(vila, Recurso.OURO, BigDecimal.valueOf(100));
		ordem("MADEIRA", "COMPRA", 2).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Capacidade de armazenamento excedida"));
	}

	@Test
	void getPrecosSemMercadoEComMercado() throws Exception {
		novaVila();
		mvc.perform(get("/api/jogo/mercado/precos").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.mercadoAtivo").value(false));
		comerciante(mercado(EstadoConstrucao.ATIVA), 20);
		mvc.perform(get("/api/jogo/mercado/precos").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.mercadoAtivo").value(true))
				.andExpect(jsonPath("$.precos[0].recurso").exists())
				.andExpect(jsonPath("$.precos[0].precoVenda").exists())
				.andExpect(jsonPath("$.volumeMaximo").isNumber());
	}

	@Test
	void getOrdensRetornaHistoricoMaisRecentePrimeiro() throws Exception {
		novaVila();
		comerciante(mercado(EstadoConstrucao.ATIVA), 20);
		estoqueService.creditar(vila, Recurso.MADEIRA, BigDecimal.valueOf(5));
		estoqueService.creditar(vila, Recurso.OURO, BigDecimal.valueOf(100));
		ordem("MADEIRA", "VENDA", 2).andExpect(status().isOk());
		ordem("MADEIRA", "COMPRA", 1).andExpect(status().isOk());
		mvc.perform(get("/api/jogo/mercado/ordens").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].tipo").value("COMPRA")).andExpect(jsonPath("$[0].recurso").value("MADEIRA"))
				.andExpect(jsonPath("$[1].tipo").value("VENDA")).andExpect(jsonPath("$[1].quantidade").value(2))
				.andExpect(jsonPath("$[0].ouroTotal").exists());
	}

}
