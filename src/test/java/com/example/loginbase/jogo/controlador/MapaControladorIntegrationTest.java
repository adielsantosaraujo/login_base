package com.example.loginbase.jogo.controlador;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MapaControladorIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired RegiaoBonusRepository regiaoBonusRepository;
	@Autowired com.example.loginbase.jogo.masmorra.MasmorraRepository masmorraRepository;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private Vila vilaCom(Usuario u) {
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila Teste", 42L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i == 6) {
				r.setPossuida(true);
				r.setTipo(TipoRegiao.URBANA);
			}
			else if (i == 7) {
				r.setPossuida(true);
				r.setTipo(TipoRegiao.MONTANHA);
			}
			else if (i == 10) {
				r.setPossuida(true);
				r.setTipo(TipoRegiao.FLORESTA);
			}
			// região 1 (não possuída): tipo e bônus também são exibidos
			if (i == 1) {
				r.setTipo(TipoRegiao.LITORAL);
			}
			r = regiaoRepository.save(r);
			regiaoBonusRepository.save(new RegiaoBonus(r.getId(), BonusRegiao.SALINAS, 1, 40));
			regiaoBonusRepository.save(new RegiaoBonus(r.getId(), BonusRegiao.ENXOFRE, 2, 20));
			regiaoBonusRepository.save(new RegiaoBonus(r.getId(), BonusRegiao.MILITAR, 3, 10));
		}
		regiaoRepository.flush();
		return vila;
	}

	private void casa(Vila vila, int regiao, int x, int y) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.CASA);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(regiao);
		c.setX(x);
		c.setY(y);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(0);
		c.setPoAtual(0);
		construcaoRepository.saveAndFlush(c);
	}

	@Test
	void semAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/jogo/vila/mapa")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/jogo/regioes/6")).andExpect(status().isUnauthorized());
	}

	@Test
	void semVilaDevolve404() throws Exception {
		Usuario u = novoUsuario();
		mvc.perform(get("/api/jogo/vila/mapa").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/jogo/regioes/6").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
	}

	@Test
	void mapaTem16RegioesE3Possuidas() throws Exception {
		Usuario u = novoUsuario();
		vilaCom(u);
		mvc.perform(get("/api/jogo/vila/mapa").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vila.nome").value("Vila Teste"))
				.andExpect(jsonPath("$.regioes", hasSize(16)))
				.andExpect(jsonPath("$.regioes[?(@.possuida == true)]", hasSize(3)))
				.andExpect(jsonPath("$.regioes[5].indice").value(6))
				.andExpect(jsonPath("$.regioes[5].tipo").value("URBANA"))
				.andExpect(jsonPath("$.regioes[6].tipo").value("MONTANHA"))
				.andExpect(jsonPath("$.regioes[9].tipo").value("FLORESTA"))
				.andExpect(jsonPath("$.regioes[0].tipo").value("LITORAL"))
				.andExpect(jsonPath("$.regioes[0].possuida").value(false))
				.andExpect(jsonPath("$.regioes[0].bonus", hasSize(3)))
				.andExpect(jsonPath("$.regioes[0].bonus[0].bonus").value("SALINAS"))
				.andExpect(jsonPath("$.regioes[0].bonus[0].valor").value(40))
				.andExpect(jsonPath("$.regioes[1].tipo").value(nullValue()))
				.andExpect(jsonPath("$.regioes[1].bonus", hasSize(3)))
				.andExpect(jsonPath("$.vila.bonusRegiao.length()").value(13))
				.andExpect(jsonPath("$.vila.bonusRegiao.SALINAS").value(120))
				.andExpect(jsonPath("$.vila.bonusRegiao.FERRO").value(0))
				.andExpect(jsonPath("$.regioes[5].masmorraAtiva").value(false));
	}

	@Test
	void regiaoNaoPossuidaComMasmorraMostraNivelEId() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vilaCom(u);
		var m = masmorraRepository.saveAndFlush(
				new com.example.loginbase.jogo.masmorra.Masmorra(vila.getId(), 3, 4, 12));
		mvc.perform(get("/api/jogo/vila/mapa").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.regioes[2].possuida").value(false))
				.andExpect(jsonPath("$.regioes[2].masmorraAtiva").value(true))
				.andExpect(jsonPath("$.regioes[2].nivelMasmorra").value(4))
				.andExpect(jsonPath("$.regioes[2].masmorraId").value(m.getId()))
				.andExpect(jsonPath("$.regioes[3].masmorraAtiva").value(false))
				.andExpect(jsonPath("$.regioes[3].masmorraId").value(nullValue()));
	}

	@Test
	void regiaoPossuidaTem100LadrilhosComCasas() throws Exception {
		Usuario u = novoUsuario();
		Vila vila = vilaCom(u);
		for (int x : new int[] { 0, 2, 4, 6 }) {
			casa(vila, 6, x, 0);
		}
		mvc.perform(get("/api/jogo/regioes/6").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.regiao.indice").value(6))
				.andExpect(jsonPath("$.regiao.possuida").value(true))
				.andExpect(jsonPath("$.ladrilhos", hasSize(100)))
				.andExpect(jsonPath("$.ladrilhos[?(@.construcao != null)]", hasSize(4)))
				.andExpect(jsonPath("$.ladrilhos[0].construcao.tipo").value("CASA"))
				.andExpect(jsonPath("$.ladrilhos[2].construcao.nivel").value("N1"))
				.andExpect(jsonPath("$.ladrilhos[1].construcao").value(nullValue()));
	}

	@Test
	void regiaoDeColetaTemJazidas() throws Exception {
		Usuario u = novoUsuario();
		vilaCom(u);
		mvc.perform(get("/api/jogo/regioes/7").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ladrilhos", hasSize(100)))
				.andExpect(jsonPath("$.regiao.tipo").value("MONTANHA"))
				.andExpect(jsonPath("$.regiao.bonus", hasSize(3)))
				.andExpect(jsonPath("$.ladrilhos[?(@.jazida != null)]", hasSize(100)));
	}

	@Test
	void regiaoUrbanaNaoGeraJazidas() throws Exception {
		Usuario u = novoUsuario();
		vilaCom(u);
		mvc.perform(get("/api/jogo/regioes/6").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.ladrilhos[?(@.jazida != null)]", hasSize(0)));
	}

	@Test
	void regiaoNaoPossuidaDevolve200ComPossuidaFalse() throws Exception {
		Usuario u = novoUsuario();
		vilaCom(u);
		mvc.perform(get("/api/jogo/regioes/1").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.regiao.possuida").value(false))
				.andExpect(jsonPath("$.regiao.tipo").value("LITORAL"))
				.andExpect(jsonPath("$.regiao.bonus", hasSize(3)))
				.andExpect(jsonPath("$.ladrilhos", hasSize(0)));
	}

	@Test
	void indiceInvalidoDevolve404() throws Exception {
		Usuario u = novoUsuario();
		vilaCom(u);
		mvc.perform(get("/api/jogo/regioes/17").with(user(u.getEmail()).roles("USER")))
				.andExpect(status().isNotFound());
	}

}
