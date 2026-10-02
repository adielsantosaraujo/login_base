package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FabricacaoApiIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired EstoqueService estoque;
	@Autowired FabricacaoRepository fabricacaoRepository;
	@Autowired PeArtesao peArtesao;

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
	}

	private Construcao oficina(TipoConstrucao tipo, NivelConstrucao nivel, EstadoConstrucao estado) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	/** Artesão alocado na oficina com PE efetivo exatamente igual ao informado. */
	private Cidadao artesao(Construcao of, Profissao prof, int pe) {
		Cidadao c = cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "A" + (++seq), Sexo.M, 30 * 12));
		c.setConstrucaoId(of.getId());
		c.setProfissaoTrabalho(prof);
		cidadaoRepository.saveAndFlush(c);
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), prof, 0));
		int bonus = peArtesao.peEfetivo(c, prof);
		CidadaoProfissao cp = cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(c.getId(), prof).orElseThrow();
		cp.setPontosBase(pe - bonus);
		cidadaoProfissaoRepository.saveAndFlush(cp);
		assertThat(peArtesao.peEfetivo(c, prof)).isEqualTo(pe);
		return c;
	}

	private void estoque(Recurso r, int q) {
		estoque.creditar(vila, r, BigDecimal.valueOf(q));
	}

	private ResultActions criar(Long oficinaId, String corpo) throws Exception {
		return mvc.perform(post("/api/jogo/oficinas/" + oficinaId + "/fabricacoes")
				.with(user(usuario.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private static String body(String subtipo, int nivel, Long artesao, String atributo) {
		return "{\"subtipo\":\"%s\",\"nivel\":%d,\"artesaoId\":%d%s}".formatted(subtipo, nivel, artesao,
				atributo == null ? "" : ",\"atributoEscolhido\":\"" + atributo + "\"");
	}

	private ResultActions consultar(String url) throws Exception {
		return mvc.perform(get(url).with(user(usuario.getEmail()).roles("USER")));
	}

	@Test
	void criaFabricacaoDebitaEstoqueEListaFila() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		estoque(Recurso.FERRO, 20);
		estoque(Recurso.TABUA, 20);
		long antes = estoque.quantidade(vila, Recurso.FERRO).longValue();
		criar(f.getId(), body("ESPADA", 3, a.getId(), null)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.subtipo").value("ESPADA")).andExpect(jsonPath("$.pfTotal").value(4))
				.andExpect(jsonPath("$.estado").value("EM_ANDAMENTO")).andExpect(jsonPath("$.artesaoId").value(a.getId()))
				.andExpect(jsonPath("$.turnosEstimados").isNumber());
		Fabricacao fab = fabricacaoRepository.findByArtesaoId(a.getId()).orElseThrow();
		assertThat(fab.getPfAtual().signum()).isZero();
		assertThat(antes - estoque.quantidade(vila, Recurso.FERRO).longValue()).isPositive();
		consultar("/api/jogo/oficinas/" + f.getId() + "/fila").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].artesaoNome").value(a.getNome())).andExpect(jsonPath("$[0].pfTotal").value(4));
		consultar("/api/jogo/oficinas/" + f.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.nivelMaximoItem").value(3))
				.andExpect(jsonPath("$.tipo").value("FERRARIA"))
				.andExpect(jsonPath("$.artesaos[0].ocupado").value(true))
				.andExpect(jsonPath("$.artesaos[0].peEfetivo").value(10));
		// artesão ocupado
		criar(f.getId(), body("ESPADA", 1, a.getId(), null)).andExpect(status().isBadRequest());
	}

	@Test
	void espadaNivel6CustaAcoETabua() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N2, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		estoque(Recurso.ACO, 30);
		estoque(Recurso.TABUA, 10);
		criar(f.getId(), body("ESPADA", 6, a.getId(), null)).andExpect(status().isCreated());
		assertThat(estoque.quantidade(vila, Recurso.ACO).intValue()).isEqualTo(12);
		assertThat(estoque.quantidade(vila, Recurso.TABUA).intValue()).isEqualTo(4);
	}

	@Test
	void mensagensDeValidacao() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Construcao carp = oficina(TipoConstrucao.CARPINTARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		Cidadao fraco = artesao(f, Profissao.FERREIRO, 1);
		Cidadao outra = artesao(carp, Profissao.MADEIREIRO, 10);
		criar(f.getId(), body("ARCO", 1, a.getId(), null)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Oficina incorreta. Arco é fabricada na Carpintaria."));
		criar(f.getId(), body("ESPADA", 0, a.getId(), null)).andExpect(status().isBadRequest());
		criar(f.getId(), body("ESPADA", 11, a.getId(), null)).andExpect(status().isBadRequest());
		criar(f.getId(), body("ESPADA", 4, a.getId(), null)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Nível máximo L3 para esta oficina"));
		criar(f.getId(), body("ESPADA", 1, outra.getId(), null)).andExpect(status().isBadRequest());
		criar(f.getId(), body("ESPADA", 3, fraco.getId(), null)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("PE efetivo mínimo 4 necessário"));
		Cidadao morto = artesao(f, Profissao.FERREIRO, 10);
		morto.setVivo(false);
		cidadaoRepository.saveAndFlush(morto);
		criar(f.getId(), body("ESPADA", 1, morto.getId(), null)).andExpect(status().isBadRequest());
		// atributo em item que não aceita
		criar(f.getId(), body("ESPADA", 1, a.getId(), "FOR")).andExpect(status().isBadRequest());
		// recursos insuficientes
		estoque(Recurso.FERRO, 1);
		criar(f.getId(), body("ESPADA", 3, a.getId(), null)).andExpect(status().isConflict());
		// construção de outra vila / inexistente
		criar(999999L, body("ESPADA", 1, a.getId(), null)).andExpect(status().isNotFound());
	}

	@Test
	void peMinimoDez() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N3, EstadoConstrucao.ATIVA);
		Cidadao fraco = artesao(f, Profissao.FERREIRO, 5);
		criar(f.getId(), body("ESPADA", 6, fraco.getId(), null)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("PE efetivo mínimo 10 necessário"));
	}

	@Test
	void oficinaDeveEstarAtivaEMensagemDeRecurso() throws Exception {
		novaVila();
		Construcao obra = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA);
		criar(obra.getId(), body("ESPADA", 1, 1L, null)).andExpect(status().isBadRequest());
		Construcao casa = oficina(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		criar(casa.getId(), body("ESPADA", 1, 1L, null)).andExpect(status().isBadRequest());
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N2, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		estoque(Recurso.FERRO, 5);
		estoque(Recurso.TABUA, 100);
		criar(f.getId(), body("ESPADA", 3, a.getId(), null)).andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("insuficiente (")));
	}

	@Test
	void anelExigeAtributo() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		estoque(Recurso.FERRO, 10);
		estoque(Recurso.OURO, 100);
		criar(f.getId(), body("ANEL", 1, a.getId(), null)).andExpect(status().isBadRequest());
		criar(f.getId(), body("ANEL", 1, a.getId(), "ATK")).andExpect(status().isBadRequest());
		criar(f.getId(), body("ANEL", 1, a.getId(), "FOR")).andExpect(status().isCreated());
		assertThat(fabricacaoRepository.findByArtesaoId(a.getId()).orElseThrow().getAtributoEscolhido())
				.isEqualTo(CodigoBonus.FOR);
	}

	@Test
	void receitasListamCustoPorNivelAteMaximo() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N2, EstadoConstrucao.ATIVA);
		consultar("/api/jogo/oficinas/" + f.getId() + "/receitas").andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.subtipo=='ESPADA')].custoPorNivel['6'].ACO").value(18))
				.andExpect(jsonPath("$[?(@.subtipo=='ESPADA')].custoPorNivel['7']").isEmpty())
				.andExpect(jsonPath("$[?(@.subtipo=='ESPADA')].peMinimoPorNivel['6']").value(10))
				.andExpect(jsonPath("$[?(@.subtipo=='ANEL')].exigeAtributo").value(true))
				.andExpect(jsonPath("$[?(@.subtipo=='ESPADA')].exigeAtributo").value(false));
	}

	@Test
	void reatribuiArtesao() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N2, EstadoConstrucao.ATIVA);
		Construcao outraOf = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N2, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		Cidadao b = artesao(f, Profissao.FERREIRO, 10);
		Cidadao fraco = artesao(f, Profissao.FERREIRO, 1);
		Cidadao fora = artesao(outraOf, Profissao.FERREIRO, 10);
		estoque(Recurso.FERRO, 100);
		estoque(Recurso.TABUA, 100);
		long id = fabricar(f, a);
		Fabricacao fab = fabricacaoRepository.findById(id).orElseThrow();
		fab.setPfAtual(new BigDecimal("1.5"));
		fab.setEstado(EstadoFabricacao.PAUSADA);
		fabricacaoRepository.saveAndFlush(fab);
		reatribuir(id, fraco.getId()).andExpect(status().isBadRequest());
		reatribuir(id, fora.getId()).andExpect(status().isBadRequest());
		reatribuir(id, b.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.artesaoId").value(b.getId()))
				.andExpect(jsonPath("$.estado").value("EM_ANDAMENTO")).andExpect(jsonPath("$.pfAtual").value(1.5));
		assertThat(fabricacaoRepository.findById(id).orElseThrow().getArtesaoId()).isEqualTo(b.getId());
	}

	private long fabricar(Construcao f, Cidadao a) throws Exception {
		criar(f.getId(), body("ESPADA", 3, a.getId(), null)).andExpect(status().isCreated());
		return fabricacaoRepository.findByArtesaoId(a.getId()).orElseThrow().getId();
	}

	private ResultActions reatribuir(long id, Long artesaoId) throws Exception {
		return mvc.perform(patch("/api/jogo/fabricacoes/" + id).with(user(usuario.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"artesaoId\":" + artesaoId + "}"));
	}
}
