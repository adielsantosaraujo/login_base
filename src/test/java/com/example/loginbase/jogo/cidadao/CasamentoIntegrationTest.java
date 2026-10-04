package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.GeradorMapaService;
import com.example.loginbase.jogo.servico.MapaTestes;
import com.example.loginbase.jogo.servico.VilaService;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CasamentoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaService vilaService;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired OcupacaoCasasService ocupacao;

	Usuario usuario;
	Vila vila;
	Cidadao filhoA; // filho da família A
	Cidadao filhaA; // irmã de filhoA
	Cidadao filhaB; // filha da família B
	Construcao casaLivre;

	@BeforeEach
	void preparar() {
		usuario = novoUsuario();
		vila = criarVilaDe(usuario, 7L);
		List<Familia> familias = familiaRepository.findByVilaId(vila.getId()).stream()
				.sorted((a, b) -> a.getId().compareTo(b.getId())).toList();
		filhoA = filho(familias.get(0), Sexo.M);
		filhaA = filho(familias.get(0), Sexo.F);
		filhaB = filho(familias.get(1), Sexo.F);
		casaLivre = casa(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 8);
	}

	private Vila criarVilaDe(Usuario u, long semente) {
		List<Integer> indices = MapaTestes.selecioneValidas(new GeradorMapaService().gerar(semente), true);
		return vilaService.criarVilaComSemente(u.getId(), semente, indices);
	}

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private Cidadao filho(Familia f, Sexo sexo) {
		return cidadaoRepository.findByFamiliaIdAndVivoTrue(f.getId()).stream()
				.filter(c -> c.getIdadeAnos() == 18 && c.getSexo() == sexo).findFirst().orElseThrow();
	}

	private Construcao casa(NivelConstrucao nivel, EstadoConstrucao estado, int x) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.CASA);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(x);
		c.setY(2);
		c.setTamanho(2);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	private ResultActions casar(Usuario u, Long c1, Long c2, Long casaId, String sobrenome) throws Exception {
		return mvc.perform(post("/api/jogo/casamento").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(
						"{\"cidadao1Id\":%d,\"cidadao2Id\":%d,\"casaId\":%d,\"sobrenomeEscolhido\":\"%s\"}"
								.formatted(c1, c2, casaId, sobrenome)));
	}

	private String sobrenome(Cidadao c) {
		return familiaRepository.findById(c.getFamiliaId()).orElseThrow().getSobrenome();
	}

	@Test
	void casaElegiveisCriaNovoNucleoComConjugesReciprocos() throws Exception {
		Long familiaOrigemA = filhoA.getFamiliaId();
		String sobrenome = sobrenome(filhaB);
		casar(usuario, filhoA.getId(), filhaB.getId(), casaLivre.getId(), sobrenome)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sobrenome").value(sobrenome))
				.andExpect(jsonPath("$.casaId").value(casaLivre.getId()))
				.andExpect(jsonPath("$.membros", hasSize(2)));

		Cidadao a = cidadaoRepository.findById(filhoA.getId()).orElseThrow();
		Cidadao b = cidadaoRepository.findById(filhaB.getId()).orElseThrow();
		assertThat(a.getConjugeId()).isEqualTo(b.getId());
		assertThat(b.getConjugeId()).isEqualTo(a.getId());
		assertThat(a.getFamiliaId()).isEqualTo(b.getFamiliaId()).isNotEqualTo(familiaOrigemA);
		Familia nova = familiaRepository.findById(a.getFamiliaId()).orElseThrow();
		assertThat(nova.getSobrenome()).isEqualTo(sobrenome);
		assertThat(nova.getCasaId()).isEqualTo(casaLivre.getId());
		// pais continuam na casa de origem; núcleo de origem preservado
		assertThat(familiaRepository.findById(familiaOrigemA)).isPresent();
		assertThat(cidadaoRepository.findByFamiliaIdAndVivoTrue(familiaOrigemA)).hasSize(3);
		assertThat(eventoRepository.findAll()).anyMatch(e -> e.getTipo() == TipoEventoTurno.CASAMENTO
				&& e.getVilaId().equals(vila.getId()));
	}

	@Test
	void sobrenomeEscolhidoDeUmDosNoivos() throws Exception {
		String sobrenomeA = sobrenome(filhoA);
		casar(usuario, filhoA.getId(), filhaB.getId(), casaLivre.getId(), sobrenomeA)
				.andExpect(status().isOk()).andExpect(jsonPath("$.sobrenome").value(sobrenomeA));
		assertThat(familiaRepository.findById(cidadaoRepository.findById(filhoA.getId()).orElseThrow().getFamiliaId())
				.orElseThrow().getSobrenome()).isEqualTo(sobrenomeA);
	}

	@Test
	void sobrenomeForaDosNoivosDevolve400() throws Exception {
		casar(usuario, filhoA.getId(), filhaB.getId(), casaLivre.getId(), "Inventado")
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").exists());
	}

	@Test
	void menorDe18Devolve400() throws Exception {
		filhaB.setIdadeMeses(17 * 12 + 11);
		cidadaoRepository.saveAndFlush(filhaB);
		casar(usuario, filhoA.getId(), filhaB.getId(), casaLivre.getId(), sobrenome(filhoA))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").exists());
	}

	@Test
	void casadoDevolve400() throws Exception {
		Cidadao pai = cidadaoRepository.findById(filhoA.getPaiId()).orElseThrow(); // casado com a mãe
		casar(usuario, pai.getId(), filhaB.getId(), casaLivre.getId(), sobrenome(pai))
				.andExpect(status().isBadRequest());
	}

	@Test
	void irmaosDevolve400() throws Exception {
		casar(usuario, filhoA.getId(), filhaA.getId(), casaLivre.getId(), sobrenome(filhoA))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").exists());
	}

	@Test
	void semNucleoLivreDevolve400() throws Exception {
		Long casaCheia = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA).stream()
				.filter(c -> c.getId().equals(familiaRepository.findById(filhaB.getFamiliaId()).orElseThrow()
						.getCasaId()))
				.findFirst().orElseThrow().getId();
		casar(usuario, filhoA.getId(), filhaB.getId(), casaCheia, sobrenome(filhoA))
				.andExpect(status().isBadRequest());
	}

	@Test
	void casaEmObraDevolve400() throws Exception {
		Construcao emObra = casa(NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 12);
		casar(usuario, filhoA.getId(), filhaB.getId(), emObra.getId(), sobrenome(filhoA))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cidadaoOuCasaDeOutraVilaDevolve403() throws Exception {
		Usuario outro = novoUsuario();
		Vila vilaOutro = criarVilaDe(outro, 9L);
		Familia fo = familiaRepository.findByVilaId(vilaOutro.getId()).get(0);
		Cidadao estranho = filho(fo, Sexo.F);
		casar(usuario, filhoA.getId(), estranho.getId(), casaLivre.getId(), sobrenome(filhoA))
				.andExpect(status().isForbidden());
		casar(outro, estranho.getId(), filho(fo, Sexo.M).getId(), casaLivre.getId(), fo.getSobrenome())
				.andExpect(status().isForbidden());
	}

	@Test
	void nucleoDeOrigemVazioEhApagadoEViuvoPodeCasar() throws Exception {
		// núcleo de origem do filhoA fica só com ele mesmo
		Familia solo = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Solo", casaLivre.getId()));
		filhoA.setFamiliaId(solo.getId());
		filhoA.setPaiId(null);
		filhoA.setMaeId(null);
		cidadaoRepository.saveAndFlush(filhoA);
		Construcao outra = casa(NivelConstrucao.N2, EstadoConstrucao.ATIVA, 14);
		casar(usuario, filhoA.getId(), filhaB.getId(), outra.getId(), "Solo").andExpect(status().isOk());
		assertThat(familiaRepository.findById(solo.getId())).isEmpty();
	}

	@Test
	void listaFamiliasECasasComOcupacao() throws Exception {
		mvc.perform(get("/api/jogo/familias").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$[0].membros", hasSize(4)))
				.andExpect(jsonPath("$[0].membros[0].estadoCivil").value("CASADO"))
				.andExpect(jsonPath("$[0].membros[0].idadeAnos").value(40));
		mvc.perform(get("/api/jogo/casas").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)))
				.andExpect(jsonPath("$[4].nucleosLivres").value(1))
				.andExpect(jsonPath("$[4].vagasLivres").value(4))
				.andExpect(jsonPath("$[0].nucleosLivres").value(0))
				.andExpect(jsonPath("$[0].vagasLivres").value(0));
	}

	@Test
	void ocupacaoConsideraCapacidadePorNivelEGestacao() {
		Construcao n3 = casa(NivelConstrucao.N3, EstadoConstrucao.ATIVA, 16);
		var o = ocupacao.ocupacao(n3);
		assertThat(o.nucleosTotal()).isEqualTo(4);
		assertThat(o.vagasTotal()).isEqualTo(24);
		Familia f = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Gest", n3.getId()));
		Cidadao mae = new Cidadao(vila.getId(), f.getId(), "Maria", Sexo.F, 25 * 12);
		mae.setGestacaoTurnos(3);
		cidadaoRepository.saveAndFlush(mae);
		var o2 = ocupacao.ocupacao(n3);
		assertThat(o2.nucleosOcupados()).isEqualTo(1);
		assertThat(o2.vagasOcupadas()).isEqualTo(2); // mãe + vaga reservada do bebê
		assertThat(o2.vagasLivres()).isEqualTo(22);
	}

}
