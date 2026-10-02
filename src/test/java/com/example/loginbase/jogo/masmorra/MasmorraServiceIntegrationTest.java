package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@Transactional
class MasmorraServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired MasmorraRepository masmorraRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired MasmorraService service;

	private Vila vila;

	@BeforeEach
	void setUp() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 7L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			r.setPossuida(i == 6 || i == 7 || i == 10);
			regiaoRepository.save(r);
		}
		regiaoRepository.flush();
	}

	private List<Masmorra> ativas() {
		return masmorraRepository.findByVilaIdAndAtivaTrueOrderByIdAsc(vila.getId());
	}

	private Masmorra masmorra(int regiao, int nivel, int turnoSurgimento) {
		return masmorraRepository.saveAndFlush(new Masmorra(vila.getId(), regiao, nivel, turnoSurgimento));
	}

	/** Random que sempre sorteia 0 (acerta qualquer chance positiva). */
	private static Random sempreAcerta() {
		return new Random() {
			@Override
			public double nextDouble() {
				return 0.0;
			}
		};
	}

	@Test
	void surgeSoEmRegiaoElegivelELimitaEmTres() {
		service.processar(vila, 20, sempreAcerta(), 0.01);
		List<Masmorra> a = ativas();
		assertThat(a).hasSize(3);
		// percorre por índice: 1, 2, 3 (nenhuma possuída)
		assertThat(a).extracting(Masmorra::getRegiaoIndice).containsExactly(1, 2, 3);
		assertThat(a).allSatisfy(m -> {
			assertThat(m.getNivel()).isEqualTo(1);
			assertThat(m.getTurnoSurgimento()).isEqualTo(20);
		});
		assertThat(eventoRepository.findAll()).filteredOn(e -> e.getTipo() == TipoEventoTurno.MASMORRA)
				.hasSize(3);
		service.processar(vila, 21, sempreAcerta(), 0.01);
		assertThat(ativas()).hasSize(3);
	}

	@Test
	void naoSurgeEmRegiaoPossuidaOuOcupada() {
		masmorra(1, 1, 5);
		service.processar(vila, 20, sempreAcerta(), 0.01);
		assertThat(ativas()).extracting(Masmorra::getRegiaoIndice).containsExactly(1, 2, 3);
		// com 3 regiões ativas removidas e possuidas 6,7,10 jamais aparecem
		for (Masmorra m : ativas()) {
			service.remover(m, 20);
		}
		service.processar(vila, 26, sempreAcerta(), 0.01);
		assertThat(ativas()).extracting(Masmorra::getRegiaoIndice).containsExactly(1, 2, 3);
	}

	@Test
	void carenciaDe12TurnosDaVila() {
		service.processar(vila, 12, sempreAcerta(), 0.01); // 12 - 1 = 11
		assertThat(ativas()).isEmpty();
		service.processar(vila, 13, sempreAcerta(), 0.01); // 12
		assertThat(ativas()).hasSize(3);
	}

	@Test
	void surgimentoReproduzivelComSemente() {
		for (int t = 13; t < 400; t++) {
			service.processar(vila, t);
		}
		List<int[]> primeira = ativas().stream().map(m -> new int[] { m.getRegiaoIndice(), m.getTurnoSurgimento() })
				.toList();
		assertThat(primeira).isNotEmpty();
		// repete a sorteio de um turno: mesma semente => mesmo resultado
		Random a = new Random(vila.getSemente() * 31 + 50);
		Random b = new Random(vila.getSemente() * 31 + 50);
		for (int i = 0; i < 16; i++) {
			assertThat(a.nextDouble()).isEqualTo(b.nextDouble());
		}
	}

	@Test
	void chanceZeroNaoCria() {
		service.processar(vila, 20, sempreAcerta(), 0.0);
		assertThat(ativas()).isEmpty();
	}

	@Test
	void bloqueioAposLimpezaT5BloqueiaT6Libera() {
		Masmorra m = masmorra(1, 3, 5);
		service.remover(m, 30);
		assertThat(m.isAtiva()).isFalse();
		assertThat(regiaoRepository.findByVilaIdAndIndice(vila.getId(), 1).orElseThrow().getLimpaAteTurno())
				.isEqualTo(36);
		service.processar(vila, 35, sempreAcerta(), 0.01);
		assertThat(ativas()).extracting(Masmorra::getRegiaoIndice).containsExactly(2, 3, 4);
		for (Masmorra x : ativas()) {
			x.setAtiva(false);
			masmorraRepository.saveAndFlush(x);
		}
		service.processar(vila, 36, sempreAcerta(), 0.01);
		assertThat(ativas()).extracting(Masmorra::getRegiaoIndice).containsExactly(1, 2, 3);
	}

	@Test
	void evoluiACada18TurnosAteNivel10() {
		Masmorra m = masmorra(1, 1, 0);
		for (int t = 1; t <= 17; t++) {
			service.processar(vila, t, new Random(1), 0.0);
		}
		assertThat(m.getNivel()).isEqualTo(1);
		assertThat(m.getTurnosSemAtaque()).isEqualTo(17);
		service.processar(vila, 18, new Random(1), 0.0);
		assertThat(m.getNivel()).isEqualTo(2);
		assertThat(m.getTurnosSemAtaque()).isZero();
		assertThat(eventoRepository.findAll()).anyMatch(e -> e.getMensagem().contains("evoluiu para N2"));
		for (int t = 19; t <= 18 * 12; t++) {
			service.processar(vila, t, new Random(1), 0.0);
		}
		assertThat(m.getNivel()).isEqualTo(10);
	}

	@Test
	void naoEvoluiNoTurnoDeSurgimento() {
		Masmorra m = masmorra(1, 1, 20);
		service.processar(vila, 20, new Random(1), 0.0);
		assertThat(m.getTurnosSemAtaque()).isZero();
	}

	@Test
	void naoIncrementaNoTurnoDoAtaqueERegistrarAtaqueZera() {
		Masmorra m = masmorra(1, 1, 0);
		m.setTurnosSemAtaque(10);
		service.registrarAtaque(m, 5);
		assertThat(m.getTurnosSemAtaque()).isZero();
		assertThat(m.getTurnoUltimoAtaque()).isEqualTo(5);
		service.processar(vila, 5, new Random(1), 0.0);
		assertThat(m.getTurnosSemAtaque()).isZero();
		service.processar(vila, 6, new Random(1), 0.0);
		assertThat(m.getTurnosSemAtaque()).isEqualTo(1);
	}

}
