package com.example.loginbase.jogo.batalha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Random;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.catalogo.Alcance;
import com.example.loginbase.jogo.item.catalogo.ArmaCatalogo;
import com.example.loginbase.jogo.item.catalogo.ArmaduraCatalogo;

class CombateAtributosTest {

	private final CombateAtributos calc = new CombateAtributos();

	// FOR 6, VIT 5, VEL 5, G = 5 + 1 + 1 + 1 = 8
	private EntradaCombate base(ArmaCatalogo arma, int nivelArma, double defPecas, int iniArm, int atkPct) {
		return new EntradaCombate(5, 6, 5, 0, 8, 0, arma, nivelArma, defPecas, iniArm, atkPct, 0, 0, 0);
	}

	@Test
	void exemplo104() {
		AtributosCombate a = calc.calcular(base(ArmaCatalogo.ESPADA, 1, 0, 0, 0));
		assertThat(a.ataque()).isCloseTo(29, within(1e-9));
		assertThat(a.defesa()).isCloseTo(15, within(1e-9));
		assertThat(a.pvMax()).isEqualTo(79);
		// 2 x 5 + 8 + 0 = 18 (o doc dizia 10, valor incorreto)
		assertThat(a.iniciativaBase()).isEqualTo(18);
		assertThat(a.criticoPp()).isCloseTo(7.5, within(1e-9));
	}

	@Test
	void comPeitoralL1() {
		AtributosCombate a = calc.calcular(base(ArmaCatalogo.ESPADA, 1, ArmaduraCatalogo.PEITORAL.defesa(1), 0, 0));
		assertThat(a.defesa()).isCloseTo(23, within(1e-9));
	}

	@Test
	void conjuntoCompleto() {
		double soma = 0;
		int ini = 0;
		for (ArmaduraCatalogo p : ArmaduraCatalogo.values()) {
			soma += p.defesa(1);
			ini += p.iniciativaExtra();
		}
		assertThat(soma).isCloseTo(24, within(1e-9));
		AtributosCombate a = calc.calcular(base(ArmaCatalogo.ESPADA, 1, soma, ini, 0));
		assertThat(a.defesa()).isCloseTo(24 + 5 + 8 + 2, within(1e-9));
		assertThat(a.iniciativaBase()).isEqualTo(19); // Sapato +1

		double l5 = 0;
		for (ArmaduraCatalogo p : ArmaduraCatalogo.values()) {
			l5 += p.defesa(5);
		}
		assertThat(l5).isCloseTo(43.2, within(1e-9));
		assertThat(ArmaduraCatalogo.PEITORAL.defesa(5) + ArmaduraCatalogo.LUVAS.defesa(5))
				.isCloseTo(18, within(1e-9));
	}

	@Test
	void bonusAtkPercentual() {
		AtributosCombate a = calc.calcular(base(ArmaCatalogo.ESPADA, 1, 0, 0, 3));
		assertThat(a.ataque()).isCloseTo(29 * 1.03, within(1e-9));
	}

	@Test
	void lancaUsaMediaDeForcaEVel() {
		// (9 x (1 + 0,05 x 5,5) + 16) = 9 x 1,275 + 16
		AtributosCombate a = calc.calcular(base(ArmaCatalogo.LANCA, 1, 0, 0, 0));
		assertThat(a.ataque()).isCloseTo(9 * 1.275 + 16, within(1e-9));
		assertThat(a.defesa()).isCloseTo(13, within(1e-9)); // sem +2 de espada
		assertThat(a.iniciativaBase()).isEqualTo(19);
	}

	@Test
	void bestaUsaIntEIniciativaNegativa() {
		EntradaCombate e = new EntradaCombate(5, 6, 5, 10, 8, 0, ArmaCatalogo.BESTA, 1, 0, 0, 0, 0, 0, 0);
		AtributosCombate a = calc.calcular(e);
		assertThat(a.ataque()).isCloseTo(13 * 1.5 + 16, within(1e-9));
		assertThat(a.iniciativaBase()).isEqualTo(14);
	}

	@Test
	void bonusDefIniECrit() {
		EntradaCombate e = new EntradaCombate(5, 6, 5, 0, 8, 10, ArmaCatalogo.ESPADA, 1, 0, 0, 0, 10, 2, 3);
		AtributosCombate a = calc.calcular(e);
		assertThat(a.pvMax()).isEqualTo(89);
		assertThat(a.defesa()).isCloseTo(15 * 1.10, within(1e-9));
		assertThat(a.iniciativaBase()).isEqualTo(20);
		assertThat(a.criticoPp()).isCloseTo(10.5, within(1e-9));
	}

	@Test
	void danoExemplo104() {
		// 29 x 100 / 124 = 23,4; U in [0,9;1,1) -> [21,0; 25,7)
		for (long s = 0; s < 200; s++) {
			int d = CalculoDano.dano(29, 8, new Random(s));
			assertThat(d).isBetween(21, 26);
		}
		// Goblin ATQ 16 vs DEF 15: 16 x 100 / 145 = 11,03
		for (long s = 0; s < 200; s++) {
			assertThat(CalculoDano.dano(16, 15, new Random(s))).isBetween(10, 12);
		}
	}

	@Test
	void danoComArmaduraCompleta() {
		for (long s = 0; s < 200; s++) {
			// 25 x 100 / 172 = 14,5 -> [13,1; 16]
			assertThat(CalculoDano.dano(25, 24, new Random(s))).isBetween(13, 16);
		}
	}

	@Test
	void danoMinimoUm() {
		assertThat(CalculoDano.dano(1, 500, new Random(1))).isEqualTo(1);
		assertThat(CalculoDano.dano(0, 0, new Random(1))).isEqualTo(1);
	}

	@Test
	void mesmaSementeMesmoDano() {
		assertThat(CalculoDano.dano(29, 8, new Random(42))).isEqualTo(CalculoDano.dano(29, 8, new Random(42)));
	}

	@Test
	void defesaEfetivaBestaEXama() {
		assertThat(CalculoDano.defesaEfetiva(40, true)).isCloseTo(30, within(1e-9));
		assertThat(CalculoDano.defesaEfetiva(40, false)).isCloseTo(40, within(1e-9));
		Combatente xama = TipoInimigo.XAMA_ORC.criar(0, 1);
		assertThat(xama.ignoraDefesa25()).isTrue();
		assertThat(TipoInimigo.GOBLIN.criar(1, 1).ignoraDefesa25()).isFalse();
	}

	@Test
	void multiplicadorDeInimigo() {
		assertThat(TipoInimigo.multiplicador(1)).isCloseTo(1.0, within(1e-9));
		assertThat(TipoInimigo.multiplicador(5)).isCloseTo(1.6, within(1e-9));
		assertThat(TipoInimigo.multiplicador(10)).isCloseTo(2.35, within(1e-9));
		AtributosCombate goblin = TipoInimigo.GOBLIN.atributos(5);
		assertThat(goblin.pvMax()).isEqualTo(64);
		assertThat(goblin.ataque()).isCloseTo(25.6, within(1e-9));
		assertThat(goblin.defesa()).isCloseTo(12.8, within(1e-9));
	}

	@Test
	void inimigoCombatente() {
		Combatente arq = TipoInimigo.GOBLIN_ARQUEIRO.criar(3, 1);
		assertThat(arq.lado()).isEqualTo(LadoCombate.INIMIGO);
		assertThat(arq.linha()).isEqualTo(LinhaCombate.RETAGUARDA);
		assertThat(arq.alcance()).isEqualTo(Alcance.DISTANCIA);
		assertThat(arq.criticoPp()).isEqualTo(5);
		assertThat(arq.vel()).isZero();
		assertThat(TipoInimigo.GOBLIN.criar(0, 1).alcance()).isEqualTo(Alcance.CORPO_A_CORPO_FRENTE);
	}
}
