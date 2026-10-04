package com.example.loginbase.jogo.servico;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@Transactional
class BonusRegiaoServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired RegiaoBonusRepository regiaoBonusRepository;
	@Autowired BonusRegiaoService service;

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 123L, 1));
	}

	private Regiao regiao(Vila vila, int indice, TipoRegiao tipo, boolean possuida, Object... bonus) {
		Regiao r = new Regiao(vila.getId(), indice);
		r.setTipo(tipo);
		r.setPossuida(possuida);
		r = regiaoRepository.saveAndFlush(r);
		for (int i = 0; i < bonus.length; i += 2) {
			regiaoBonusRepository.saveAndFlush(
					new RegiaoBonus(r.getId(), (BonusRegiao) bonus[i], i / 2 + 1, (Integer) bonus[i + 1]));
		}
		return r;
	}

	private Vila vilaExemplo2() {
		Vila vila = novaVila();
		regiao(vila, 6, TipoRegiao.URBANA, true, BonusRegiao.COMERCIO, 47, BonusRegiao.INDUSTRIA, 30,
				BonusRegiao.DESENVOLVIMENTO, 12);
		regiao(vila, 7, TipoRegiao.LITORAL, true, BonusRegiao.SALINAS, 38, BonusRegiao.MILITAR, 20,
				BonusRegiao.ENXOFRE, 6);
		regiao(vila, 10, TipoRegiao.PLANICIE, true, BonusRegiao.CRIACOES, 41, BonusRegiao.FLORESTA, 25,
				BonusRegiao.PLANTACOES, 14);
		return vila;
	}

	@Test
	void exemplo2SomaCorretaComTrezeChaves() {
		Map<BonusRegiao, Integer> m = service.bonusDaVila(vilaExemplo2().getId());
		assertThat(m).hasSize(13);
		assertThat(m).containsEntry(BonusRegiao.COMERCIO, 47).containsEntry(BonusRegiao.CRIACOES, 41)
				.containsEntry(BonusRegiao.SALINAS, 38).containsEntry(BonusRegiao.INDUSTRIA, 30)
				.containsEntry(BonusRegiao.FLORESTA, 25).containsEntry(BonusRegiao.MILITAR, 20)
				.containsEntry(BonusRegiao.PLANTACOES, 14).containsEntry(BonusRegiao.DESENVOLVIMENTO, 12)
				.containsEntry(BonusRegiao.ENXOFRE, 6);
		for (BonusRegiao b : new BonusRegiao[] { BonusRegiao.BARREIRO, BonusRegiao.ROCHA, BonusRegiao.FERRO,
				BonusRegiao.CARVAO }) {
			assertThat(m).containsEntry(b, 0);
		}
	}

	@Test
	void regiaoNaoPossuidaNaoSoma() {
		Vila vila = vilaExemplo2();
		regiao(vila, 11, TipoRegiao.MONTANHA, false, BonusRegiao.FERRO, 44);
		assertThat(service.bonus(vila.getId(), BonusRegiao.FERRO)).isZero();
		assertThat(service.bonus(vila.getId(), BonusRegiao.COMERCIO)).isEqualTo(47);
	}

	@Test
	void fatorUsaBonusDaVila() {
		Vila vila = novaVila();
		regiao(vila, 1, TipoRegiao.FLORESTA, true, BonusRegiao.FLORESTA, 42);
		assertThat(service.fator(vila.getId(), BonusRegiao.FLORESTA)).isEqualTo(new BigDecimal("1.42"));
	}

	@Test
	void vilaSemBonusTudoZeroEFatorUm() {
		Vila vila = novaVila();
		Map<BonusRegiao, Integer> m = service.bonusDaVila(vila.getId());
		assertThat(m).hasSize(13);
		assertThat(m.values()).allMatch(v -> v == 0);
		assertThat(service.fator(vila.getId(), BonusRegiao.FLORESTA)).isEqualTo(new BigDecimal("1.00"));
	}

}
