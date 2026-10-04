package com.example.loginbase.jogo.repositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.excecao.VilaNaoEncontradaException;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.RegiaoTerreno;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.modelo.VilaPrevia;
import com.example.loginbase.jogo.servico.VilaAtual;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ModeloJogoBaseIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired JogoTurnoRepository jogoTurnoRepository;
	@Autowired VilaPreviaRepository vilaPreviaRepository;
	@Autowired RegiaoTerrenoRepository regiaoTerrenoRepository;
	@Autowired LadrilhoRepository ladrilhoRepository;
	@Autowired VilaAtual vilaAtual;
	@Autowired EntityManager em;

	private Usuario novoUsuario() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		return usuarioRepository.saveAndFlush(u);
	}

	private Vila novaVila() {
		return vilaRepository.saveAndFlush(new Vila(novoUsuario().getId(), "Vila", 123L, 1));
	}

	@Test
	void recuperaVilaPorUsuarioEVersionInicia0() {
		Vila vila = novaVila();
		em.clear();
		Vila lida = vilaRepository.findByUsuarioId(vila.getUsuarioId()).orElseThrow();
		assertThat(lida.getVersion()).isZero();
		assertThat(vilaRepository.existsByUsuarioId(vila.getUsuarioId())).isTrue();
	}

	@Test
	void usuarioSoPodeTerUmaVila() {
		Vila vila = novaVila();
		assertThatThrownBy(() -> vilaRepository.saveAndFlush(new Vila(vila.getUsuarioId(), "Outra", 1L, 1)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void dezesseisRegioesEIndiceForaDoIntervaloRejeitado() {
		Vila vila = novaVila();
		for (int i = 1; i <= 16; i++) {
			regiaoRepository.save(new Regiao(vila.getId(), i));
		}
		regiaoRepository.flush();
		assertThat(regiaoRepository.findAllByVilaId(vila.getId())).hasSize(16);
		Regiao r = regiaoRepository.findByVilaIdAndIndice(vila.getId(), 7).orElseThrow();
		assertThat(r.getTipo()).isNull();
		assertThat(r.isPossuida()).isFalse();

		assertThatThrownBy(() -> regiaoRepository.saveAndFlush(new Regiao(vila.getId(), 17)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void indiceDuplicadoNaMesmaVilaRejeitado() {
		Vila vila = novaVila();
		regiaoRepository.saveAndFlush(new Regiao(vila.getId(), 1));
		assertThatThrownBy(() -> regiaoRepository.saveAndFlush(new Regiao(vila.getId(), 1)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Regiao novaRegiaoPossuida() {
		Regiao r = new Regiao(novaVila().getId(), 1);
		r.setTipo(TipoRegiao.MONTANHA);
		r.setPossuida(true);
		return regiaoRepository.saveAndFlush(r);
	}

	@Test
	void persisteELeTerrenosDaRegiaoEmOrdemDePosicao() {
		Regiao r = novaRegiaoPossuida();
		regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(r.getId(), TipoTerreno.CARVAO, 3, 15));
		regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(r.getId(), TipoTerreno.ROCHA, 1, 50));
		regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(r.getId(), TipoTerreno.FERRO, 2, 35));
		em.clear();

		assertThat(regiaoTerrenoRepository.findByRegiaoIdOrderByPosicao(r.getId()))
				.extracting(RegiaoTerreno::getTerreno, RegiaoTerreno::getPosicao, RegiaoTerreno::getPercentual)
				.containsExactly(
						org.assertj.core.groups.Tuple.tuple(TipoTerreno.ROCHA, 1, 50),
						org.assertj.core.groups.Tuple.tuple(TipoTerreno.FERRO, 2, 35),
						org.assertj.core.groups.Tuple.tuple(TipoTerreno.CARVAO, 3, 15));
		assertThat(regiaoTerrenoRepository.findByRegiaoIdIn(java.util.List.of(r.getId()))).hasSize(3);
	}

	@Test
	void terrenoDaRegiaoRejeitaPercentualPosicaoEDuplicata() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		assertThatThrownBy(() -> regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(id, TipoTerreno.ROCHA, 1, 61)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void terrenoDaRegiaoRejeitaPosicao3Com9() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		assertThatThrownBy(() -> regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(id, TipoTerreno.ROCHA, 3, 9)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void terrenoDaRegiaoRejeitaTerrenoOuPosicaoDuplicados() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(id, TipoTerreno.ROCHA, 1, 40));
		assertThatThrownBy(() -> regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(id, TipoTerreno.ROCHA, 2, 30)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void terrenoDaRegiaoRejeitaPosicaoDuplicada() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(id, TipoTerreno.ROCHA, 1, 40));
		assertThatThrownBy(() -> regiaoTerrenoRepository.saveAndFlush(new RegiaoTerreno(id, TipoTerreno.FERRO, 1, 30)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void persisteELadrilhosComBonusTotalEConsultas() {
		Regiao r = novaRegiaoPossuida();
		ladrilhoRepository.save(new Ladrilho(r.getId(), 1, 0, TipoTerreno.ROCHA, 40, 25));
		ladrilhoRepository.save(new Ladrilho(r.getId(), 0, 1, TipoTerreno.ROCHA, 30, 0));
		ladrilhoRepository.save(new Ladrilho(r.getId(), 5, 0, TipoTerreno.FERRO, 10, 100));
		ladrilhoRepository.save(new Ladrilho(r.getId(), 0, 0, TipoTerreno.ROCHA, 20, 50));
		ladrilhoRepository.flush();
		em.clear();

		assertThat(ladrilhoRepository.existsByRegiaoId(r.getId())).isTrue();
		assertThat(ladrilhoRepository.findByRegiaoIdAndXAndY(r.getId(), 1, 0).orElseThrow().getBonusTotal()).isEqualTo(65);
		assertThat(ladrilhoRepository.findByRegiaoIdAndXAndY(r.getId(), 9, 9)).isEmpty();
		assertThat(ladrilhoRepository.findByRegiaoIdOrderByYAscXAsc(r.getId()))
				.extracting(l -> l.getX() + "," + l.getY()).containsExactly("0,0", "1,0", "5,0", "0,1");
		assertThat(ladrilhoRepository.findByRegiaoIdAndTerrenoOrderByYAscXAsc(r.getId(), TipoTerreno.ROCHA))
				.extracting(l -> l.getX() + "," + l.getY()).containsExactly("0,0", "1,0", "0,1");
	}

	@Test
	void ladrilhoRejeitaBonusFolgaEPosicaoDuplicada() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		assertThatThrownBy(() -> ladrilhoRepository.saveAndFlush(new Ladrilho(id, 0, 0, TipoTerreno.ROCHA, 10, 30)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void ladrilhoRejeitaBonusBase101() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		assertThatThrownBy(() -> ladrilhoRepository.saveAndFlush(new Ladrilho(id, 0, 0, TipoTerreno.ROCHA, 101, 0)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void ladrilhoRejeitaXForaDaGrade() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		assertThatThrownBy(() -> ladrilhoRepository.saveAndFlush(new Ladrilho(id, 10, 0, TipoTerreno.ROCHA, 10, 0)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void ladrilhoRejeitaPosicaoDuplicada() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		ladrilhoRepository.saveAndFlush(new Ladrilho(id, 2, 3, TipoTerreno.ROCHA, 10, 0));
		assertThatThrownBy(() -> ladrilhoRepository.saveAndFlush(new Ladrilho(id, 2, 3, TipoTerreno.FERRO, 10, 0)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void terrenoInvalidoViaSqlNativoRejeitado() {
		Regiao r = novaRegiaoPossuida();
		Long id = r.getId();
		assertThatThrownBy(() -> em.createNativeQuery("insert into ladrilho (regiao_id, x, y, terreno, bonus_base, "
				+ "bonus_adjacente) values (:r, 0, 0, 'LAVA', 10, 0)").setParameter("r", id).executeUpdate())
				.isInstanceOf(Exception.class);
	}

	@Test
	void persisteELeVilaPrevia() {
		Usuario u = novoUsuario();
		UUID previaId = UUID.randomUUID();
		vilaPreviaRepository.saveAndFlush(new VilaPrevia(u.getId(), previaId, 99L, 1));
		em.clear();

		VilaPrevia lida = vilaPreviaRepository.findById(u.getId()).orElseThrow();
		assertThat(lida.getPreviaId()).isEqualTo(previaId);
		assertThat(lida.getSemente()).isEqualTo(99L);
		assertThat(lida.getRodada()).isEqualTo(1);
		assertThat(lida.getCriadoEm()).isNotNull();
		assertThat(vilaPreviaRepository.existsById(u.getId())).isTrue();
		vilaPreviaRepository.deleteById(u.getId());
		vilaPreviaRepository.flush();
		assertThat(vilaPreviaRepository.existsById(u.getId())).isFalse();
	}

	@Test
	void persisteTurno() {
		int numero = 987654;
		jogoTurnoRepository.saveAndFlush(new com.example.loginbase.jogo.modelo.JogoTurno(numero, java.time.Instant.now()));
		assertThat(jogoTurnoRepository.existsByNumero(numero)).isTrue();
		assertThat(jogoTurnoRepository.findByNumero(numero)).isPresent();
	}

	@Test
	void persisteConstrucaoCasa() {
		Vila vila = novaVila();
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.CASA);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(6);
		c.setX(0);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(4);
		c.setPoAtual(4);
		construcaoRepository.saveAndFlush(c);
		em.clear();

		assertThat(construcaoRepository.findByVilaId(vila.getId())).hasSize(1);
		Construcao lida = construcaoRepository.findByVilaIdAndRegiaoIndice(vila.getId(), 6).get(0);
		assertThat(lida.getEstado()).isEqualTo(EstadoConstrucao.ATIVA);
		assertThat(lida.getCriadoEm()).isNotNull();
	}

	@Test
	void vilaAtualRetornaVilaDoUsuarioLogado() {
		Usuario u = novoUsuario();
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Minha", 5L, 1));
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken(u.getEmail(), "x", "ROLE_USER"));
		try {
			assertThat(vilaAtual.obter().getId()).isEqualTo(vila.getId());
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void vilaAtualSemVilaLanca404() {
		Usuario u = novoUsuario();
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken(u.getEmail(), "x", "ROLE_USER"));
		try {
			assertThatThrownBy(() -> vilaAtual.obter())
					.isInstanceOf(VilaNaoEncontradaException.class)
					.extracting(e -> ((VilaNaoEncontradaException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
