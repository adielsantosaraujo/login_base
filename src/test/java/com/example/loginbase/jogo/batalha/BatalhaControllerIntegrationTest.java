package com.example.loginbase.jogo.batalha;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class BatalhaControllerIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired BatalhaRepository batalhaRepository;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private Vila vila(Usuario u) {
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private Batalha batalha(Vila v, int turno) {
		Batalha b = new Batalha();
		b.setVilaId(v.getId());
		b.setTropaNome("Tropa");
		b.setMasmorraId(9L);
		b.setMasmorraNivel(2);
		b.setRegiaoIndice(3);
		b.setTurno(turno);
		b.setSemente(1);
		b.setResultado(ResultadoCombate.VITORIA);
		b.setRodadas(2);
		// ids colidem de propósito: cidadão 0 e inimigo 0
		b.setLog(new LogBatalha(List.of(
				new ResultadoBatalha.PvFinal(0, LadoCombate.TROPA, "Ana", 20, 15),
				new ResultadoBatalha.PvFinal(0, LadoCombate.INIMIGO, "Goblin", 10, 0)),
				List.of(new AcaoBatalha(1, 0, LadoCombate.TROPA, 0, LadoCombate.INIMIGO, 6, false, 10, 4, false),
						new AcaoBatalha(1, 0, LadoCombate.INIMIGO, 0, LadoCombate.TROPA, 5, false, 20, 15, false),
						new AcaoBatalha(2, 0, LadoCombate.TROPA, 0, LadoCombate.INIMIGO, 9, true, 4, 0, true))));
		return batalhaRepository.saveAndFlush(b);
	}

	private ResultActions obter(Usuario u, String url) throws Exception {
		return mvc.perform(get(url).with(user(u.getEmail()).roles("USER")));
	}

	@Test
	void listaOrdenadaPorTurnoDesc() throws Exception {
		Usuario u = novoUsuario();
		Vila v = vila(u);
		batalha(v, 5);
		batalha(v, 12);
		batalha(v, 8);
		Usuario outro = novoUsuario();
		batalha(vila(outro), 99);
		obter(u, "/api/jogo/batalhas").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].turno").value(12)).andExpect(jsonPath("$[1].turno").value(8))
				.andExpect(jsonPath("$[2].turno").value(5)).andExpect(jsonPath("$[0].resultado").value("VITORIA"))
				.andExpect(jsonPath("$[0].tropaNome").value("Tropa")).andExpect(jsonPath("$[0].masmorraNivel").value(2));
	}

	@Test
	void vilaSemBatalhasRetornaListaVazia() throws Exception {
		Usuario u = novoUsuario();
		vila(u);
		obter(u, "/api/jogo/batalhas").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void detalheAgrupaPorRodadaComNomes() throws Exception {
		Usuario u = novoUsuario();
		Batalha b = batalha(vila(u), 7);
		obter(u, "/api/jogo/batalhas/" + b.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(b.getId())).andExpect(jsonPath("$.totalRodadas").value(2))
				.andExpect(jsonPath("$.recompensas").doesNotExist())
				.andExpect(jsonPath("$.participantes", hasSize(2)))
				.andExpect(jsonPath("$.participantes[0].pvFinal").value(15))
				.andExpect(jsonPath("$.rodadas", hasSize(2))).andExpect(jsonPath("$.rodadas[0].numero").value(1))
				.andExpect(jsonPath("$.rodadas[0].acoes", hasSize(2)))
				.andExpect(jsonPath("$.rodadas[0].acoes[0].atacante.nome").value("Ana"))
				.andExpect(jsonPath("$.rodadas[0].acoes[0].alvo.nome").value("Goblin"))
				.andExpect(jsonPath("$.rodadas[0].acoes[1].atacante.nome").value("Goblin"))
				.andExpect(jsonPath("$.rodadas[0].acoes[1].alvo.nome").value("Ana"))
				.andExpect(jsonPath("$.rodadas[1].acoes[0].critico").value(true))
				.andExpect(jsonPath("$.rodadas[1].acoes[0].abatido").value(true))
				.andExpect(jsonPath("$.rodadas[1].acoes[0].pvAntes").value(4))
				.andExpect(jsonPath("$.rodadas[1].acoes[0].pvDepois").value(0));
	}

	@Test
	void detalheDeOutraVilaOuInexistenteRetorna404() throws Exception {
		Usuario u = novoUsuario();
		vila(u);
		Batalha alheia = batalha(vila(novoUsuario()), 3);
		obter(u, "/api/jogo/batalhas/" + alheia.getId()).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.erro").value("Batalha não encontrada"));
		obter(u, "/api/jogo/batalhas/999999999").andExpect(status().isNotFound())
				.andExpect(jsonPath("$.erro").value("Batalha não encontrada"));
	}
}
