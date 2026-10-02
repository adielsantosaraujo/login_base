package com.example.loginbase.jogo.cidadao;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CidadaoControllerIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired com.example.loginbase.jogo.construcao.ConstrucaoRepository construcaoRepository;

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

	private ResultActions obter(String url) throws Exception {
		return mvc.perform(get(url).with(user(usuario.getEmail()).roles("USER")));
	}

	private ResultActions distribuir(Long id, String corpo) throws Exception {
		return mvc.perform(post("/api/jogo/cidadao/" + id + "/distribuir-pontos")
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	@Test
	void obtemDadosCompletos() throws Exception {
		novaVila();
		Cidadao c = cidadao(30);
		c.setInteligencia(10);
		c.setPontosCarPendentes(7);
		c.setPontosProfPendentes(3);
		c.setFamintoTurnos(1);
		cidadaoRepository.saveAndFlush(c);
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.CONSTRUTOR, 4));
		obter("/api/jogo/cidadao/" + c.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value(c.getNome()))
				.andExpect(jsonPath("$.idadeAnos").value(30))
				.andExpect(jsonPath("$.familiaNome").value("Silva"))
				.andExpect(jsonPath("$.faminto").value(true))
				.andExpect(jsonPath("$.pontosCarPendentes").value(7))
				.andExpect(jsonPath("$.pontosProfPendentes").value(3))
				.andExpect(jsonPath("$.caracteristicas.INT").value(10))
				.andExpect(jsonPath("$.profissoes", hasSize(12)))
				.andExpect(jsonPath("$.profissoes[?(@.profissao=='CONSTRUTOR')].peBase").value(4))
				.andExpect(jsonPath("$.profissoes[?(@.profissao=='CONSTRUTOR')].peEfetivo").value(6))
				.andExpect(jsonPath("$.profissoes[?(@.profissao=='CONSTRUTOR')].eficiencia").isNotEmpty())
				.andExpect(jsonPath("$.construcaoId").doesNotExist());
	}

	@Test
	void distribuiPontosEAcumula() throws Exception {
		novaVila();
		Cidadao c = cidadao(30);
		c.setPontosCarPendentes(5);
		c.setPontosProfPendentes(2);
		cidadaoRepository.saveAndFlush(c);
		int vit0 = c.getVit();
		int for0 = c.getForca();
		distribuir(c.getId(), "{\"caracteristicas\":{\"vit\":2,\"FOR\":3},\"profissoes\":{\"CONSTRUTOR\":2}}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pontosCarPendentes").value(0))
				.andExpect(jsonPath("$.pontosProfPendentes").value(0))
				.andExpect(jsonPath("$.caracteristicas.VIT").value(vit0 + 2))
				.andExpect(jsonPath("$.caracteristicas.FOR").value(for0 + 3))
				.andExpect(jsonPath("$.profissoes[?(@.profissao=='CONSTRUTOR')].peBase").value(2));
		distribuir(c.getId(), "{\"caracteristicas\":{\"VIT\":1}}").andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaExcessoNegativoEChaveInvalida() throws Exception {
		novaVila();
		Cidadao c = cidadao(30);
		c.setPontosCarPendentes(2);
		c.setPontosProfPendentes(1);
		cidadaoRepository.saveAndFlush(c);
		distribuir(c.getId(), "{\"caracteristicas\":{\"VIT\":3}}").andExpect(status().isBadRequest());
		distribuir(c.getId(), "{\"profissoes\":{\"CONSTRUTOR\":2}}").andExpect(status().isBadRequest());
		distribuir(c.getId(), "{\"caracteristicas\":{\"VIT\":-1}}").andExpect(status().isBadRequest());
		distribuir(c.getId(), "{\"caracteristicas\":{\"XYZ\":1}}").andExpect(status().isBadRequest());
		distribuir(c.getId(), "{}").andExpect(status().isBadRequest());
	}

	@Test
	void naoEncontradoEOutraVila() throws Exception {
		novaVila();
		obter("/api/jogo/cidadao/999999999").andExpect(status().isNotFound());
		Usuario meu = usuario;
		Cidadao meuCid = cidadao(30);
		novaVila();
		Cidadao alheio = cidadao(30);
		usuario = meu;
		vila = vilaRepository.findAll().stream().filter(v -> v.getUsuarioId().equals(meu.getId())).findFirst().orElseThrow();
		obter("/api/jogo/cidadao/" + alheio.getId()).andExpect(status().isNotFound());
		distribuir(alheio.getId(), "{\"caracteristicas\":{\"VIT\":1}}").andExpect(status().isNotFound());
		obter("/api/jogo/cidadao/" + meuCid.getId()).andExpect(status().isOk());
	}

	@Test
	void listaVivosComFiltroDeElegiveis() throws Exception {
		novaVila();
		cidadao(30);
		cidadao(10);
		cidadao(70);
		Cidadao alocado = cidadao(30);
		com.example.loginbase.jogo.construcao.Construcao k = new com.example.loginbase.jogo.construcao.Construcao();
		k.setVilaId(vila.getId());
		k.setTipo(com.example.loginbase.jogo.construcao.TipoConstrucao.SERRARIA);
		k.setNivel(com.example.loginbase.jogo.construcao.NivelConstrucao.N1);
		k.setRegiaoIndice(1);
		k.setX(0);
		k.setY(0);
		k.setTamanho(1);
		k.setEstado(com.example.loginbase.jogo.construcao.EstadoConstrucao.ATIVA);
		k.setPoTotal(4);
		k.setPoAtual(4);
		alocado.setConstrucaoId(construcaoRepository.saveAndFlush(k).getId());
		alocado.setProfissaoTrabalho(Profissao.CONSTRUTOR);
		cidadaoRepository.saveAndFlush(alocado);
		Cidadao morto = cidadao(30);
		morto.setVivo(false);
		cidadaoRepository.saveAndFlush(morto);
		obter("/api/jogo/cidadaos").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$[0].faminto").value(false));
		obter("/api/jogo/cidadaos?elegiveisTrabalho=true").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
	}

}
