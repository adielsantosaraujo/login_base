package com.example.loginbase.jogo.construcao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlocacaoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ConsultaTrabalhadores consulta;
	@Autowired EstoqueService estoqueService;

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

	private Cidadao cidadao(int idadeAnos) {
		return cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, idadeAnos * 12));
	}

	private Construcao construcao(TipoConstrucao tipo, NivelConstrucao nivel, EstadoConstrucao estado) {
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
		c.setPoAtual(estado == EstadoConstrucao.ATIVA ? 4 : 0);
		return construcaoRepository.saveAndFlush(c);
	}

	private ResultActions alocar(Long construcaoId, Long cidadaoId, String profissao) throws Exception {
		String corpo = profissao == null ? "{\"cidadaoId\":%d}".formatted(cidadaoId)
				: "{\"cidadaoId\":%d,\"profissao\":\"%s\"}".formatted(cidadaoId, profissao);
		return mvc.perform(post("/api/jogo/construcoes/" + construcaoId + "/alocacoes")
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	@Test
	void alocaCidadaoValidoEListaComEficiencia() throws Exception {
		novaVila();
		Construcao serraria = construcao(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao c = cidadao(30);
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.MADEIREIRO, 5));
		alocar(serraria.getId(), c.getId(), null).andExpect(status().isCreated())
				.andExpect(jsonPath("$.profissao").value("MADEIREIRO"))
				.andExpect(jsonPath("$.eficiencia").isNumber());
		assertThat(cidadaoRepository.findById(c.getId()).orElseThrow().getConstrucaoId()).isEqualTo(serraria.getId());
		mvc.perform(get("/api/jogo/construcoes/" + serraria.getId() + "/alocacoes").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value(c.getNome()));
	}

	@Test
	void rejeitaVagaCheiaIdadeTropaEDuplicidade() throws Exception {
		novaVila();
		Construcao serraria = construcao(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		alocar(serraria.getId(), cidadao(30).getId(), null).andExpect(status().isCreated());
		Cidadao dois = cidadao(30);
		alocar(serraria.getId(), dois.getId(), null).andExpect(status().isCreated());
		alocar(serraria.getId(), cidadao(30).getId(), null).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Sem vagas disponíveis"));
		Construcao outra = construcao(TipoConstrucao.SERRARIA, NivelConstrucao.N3, EstadoConstrucao.ATIVA);
		alocar(outra.getId(), cidadao(13).getId(), null).andExpect(status().isBadRequest());
		alocar(outra.getId(), cidadao(65).getId(), null).andExpect(status().isBadRequest());
		Cidadao tropa = cidadao(30);
		tropa.setTropaId(1L);
		cidadaoRepository.saveAndFlush(tropa);
		alocar(outra.getId(), tropa.getId(), null).andExpect(status().isBadRequest());
		alocar(outra.getId(), dois.getId(), null).andExpect(status().isBadRequest());
		Cidadao morto = cidadao(30);
		morto.setVivo(false);
		cidadaoRepository.saveAndFlush(morto);
		alocar(outra.getId(), morto.getId(), null).andExpect(status().isBadRequest());
	}

	@Test
	void estalagemExigeProfissaoEObraSoConstrutorOuCarregador() throws Exception {
		novaVila();
		Construcao estalagem = construcao(TipoConstrucao.ESTALAGEM, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao c = cidadao(30);
		alocar(estalagem.getId(), c.getId(), null).andExpect(status().isBadRequest());
		alocar(estalagem.getId(), c.getId(), "MINEIRO").andExpect(status().isBadRequest());
		alocar(estalagem.getId(), c.getId(), "COMERCIANTE").andExpect(status().isCreated())
				.andExpect(jsonPath("$.profissao").value("COMERCIANTE"));

		Construcao obra = construcao(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA);
		alocar(obra.getId(), cidadao(30).getId(), "MINEIRO").andExpect(status().isBadRequest());
		alocar(obra.getId(), cidadao(30).getId(), null).andExpect(status().isBadRequest());
		alocar(obra.getId(), cidadao(30).getId(), "CONSTRUTOR").andExpect(status().isCreated());
		alocar(obra.getId(), cidadao(30).getId(), "CARREGADOR").andExpect(status().isCreated());
		alocar(obra.getId(), cidadao(30).getId(), "CONSTRUTOR").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Sem vagas disponíveis"));
	}

	@Test
	void desalocaELiberaVaga() throws Exception {
		novaVila();
		Construcao serraria = construcao(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao c = cidadao(30);
		alocar(serraria.getId(), c.getId(), null).andExpect(status().isCreated());
		mvc.perform(delete("/api/jogo/construcoes/" + serraria.getId() + "/alocacoes/" + c.getId())
				.with(user(usuario.getEmail()).roles("USER")).with(csrf())).andExpect(status().isNoContent());
		Cidadao lido = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(lido.getConstrucaoId()).isNull();
		assertThat(lido.getProfissaoTrabalho()).isNull();
		mvc.perform(delete("/api/jogo/construcoes/" + serraria.getId() + "/alocacoes/" + c.getId())
				.with(user(usuario.getEmail()).roles("USER")).with(csrf())).andExpect(status().isBadRequest());
	}

	@Test
	void capacidadeDoArmazemUsaCarregadoresAlocados() {
		novaVila();
		estoqueService.inicializar(vila, java.util.Map.of());
		Construcao armazem = construcao(TipoConstrucao.ARMAZEM, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		assertThat(estoqueService.calcularCapacidadeTotal(vila).get(Recurso.MADEIRA)).isEqualByComparingTo("500");
		Cidadao c = cidadao(30);
		c.setConstrucaoId(armazem.getId());
		c.setProfissaoTrabalho(Profissao.CARREGADOR);
		cidadaoRepository.saveAndFlush(c);
		double ef = consulta.trabalhadores(armazem, vila).get(0).eficiencia();
		BigDecimal esperado = BigDecimal.valueOf(500).add(BigDecimal.valueOf(500 * ef).setScale(2, java.math.RoundingMode.DOWN));
		assertThat(estoqueService.calcularCapacidadeTotal(vila).get(Recurso.MADEIRA).doubleValue())
				.isCloseTo(esperado.doubleValue(), org.assertj.core.data.Offset.offset(0.5));
		// Armazém em obra não conta
		Construcao obra = construcao(TipoConstrucao.ARMAZEM, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA);
		Cidadao c2 = cidadao(30);
		c2.setConstrucaoId(obra.getId());
		c2.setProfissaoTrabalho(Profissao.CARREGADOR);
		cidadaoRepository.saveAndFlush(c2);
		assertThat(estoqueService.calcularCapacidadeTotal(vila).get(Recurso.MADEIRA).doubleValue())
				.isCloseTo(esperado.doubleValue(), org.assertj.core.data.Offset.offset(0.5));
	}

}
