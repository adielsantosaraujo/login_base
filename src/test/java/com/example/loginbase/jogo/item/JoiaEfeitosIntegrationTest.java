package com.example.loginbase.jogo.item;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JoiaEfeitosIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired ItemRepository itemRepository;
	@MockitoBean ConsultaTropas consultaTropas;

	private Usuario usuario;
	private Vila vila;
	private Familia familia;

	private void novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		usuario = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
	}

	private Cidadao cidadao(int idadeAnos, int forca) {
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), "C", Sexo.M, idadeAnos * 12);
		c.setVit(0);
		c.setForca(forca);
		c.setVel(0);
		return cidadaoRepository.saveAndFlush(c);
	}

	private Item colar(int nivel) {
		return itemRepository.saveAndFlush(new Item(vila.getId(), ItemSubtipo.COLAR, Qualidade.SIMPLES, nivel, List.of()));
	}

	private Item anel(int nivel, CodigoBonus atributo) {
		Item i = new Item(vila.getId(), ItemSubtipo.ANEL, Qualidade.SIMPLES, nivel, List.of());
		i.setAtributoEscolhido(atributo);
		return itemRepository.saveAndFlush(i);
	}

	private ResultActions equipar(Cidadao c, String slot, Item i) throws Exception {
		return mvc.perform(put("/api/jogo/cidadao/" + c.getId() + "/equipamento/" + slot)
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"itemId\":" + i.getId() + "}"));
	}

	private ResultActions desequipar(Cidadao c, String slot) throws Exception {
		return mvc.perform(delete("/api/jogo/cidadao/" + c.getId() + "/equipamento/" + slot)
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()));
	}

	private ResultActions painel(Cidadao c) throws Exception {
		return mvc.perform(get("/api/jogo/cidadao/" + c.getId()).with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk());
	}


	@Test
	void colarSomaVidaEDesequiparRemove() throws Exception {
		novaVila();
		Cidadao c = cidadao(25, 0);
		painel(c).andExpect(jsonPath("$.vidaMaxima").value(30));
		equipar(c, "COLAR", colar(3)).andExpect(status().isOk());
		painel(c).andExpect(jsonPath("$.vidaMaxima").value(45));
		desequipar(c, "COLAR").andExpect(status().isOk());
		painel(c).andExpect(jsonPath("$.vidaMaxima").value(30));
	}

	@Test
	void segundoColarTrocaOPrimeiro() throws Exception {
		novaVila();
		Cidadao c = cidadao(25, 0);
		equipar(c, "COLAR", colar(1)).andExpect(status().isOk());
		equipar(c, "COLAR", colar(4)).andExpect(status().isOk());
		painel(c).andExpect(jsonPath("$.vidaMaxima").value(50));
	}

	@Test
	void anelForcaSobeCaracteristicaEPeAoCruzarMultiploDe5() throws Exception {
		novaVila();
		Cidadao c = cidadao(25, 4);
		painel(c).andExpect(jsonPath("$.caracteristicasTotais.FOR").value(4))
				.andExpect(jsonPath("$.profissoes[?(@.profissao=='GUERREIRO')].peEfetivo").value(0))
				.andExpect(jsonPath("$.vidaMaxima").value(30));
		equipar(c, "ANEL", anel(1, CodigoBonus.FOR)).andExpect(status().isOk());
		painel(c).andExpect(jsonPath("$.caracteristicasTotais.FOR").value(5))
				.andExpect(jsonPath("$.profissoes[?(@.profissao=='GUERREIRO')].peEfetivo").value(1))
				.andExpect(jsonPath("$.vidaMaxima").value(33));
		desequipar(c, "ANEL_1").andExpect(status().isOk());
		painel(c).andExpect(jsonPath("$.caracteristicasTotais.FOR").value(4));
	}

	@Test
	void doisAneisOkTerceiroRejeitado() throws Exception {
		novaVila();
		Cidadao c = cidadao(25, 0);
		equipar(c, "ANEL", anel(1, CodigoBonus.VIT)).andExpect(status().isOk());
		equipar(c, "ANEL", anel(4, CodigoBonus.VIT)).andExpect(status().isOk());
		painel(c).andExpect(jsonPath("$.caracteristicasTotais.VIT").value(3));
		equipar(c, "ANEL", anel(1, CodigoBonus.VEL)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Máximo 2 anéis por pessoa"));
	}

	@Test
	void idadeMenorQue14Rejeitada() throws Exception {
		novaVila();
		Cidadao c = cidadao(13, 0);
		equipar(c, "COLAR", colar(1)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Mínimo 14 anos para equipar joias"));
		painel(c).andExpect(jsonPath("$.vidaMaxima").value(30));
	}

	@Test
	void expedicaoBloqueiaTroca() throws Exception {
		novaVila();
		Cidadao c = cidadao(25, 0);
		when(consultaTropas.emExpedicao(c.getId())).thenReturn(true);
		equipar(c, "COLAR", colar(1)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Não pode trocar equipamento em expedição"));
	}
}
