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
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Jazida;
import com.example.loginbase.jogo.modelo.LadrilhoJazida;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
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
	@Autowired LadrilhoJazidaRepository ladrilhoJazidaRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired JogoTurnoRepository jogoTurnoRepository;
	@Autowired RegiaoBonusRepository regiaoBonusRepository;
	@Autowired VilaPreviaRepository vilaPreviaRepository;
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

	@Test
	void ladrilhoJazidaChaveCompostaEPersistencia() {
		Vila vila = novaVila();
		Regiao r = new Regiao(vila.getId(), 1);
		r.setTipo(TipoRegiao.FLORESTA);
		r.setPossuida(true);
		r = regiaoRepository.saveAndFlush(r);

		ladrilhoJazidaRepository.saveAndFlush(new LadrilhoJazida(r.getId(), 0, 0, Jazida.FLORESTA));
		ladrilhoJazidaRepository.saveAndFlush(new LadrilhoJazida(r.getId(), 0, 1, Jazida.ROCHA));
		em.clear();
		assertThat(ladrilhoJazidaRepository.findAllByRegiaoId(r.getId())).hasSize(2);
		assertThat(ladrilhoJazidaRepository.findById(new LadrilhoJazida.Chave(r.getId(), 0, 1)))
				.get().extracting(LadrilhoJazida::getJazida).isEqualTo(Jazida.ROCHA);

		Long regiaoId = r.getId();
		assertThatThrownBy(() -> {
			em.createNativeQuery("insert into ladrilho_jazida (regiao_id, x, y, jazida) values (:r, 0, 0, 'ROCHA')")
					.setParameter("r", regiaoId).executeUpdate();
		}).isInstanceOf(Exception.class);
	}

	@Test
	void ladrilhoForaDaGradeRejeitado() {
		Vila vila = novaVila();
		Regiao r = regiaoRepository.saveAndFlush(new Regiao(vila.getId(), 1));
		assertThatThrownBy(() -> ladrilhoJazidaRepository.saveAndFlush(new LadrilhoJazida(r.getId(), 10, 0, Jazida.CAMPO)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void persisteELeBonusDeRegiaoEConstraints() {
		Vila vila = novaVila();
		Regiao r = new Regiao(vila.getId(), 1);
		r.setTipo(TipoRegiao.MONTANHA);
		r.setPossuida(true);
		r = regiaoRepository.saveAndFlush(r);
		regiaoBonusRepository.saveAndFlush(new RegiaoBonus(r.getId(), BonusRegiao.ROCHA, 1, 44));
		regiaoBonusRepository.saveAndFlush(new RegiaoBonus(r.getId(), BonusRegiao.FERRO, 2, 20));
		regiaoBonusRepository.saveAndFlush(new RegiaoBonus(r.getId(), BonusRegiao.CARVAO, 3, 9));
		em.clear();

		var lidos = regiaoBonusRepository.findByRegiaoIdOrderByPosicao(r.getId());
		assertThat(lidos).extracting(RegiaoBonus::getBonus)
				.containsExactly(BonusRegiao.ROCHA, BonusRegiao.FERRO, BonusRegiao.CARVAO);
		assertThat(regiaoBonusRepository.findByRegiaoIdIn(java.util.List.of(r.getId()))).hasSize(3);
		var somas = regiaoBonusRepository.somarBonusPossuidos(vila.getId());
		assertThat(somas).hasSize(3);
		assertThat(somas).filteredOn(t -> t.getBonus() == BonusRegiao.ROCHA).first()
				.extracting(t -> t.getTotal()).isEqualTo(44L);

		Long regiaoId = r.getId();
		assertThatThrownBy(() -> regiaoBonusRepository.saveAndFlush(new RegiaoBonus(regiaoId, BonusRegiao.ENXOFRE, 1, 20)))
				.isInstanceOf(DataIntegrityViolationException.class);
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
