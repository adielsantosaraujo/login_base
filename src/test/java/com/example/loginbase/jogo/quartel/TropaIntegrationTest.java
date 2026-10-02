package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.MorteService;
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
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TropaIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired TropaRepository tropaRepository;
	@Autowired MorteService morteService;

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

	private Cidadao cidadao(int anos) {
		return cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, anos * 12));
	}

	/** Guerreiro elegível: 20 anos, PE base 1, espada equipada. */
	private Cidadao guerreiro() {
		Cidadao c = cidadao(20);
		profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.GUERREIRO, 1));
		equiparArma(c);
		return c;
	}

	private void equiparArma(Cidadao c) {
		Item i = itemRepository.saveAndFlush(new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.SIMPLES, 1, List.of()));
		i.setCidadaoId(c.getId());
		i.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(i);
	}

	private Construcao quartel(NivelConstrucao nivel, EstadoConstrucao estado, int instrutores) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.QUARTEL);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(8);
		c.setPoAtual(8);
		c = construcaoRepository.saveAndFlush(c);
		for (int i = 0; i < instrutores; i++) {
			Cidadao inst = cidadao(30);
			inst.setConstrucaoId(c.getId());
			inst.setProfissaoTrabalho(Profissao.GUERREIRO);
			cidadaoRepository.saveAndFlush(inst);
		}
		return c;
	}

	private static String membros(String nome, Object... pares) {
		StringBuilder sb = new StringBuilder("{\"nome\":\"" + nome + "\",\"membros\":[");
		for (int i = 0; i < pares.length; i += 2) {
			sb.append(i > 0 ? "," : "").append("{\"cidadaoId\":").append(pares[i]).append(",\"posicao\":\"")
					.append(pares[i + 1]).append("\"}");
		}
		return sb.append("]}").toString();
	}

	private ResultActions criar(Long quartelId, String json) throws Exception {
		return mvc.perform(post("/api/jogo/quarteis/" + quartelId + "/tropas").with(user(usuario.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private ResultActions addMembro(Long tropaId, Long cidadaoId, String pos) throws Exception {
		return mvc.perform(post("/api/jogo/tropas/" + tropaId + "/membros").with(user(usuario.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"cidadaoId\":" + cidadaoId + ",\"posicao\":\"" + pos + "\"}"));
	}

	private ResultActions consultar(String url) throws Exception {
		return mvc.perform(get(url).with(user(usuario.getEmail()).roles("USER")));
	}

	private void erro(ResultActions r, String msg) throws Exception {
		r.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(msg));
	}

	@Test
	void criaTropaComPosicoes() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao a = guerreiro(), b = guerreiro(), c = guerreiro();
		criar(q.getId(), membros("Guardiões", a.getId(), "FRENTE", b.getId(), "FRENTE", c.getId(), "RETAGUARDA"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("AQUARTELADA"))
				.andExpect(jsonPath("$.nome").value("Guardiões")).andExpect(jsonPath("$.totalMembros").value(3))
				.andExpect(jsonPath("$.membros", hasSize(3)))
				.andExpect(jsonPath("$.membros[0].posicao").value("FRENTE"))
				.andExpect(jsonPath("$.membros[2].posicao").value("RETAGUARDA"));
		Cidadao recarregado = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(recarregado.getTropaId()).isNotNull();
		assertThat(recarregado.getPosicaoTropa()).isEqualTo(PosicaoTropa.RETAGUARDA);
		consultar("/api/jogo/quarteis/" + q.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.nivel").value("N1"))
				.andExpect(jsonPath("$.instrutores").value(1)).andExpect(jsonPath("$.capacidade").value(5))
				.andExpect(jsonPath("$.membrosAtuais").value(3)).andExpect(jsonPath("$.maxTropas").value(1))
				.andExpect(jsonPath("$.tropas", hasSize(1)));
	}

	@Test
	void quartelSemInstrutorOuInativoBloqueia() throws Exception {
		novaVila();
		Construcao sem = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 0);
		erro(criar(sem.getId(), membros("T")), "Quartel sem instrutor");
		Construcao obra = quartel(NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 1);
		erro(criar(obra.getId(), membros("T")), "Quartel não está ativo");
	}

	@Test
	void instrutorFeridoNaoConta() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao inst = cidadaoRepository.findByConstrucaoIdAndVivoTrue(q.getId()).get(0);
		inst.setEstado(EstadoCidadao.FERIDO);
		cidadaoRepository.saveAndFlush(inst);
		erro(criar(q.getId(), membros("T")), "Quartel sem instrutor");
	}

	@Test
	void capacidadeTotalDoQuartelEMaxTropas() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N2, EstadoConstrucao.ATIVA, 1);
		Object[] cinco = new Object[10];
		for (int i = 0; i < 5; i++) {
			cinco[2 * i] = guerreiro().getId();
			cinco[2 * i + 1] = "FRENTE";
		}
		criar(q.getId(), membros("A", cinco)).andExpect(status().isCreated());
		// 4 membros em nova tropa: 5 + 4 = 9 > 8 (capacidade total do quartel)
		Object[] quatro = new Object[8];
		for (int i = 0; i < 4; i++) {
			quatro[2 * i] = guerreiro().getId();
			quatro[2 * i + 1] = "FRENTE";
		}
		erro(criar(q.getId(), membros("B", quatro)), "Capacidade do quartel excedida");
		assertThat(tropaRepository.findByQuartelId(q.getId())).hasSize(1);
		criar(q.getId(), membros("B", quatro[0], "FRENTE", quatro[2], "FRENTE", quatro[4], "FRENTE"))
				.andExpect(status().isCreated());
		// 8 membros no total: 9º é rejeitado ao adicionar
		Long tropaB = tropaRepository.findByQuartelId(q.getId()).stream().filter(t -> t.getNome().equals("B"))
				.findFirst().orElseThrow().getId();
		erro(addMembro(tropaB, (Long) quatro[6], "FRENTE"), "Capacidade do quartel excedida");
	}

	@Test
	void maximoDeTropasPorNivel() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		criar(q.getId(), membros("A")).andExpect(status().isCreated());
		erro(criar(q.getId(), membros("B")), "Limite de tropas do quartel atingido");
	}

	@Test
	void nomeObrigatorioEUnico() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N3, EstadoConstrucao.ATIVA, 1);
		erro(criar(q.getId(), "{\"nome\":\"  \"}"), "Informe o nome da tropa");
		criar(q.getId(), "{\"nome\":\"A\"}").andExpect(status().isCreated());
		erro(criar(q.getId(), "{\"nome\":\"A\"}"), "Já existe uma tropa com este nome");
	}

	@Test
	void membrosInelegiveis() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N3, EstadoConstrucao.ATIVA, 1);
		Cidadao semArma = cidadao(20);
		profissaoRepository.saveAndFlush(new CidadaoProfissao(semArma.getId(), Profissao.GUERREIRO, 1));
		erro(criar(q.getId(), membros("T", semArma.getId(), "FRENTE")), "Cidadão não tem arma equipada");
		Cidadao semPe = cidadao(20);
		equiparArma(semPe);
		erro(criar(q.getId(), membros("T", semPe.getId(), "FRENTE")), "PE Guerreiro insuficiente");
		Cidadao novo = guerreiro();
		novo.setIdadeMeses(15 * 12);
		cidadaoRepository.saveAndFlush(novo);
		erro(criar(q.getId(), membros("T", novo.getId(), "FRENTE")), "Idade fora do intervalo de combate (16–54)");
		Cidadao ferido = guerreiro();
		ferido.setEstado(EstadoCidadao.FERIDO);
		cidadaoRepository.saveAndFlush(ferido);
		erro(criar(q.getId(), membros("T", ferido.getId(), "FRENTE")), "Cidadão ferido");
		Cidadao morto = guerreiro();
		morteService.morrer(morto, "teste");
		erro(criar(q.getId(), membros("T", morto.getId(), "FRENTE")), "Cidadão morto");
		Cidadao alocado = guerreiro();
		alocado.setConstrucaoId(q.getId());
		cidadaoRepository.saveAndFlush(alocado);
		erro(criar(q.getId(), membros("T", alocado.getId(), "FRENTE")), "Cidadão alocado em prédio");
		Cidadao ok = guerreiro();
		erro(criar(q.getId(), membros("T", ok.getId(), "FRENTE", ok.getId(), "RETAGUARDA")), "Cidadão já está em tropa");
		criar(q.getId(), membros("T", ok.getId(), "CENTRO")).andExpect(status().isBadRequest());
		assertThat(tropaRepository.findByQuartelId(q.getId())).isEmpty();
	}

	@Test
	void cidadaoJaEmOutraTropa() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N3, EstadoConstrucao.ATIVA, 1);
		Cidadao g = guerreiro();
		criar(q.getId(), membros("A", g.getId(), "FRENTE")).andExpect(status().isCreated());
		erro(criar(q.getId(), membros("B", g.getId(), "FRENTE")), "Cidadão já está em tropa");
	}

	@Test
	void adicionarERemoverMembro() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao a = guerreiro(), b = guerreiro();
		criar(q.getId(), membros("A", a.getId(), "FRENTE")).andExpect(status().isCreated());
		Long tropaId = tropaRepository.findByQuartelId(q.getId()).get(0).getId();
		addMembro(tropaId, b.getId(), "RETAGUARDA").andExpect(status().isCreated())
				.andExpect(jsonPath("$.totalMembros").value(2));
		erro(addMembro(tropaId, b.getId(), "FRENTE"), "Cidadão já está em tropa");
		mvc.perform(delete("/api/jogo/tropas/" + tropaId + "/membros/" + b.getId())
				.with(user(usuario.getEmail()).roles("USER")).with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalMembros").value(1));
		assertThat(cidadaoRepository.findById(b.getId()).orElseThrow().getTropaId()).isNull();
		mvc.perform(delete("/api/jogo/tropas/" + tropaId + "/membros/" + b.getId())
				.with(user(usuario.getEmail()).roles("USER")).with(csrf())).andExpect(status().isNotFound());
	}

	@Test
	void desfazerSoAquartelada() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N2, EstadoConstrucao.ATIVA, 1);
		Cidadao a = guerreiro();
		criar(q.getId(), membros("A", a.getId(), "FRENTE")).andExpect(status().isCreated());
		criar(q.getId(), membros("B")).andExpect(status().isCreated());
		Tropa ta = tropaRepository.findByQuartelId(q.getId()).stream().filter(t -> t.getNome().equals("A")).findFirst().orElseThrow();
		Tropa tb = tropaRepository.findByQuartelId(q.getId()).stream().filter(t -> t.getNome().equals("B")).findFirst().orElseThrow();
		tb.setEstado(EstadoTropa.EM_VIAGEM_IDA);
		tropaRepository.saveAndFlush(tb);
		erro(mvc.perform(delete("/api/jogo/tropas/" + tb.getId()).with(user(usuario.getEmail()).roles("USER")).with(csrf())),
				"Tropa em expedição não pode ser alterada");
		erro(addMembro(tb.getId(), guerreiro().getId(), "FRENTE"), "Tropa em expedição não pode ser alterada");
		mvc.perform(delete("/api/jogo/tropas/" + ta.getId()).with(user(usuario.getEmail()).roles("USER")).with(csrf()))
				.andExpect(status().isNoContent());
		assertThat(tropaRepository.findById(ta.getId())).isEmpty();
		Cidadao recarregado = cidadaoRepository.findById(a.getId()).orElseThrow();
		assertThat(recarregado.getTropaId()).isNull();
		assertThat(recarregado.getPosicaoTropa()).isNull();
	}

	@Test
	void guerreirosDisponiveisComElegibilidadeEMotivo() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao ok = guerreiro();
		Cidadao semArma = cidadao(20);
		profissaoRepository.saveAndFlush(new CidadaoProfissao(semArma.getId(), Profissao.GUERREIRO, 2));
		consultar("/api/jogo/quarteis/" + q.getId() + "/guerreiros-disponiveis").andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id==" + ok.getId() + ")].elegivel").value(true))
				.andExpect(jsonPath("$[?(@.id==" + ok.getId() + ")].arma").value("Espada"))
				.andExpect(jsonPath("$[?(@.id==" + semArma.getId() + ")].elegivel").value(false))
				.andExpect(jsonPath("$[?(@.id==" + semArma.getId() + ")].motivo").value("Cidadão não tem arma equipada"))
				.andExpect(jsonPath("$[?(@.id==" + semArma.getId() + ")].peGuerreiro").value(2));
	}

	@Test
	void recursosDeOutraVilaRetornam404() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao g = guerreiro();
		criar(q.getId(), membros("A", g.getId(), "FRENTE")).andExpect(status().isCreated());
		Tropa tropa = tropaRepository.findByQuartelId(q.getId()).get(0);
		Cidadao outraVilaCidadao = g;
		// segunda vila/usuário
		Usuario dono = usuario;
		Vila vilaA = vila;
		novaVila();
		Construcao q2 = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		criar(q2.getId(), "{\"nome\":\"B\"}").andExpect(status().isCreated());
		Tropa t2 = tropaRepository.findByQuartelId(q2.getId()).get(0);
		consultar("/api/jogo/quarteis/" + q.getId()).andExpect(status().isNotFound());
		consultar("/api/jogo/quarteis/" + q.getId() + "/guerreiros-disponiveis").andExpect(status().isNotFound());
		criar(q.getId(), "{\"nome\":\"X\"}").andExpect(status().isNotFound());
		consultar("/api/jogo/tropas/" + tropa.getId()).andExpect(status().isNotFound());
		erro404(addMembro(tropa.getId(), guerreiro().getId(), "FRENTE"));
		mvc.perform(delete("/api/jogo/tropas/" + tropa.getId()).with(user(usuario.getEmail()).roles("USER")).with(csrf()))
				.andExpect(status().isNotFound());
		// cidadão de outra vila ao adicionar na própria tropa
		erro404(addMembro(t2.getId(), outraVilaCidadao.getId(), "FRENTE"));
		consultar("/api/jogo/tropas/" + t2.getId()).andExpect(status().isOk());
		assertThat(dono).isNotNull();
		assertThat(vilaA).isNotEqualTo(vila);
	}

	private void erro404(ResultActions r) throws Exception {
		r.andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").exists());
	}

	@Test
	void morteRemoveDaTropa() throws Exception {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao g = guerreiro();
		criar(q.getId(), membros("A", g.getId(), "FRENTE")).andExpect(status().isCreated());
		morteService.morrer(cidadaoRepository.findById(g.getId()).orElseThrow(), "teste");
		Cidadao morto = cidadaoRepository.findById(g.getId()).orElseThrow();
		assertThat(morto.getTropaId()).isNull();
		assertThat(morto.getPosicaoTropa()).isNull();
	}
}
