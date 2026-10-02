package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.batalha.BatalhaRepository;
import com.example.loginbase.jogo.batalha.Combatente;
import com.example.loginbase.jogo.batalha.FonteMasmorras;
import com.example.loginbase.jogo.batalha.LadoCombate;
import com.example.loginbase.jogo.batalha.LinhaCombate;
import com.example.loginbase.jogo.batalha.MasmorraAlvo;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.item.catalogo.Alcance;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EtapaTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExpedicaoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired TropaRepository tropaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired BatalhaRepository batalhaRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired List<EtapaTurno> etapas;
	@MockitoBean FonteMasmorras fonte;

	private Usuario usuario;
	private Vila vila;
	private Familia familia;
	private Construcao quartel;
	private int seq;

	private void cenario(String refeicao) {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		usuario = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		Regiao r = new Regiao(vila.getId(), 1);
		r.setPossuida(true);
		regiaoRepository.saveAndFlush(r);
		estoqueService.inicializar(vila, Map.of(Recurso.REFEICAO, new BigDecimal(refeicao)));
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.QUARTEL);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(0);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(8);
		c.setPoAtual(8);
		quartel = construcaoRepository.saveAndFlush(c);
	}

	private Tropa tropa(int membros) {
		Tropa t = tropaRepository.saveAndFlush(new Tropa(vila.getId(), quartel.getId(), "T" + (++seq)));
		for (int i = 0; i < membros; i++) {
			Cidadao c = new Cidadao(vila.getId(), familia.getId(), "G" + (++seq), Sexo.M, 20 * 12);
			c.setVit(5);
			c.setForca(5);
			c.setVel(5);
			c.setTropaId(t.getId());
			c.setPosicaoTropa(PosicaoTropa.FRENTE);
			c = cidadaoRepository.saveAndFlush(c);
			profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.GUERREIRO, 5));
			Item espada = new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.SIMPLES, 1, List.of());
			espada.setCidadaoId(c.getId());
			espada.setSlot(SlotEquipamento.ARMA);
			itemRepository.saveAndFlush(espada);
		}
		return t;
	}

	private Combatente fraco() {
		return new Combatente(0, LadoCombate.INIMIGO, "Inimigo", LinhaCombate.FRENTE, 5, 0, 0, 1, 0, 0,
				Alcance.CORPO_A_CORPO_FRENTE, false);
	}

	private void masmorra(long id, int regiao, int nivel) {
		MasmorraAlvo m = new MasmorraAlvo(id, regiao, nivel);
		when(fonte.listarAtivas(vila.getId())).thenReturn(List.of(m));
		when(fonte.buscarAtiva(vila.getId(), id)).thenReturn(Optional.of(m));
		when(fonte.gerarInimigos(eq(nivel), anyLong())).thenReturn(List.of(fraco()));
	}

	private ResultActions enviar(Long tropaId, Long masmorraId) throws Exception {
		return mvc.perform(post("/api/jogo/tropas/" + tropaId + "/expedicao").with(user(usuario.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"masmorraId\":" + masmorraId + "}"));
	}

	private ResultActions obter(String url) throws Exception {
		return mvc.perform(get(url).with(user(usuario.getEmail()).roles("USER")));
	}

	private void turno(int ordem, int numero) {
		etapas.stream().filter(e -> e.ordem() == ordem).findFirst().orElseThrow().executar(vila, numero);
	}

	private Tropa relida(Tropa t) {
		return tropaRepository.findById(t.getId()).orElseThrow();
	}

	private long eventos(TipoEventoTurno tipo) {
		return eventoRepository.findAll().stream().filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == tipo)
				.count();
	}

	@Test
	void destinosListaMasmorrasComCustoEComida() throws Exception {
		cenario("30");
		Tropa t = tropa(3);
		masmorra(77L, 3, 2); // região 3 -> distância 2 até a região 1
		obter("/api/jogo/tropas/" + t.getId() + "/destinos").andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].masmorraId").value(77))
				.andExpect(jsonPath("$[0].regiao").value(3)).andExpect(jsonPath("$[0].nivel").value(2))
				.andExpect(jsonPath("$[0].turnosViagem").value(2))
				.andExpect(jsonPath("$[0].comidaNecessaria").value(12))
				.andExpect(jsonPath("$[0].comidaDisponivel").value(30));
	}

	@Test
	void enviarOkDebitaComidaEMarcaViagem() throws Exception {
		cenario("30");
		Tropa t = tropa(3);
		masmorra(77L, 3, 2);
		enviar(t.getId(), 77L).andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("EM_VIAGEM_IDA"))
				.andExpect(jsonPath("$.masmorraId").value(77)).andExpect(jsonPath("$.regiaoDestino").value(3))
				.andExpect(jsonPath("$.turnosViagem").value(2)).andExpect(jsonPath("$.turnosRestantes").value(2));
		assertThat(estoqueService.quantidade(vila, Recurso.REFEICAO)).isEqualByComparingTo("18");
		assertThat(eventos(TipoEventoTurno.EXPEDICAO_PARTIU)).isEqualTo(1);
	}

	@Test
	void masmorraNaPropriaRegiaoViajaUmTurno() throws Exception {
		cenario("30");
		Tropa t = tropa(1);
		masmorra(5L, 1, 1);
		enviar(t.getId(), 5L).andExpect(status().isCreated()).andExpect(jsonPath("$.turnosViagem").value(1));
		assertThat(estoqueService.quantidade(vila, Recurso.REFEICAO)).isEqualByComparingTo("28");
	}

	@Test
	void debitaRefeicaoGraosCarneNaOrdem() throws Exception {
		cenario("4");
		estoqueService.inicializar(vila, Map.of(Recurso.REFEICAO, new BigDecimal("4"), Recurso.GRAOS,
				new BigDecimal("3"), Recurso.CARNE, new BigDecimal("10")));
		Tropa t = tropa(4); // 4 x 2 x 1 = 8
		masmorra(5L, 1, 1);
		enviar(t.getId(), 5L).andExpect(status().isCreated());
		assertThat(estoqueService.quantidade(vila, Recurso.REFEICAO)).isEqualByComparingTo("0");
		assertThat(estoqueService.quantidade(vila, Recurso.GRAOS)).isEqualByComparingTo("0");
		assertThat(estoqueService.quantidade(vila, Recurso.CARNE)).isEqualByComparingTo("9");
	}

	@Test
	void errosDeEnvio() throws Exception {
		cenario("30");
		Tropa vazia = tropa(0);
		Tropa comFerido = tropa(2);
		Cidadao f = cidadaoRepository.findByTropaId(comFerido.getId()).get(0);
		f.setEstado(EstadoCidadao.FERIDO);
		cidadaoRepository.saveAndFlush(f);
		masmorra(77L, 3, 2);
		enviar(vazia.getId(), 77L).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Tropa sem membros"));
		enviar(comFerido.getId(), 77L).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Tropa com membro ferido não pode partir em expedição"));
		Tropa ok = tropa(1);
		enviar(ok.getId(), 999L).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Masmorra não encontrada ou inativa"));
		enviar(ok.getId(), 77L).andExpect(status().isCreated());
		enviar(ok.getId(), 77L).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Tropa já está em expedição"));
	}

	@Test
	void comidaInsuficienteNaoAlteraNada() throws Exception {
		cenario("5");
		Tropa t = tropa(3); // precisa de 12
		masmorra(77L, 3, 2);
		enviar(t.getId(), 77L).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Comida insuficiente para a expedição"));
		assertThat(estoqueService.quantidade(vila, Recurso.REFEICAO)).isEqualByComparingTo("5");
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.AQUARTELADA);
		assertThat(eventos(TipoEventoTurno.EXPEDICAO_PARTIU)).isZero();
	}

	@Test
	void idaDeNTurnosBatalhaNoNEsimoEVoltaAteAquartelar() throws Exception {
		cenario("30");
		Tropa t = tropa(2);
		masmorra(77L, 3, 2); // 2 turnos
		enviar(t.getId(), 77L).andExpect(status().isCreated());

		turno(8, 10);
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.EM_VIAGEM_IDA);
		assertThat(relida(t).getTurnosRestantes()).isEqualTo(1);
		assertThat(batalhaRepository.findAll()).isEmpty();

		turno(8, 11);
		Tropa em = relida(t);
		assertThat(em.getEstado()).isEqualTo(EstadoTropa.EM_VIAGEM_VOLTA);
		assertThat(em.getTurnosRestantes()).isEqualTo(2);
		assertThat(em.getUltimaBatalhaId()).isNotNull();
		assertThat(batalhaRepository.findAll()).hasSize(1);
		verify(fonte).registrarResultado(eq(77L), eq(true), eq(11));

		turno(8, 12);
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.EM_VIAGEM_VOLTA);
		assertThat(relida(t).getTurnosRestantes()).isEqualTo(1);
		assertThat(eventos(TipoEventoTurno.TROPA_RETORNOU)).isZero();

		turno(8, 13);
		Tropa volta = relida(t);
		assertThat(volta.getEstado()).isEqualTo(EstadoTropa.AQUARTELADA);
		assertThat(volta.getMasmorraId()).isNull();
		assertThat(volta.getRegiaoDestino()).isNull();
		assertThat(volta.getTurnosViagem()).isNull();
		assertThat(volta.getTurnosRestantes()).isNull();
		assertThat(eventos(TipoEventoTurno.TROPA_RETORNOU)).isEqualTo(1);
		assertThat(batalhaRepository.findAll()).hasSize(1);
	}

	@Test
	void masmorraQueSumiuFazATropaVoltarSemLutar() throws Exception {
		cenario("30");
		Tropa t = tropa(1);
		masmorra(5L, 1, 1);
		enviar(t.getId(), 5L).andExpect(status().isCreated());
		when(fonte.buscarAtiva(vila.getId(), 5L)).thenReturn(Optional.empty());

		turno(8, 10);

		Tropa em = relida(t);
		assertThat(em.getEstado()).isEqualTo(EstadoTropa.EM_VIAGEM_VOLTA);
		assertThat(em.getTurnosRestantes()).isEqualTo(1);
		assertThat(batalhaRepository.findAll()).isEmpty();
		verify(fonte, never()).registrarResultado(anyLong(), org.mockito.ArgumentMatchers.anyBoolean(), anyInt());
		turno(8, 11);
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.AQUARTELADA);
	}

	@Test
	void viajanteNaoComeNoPasso3() throws Exception {
		cenario("30");
		Tropa t = tropa(2);
		masmorra(5L, 1, 1);
		enviar(t.getId(), 5L).andExpect(status().isCreated()); // 4 de comida, sobram 26
		turno(3, 10);
		// só 0 cidadãos na vila fora da tropa: nada é consumido
		assertThat(estoqueService.quantidade(vila, Recurso.REFEICAO)).isEqualByComparingTo("26");
		assertThat(cidadaoRepository.findByTropaId(t.getId())).allMatch(c -> c.getFamintoTurnos() == 0);
	}

	@Test
	void cidadaoDtoMostraExpedicao() throws Exception {
		cenario("30");
		Tropa t = tropa(1);
		Cidadao c = cidadaoRepository.findByTropaId(t.getId()).get(0);
		masmorra(5L, 1, 1);
		obter("/api/jogo/cidadao/" + c.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.emExpedicao").value(false))
				.andExpect(jsonPath("$.tropaId").value(t.getId())).andExpect(jsonPath("$.posicaoTropa").value("FRENTE"));
		enviar(t.getId(), 5L).andExpect(status().isCreated());
		obter("/api/jogo/cidadao/" + c.getId()).andExpect(jsonPath("$.emExpedicao").value(true))
				.andExpect(jsonPath("$.xpGuerreiro").value(0)).andExpect(jsonPath("$.feridoAteTurno").doesNotExist());
	}

	@Test
	void outraVilaDevolve404() throws Exception {
		cenario("30");
		Tropa t = tropa(1);
		masmorra(5L, 1, 1);
		Usuario outro = new Usuario();
		outro.setNome("O");
		outro.setEmail(UUID.randomUUID() + "@teste.com");
		outro.setSenha("x");
		outro = usuarioRepository.saveAndFlush(outro);
		vilaRepository.saveAndFlush(new Vila(outro.getId(), "Outra", 1L, 1));
		usuario = outro;
		obter("/api/jogo/tropas/" + t.getId() + "/destinos").andExpect(status().isNotFound());
		enviar(t.getId(), 5L).andExpect(status().isNotFound());
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.AQUARTELADA);
	}
}
