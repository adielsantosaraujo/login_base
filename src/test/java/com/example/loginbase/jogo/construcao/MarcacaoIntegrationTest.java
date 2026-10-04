package com.example.loginbase.jogo.construcao;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MarcacaoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired LadrilhoRepository ladrilhoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired VerificadorOcupacaoLadrilhos verificador;

	Usuario usuario;
	Vila vila;
	Regiao regiao;
	Construcao lenhador;

	@BeforeEach
	void preparar() {
		usuario = novoUsuario();
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 42L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i == 1) {
				r.setPossuida(true);
				r.setTipo(TipoRegiao.FLORESTA);
			}
			r = regiaoRepository.save(r);
			if (i == 1) {
				regiao = r;
			}
		}
		regiaoRepository.flush();
		// Floresta em linha y=0 (x 0..9) e em y=1 (x 0..5); rocha em (5,5).
		for (int x = 0; x < 10; x++) {
			ladrilhoRepository.save(new Ladrilho(regiao.getId(), x, 0, TipoTerreno.FLORESTA, 0, 0));
		}
		for (int x = 0; x < 6; x++) {
			ladrilhoRepository.save(new Ladrilho(regiao.getId(), x, 1, TipoTerreno.FLORESTA, 0, 0));
		}
		ladrilhoRepository.save(new Ladrilho(regiao.getId(), 5, 5, TipoTerreno.ROCHA, 0, 0));
		ladrilhoRepository.flush();
		lenhador = construir(TipoConstrucao.ACAMPAMENTO_LENHADORES, 3, 3);
	}

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private Construcao construir(TipoConstrucao tipo, int x, int y) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(x);
		c.setY(y);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	private ResultActions marcar(Usuario u, Long id, int x, int y) throws Exception {
		return mvc.perform(post("/api/jogo/construcoes/" + id + "/marcacoes").with(user(u.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"x\":%d,\"y\":%d}".formatted(x, y)));
	}

	private ResultActions desmarcar(Usuario u, Long id, int x, int y) throws Exception {
		return mvc.perform(delete("/api/jogo/construcoes/" + id + "/marcacoes/" + x + "/" + y)
				.with(user(u.getEmail()).roles("USER")).with(csrf()));
	}

	/** Prédio no topo da faixa de floresta: (0,1) adjacente a um prédio em (0,2). */
	private Construcao lenhadorPerto() {
		return construir(TipoConstrucao.ACAMPAMENTO_LENHADORES, 0, 2);
	}

	@Test
	void marcarTerrenoCompativelConectado() throws Exception {
		Construcao c = lenhadorPerto();
		marcar(usuario, c.getId(), 0, 1).andExpect(status().isCreated())
				.andExpect(jsonPath("$.x").value(0)).andExpect(jsonPath("$.y").value(1));
		// (0,0) é conectado via marcação já feita
		marcar(usuario, c.getId(), 0, 0).andExpect(status().isCreated());
		mvc.perform(get("/api/jogo/construcoes/" + c.getId() + "/marcacoes").with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void rejeitaTerrenoErrado() throws Exception {
		Construcao c = lenhadorPerto();
		ladrilhoRepository.save(new Ladrilho(regiao.getId(), 1, 2, TipoTerreno.ROCHA, 0, 0));
		marcar(usuario, c.getId(), 1, 2).andExpect(status().isBadRequest());
		// ladrilho de terreno incompatível
		marcar(usuario, c.getId(), 0, 3).andExpect(status().isBadRequest());
	}

	@Test
	void pedreiraEmFerroRejeitadaEmRochaAceita() throws Exception {
		Construcao p = construir(TipoConstrucao.PEDREIRA, 7, 5);
		ladrilhoRepository.save(new Ladrilho(regiao.getId(), 7, 4, TipoTerreno.FERRO, 0, 0));
		ladrilhoRepository.save(new Ladrilho(regiao.getId(), 6, 5, TipoTerreno.ROCHA, 0, 0));
		ladrilhoRepository.flush();
		marcar(usuario, p.getId(), 7, 4).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Ladrilho sem o terreno do prédio (Rocha)"));
		marcar(usuario, p.getId(), 6, 5).andExpect(status().isCreated());
	}

	@Test
	void rejeitaSemConectividade() throws Exception {
		Construcao c = lenhadorPerto();
		marcar(usuario, c.getId(), 4, 0).andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaApenasDiagonal() throws Exception {
		Construcao c = construir(TipoConstrucao.ACAMPAMENTO_LENHADORES, 6, 2);
		marcar(usuario, c.getId(), 5, 1).andExpect(status().isBadRequest());
		marcar(usuario, c.getId(), 6, 1).andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaAposLimiteN1() throws Exception {
		Construcao c = lenhadorPerto();
		marcar(usuario, c.getId(), 0, 1).andExpect(status().isCreated());
		marcar(usuario, c.getId(), 0, 0).andExpect(status().isCreated());
		marcar(usuario, c.getId(), 1, 0).andExpect(status().isCreated());
		marcar(usuario, c.getId(), 1, 1).andExpect(status().isCreated());
		marcar(usuario, c.getId(), 2, 0).andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaLadrilhoJaMarcadoPorOutroPredio() throws Exception {
		Construcao a = lenhadorPerto();
		Construcao b = construir(TipoConstrucao.CABANA_CACA, 2, 2);
		marcar(usuario, a.getId(), 1, 1).andExpect(status().isBadRequest()); // sem conexão com (0,2)? (1,2) não é marcado
		marcar(usuario, a.getId(), 0, 1).andExpect(status().isCreated());
		marcar(usuario, b.getId(), 2, 1).andExpect(status().isCreated());
		marcar(usuario, a.getId(), 1, 1).andExpect(status().isCreated());
		marcar(usuario, b.getId(), 1, 1).andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaNaoColetaEConstrucaoAlheia() throws Exception {
		Construcao casa = construir(TipoConstrucao.CASA, 0, 2);
		marcar(usuario, casa.getId(), 0, 1).andExpect(status().isBadRequest());
		Construcao c = lenhadorPerto();
		Usuario outro = novoUsuario();
		vilaRepository.saveAndFlush(new Vila(outro.getId(), "Outra", 7L, 1));
		marcar(outro, c.getId(), 0, 1).andExpect(status().isForbidden());
		marcar(usuario, 999999L, 0, 1).andExpect(status().isNotFound());
	}

	@Test
	void desmarcarComSucessoEBloqueiaDesconexao() throws Exception {
		Construcao c = lenhadorPerto();
		marcar(usuario, c.getId(), 0, 1).andExpect(status().isCreated());
		marcar(usuario, c.getId(), 0, 0).andExpect(status().isCreated());
		desmarcar(usuario, c.getId(), 0, 1).andExpect(status().isBadRequest()); // desconectaria (0,0)
		desmarcar(usuario, c.getId(), 0, 0).andExpect(status().isNoContent());
		desmarcar(usuario, c.getId(), 0, 1).andExpect(status().isNoContent());
		desmarcar(usuario, c.getId(), 0, 1).andExpect(status().isNotFound());
	}

	@Test
	void ladrilhoMarcadoContaComoOcupado() throws Exception {
		Construcao c = lenhadorPerto();
		marcar(usuario, c.getId(), 0, 1).andExpect(status().isCreated());
		org.junit.jupiter.api.Assertions.assertTrue(verificador.ocupado(vila.getId(), 1, 0, 1));
		org.junit.jupiter.api.Assertions.assertFalse(verificador.ocupado(vila.getId(), 1, 0, 0));
	}

}
