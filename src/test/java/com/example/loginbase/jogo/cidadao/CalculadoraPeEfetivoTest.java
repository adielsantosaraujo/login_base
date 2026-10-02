package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.BonusEquipamentoService;

class CalculadoraPeEfetivoTest {

	private final BonusEquipamentoService bonus = mock(BonusEquipamentoService.class);
	private final CalculadoraPeEfetivo calc = new CalculadoraPeEfetivo(bonus);

	private Cidadao cidadao() {
		return new Cidadao(1L, 1L, "A", Sexo.M, 480);
	}

	@Test
	void construtorComInt15() {
		Cidadao c = cidadao();
		c.setInteligencia(15);
		assertThat(calc.calcular(c, Profissao.CONSTRUTOR, 5)).isEqualTo(8);
	}

	@Test
	void somaBonusDeVariasCaracteristicas() {
		Cidadao c = cidadao();
		c.setVit(9);
		c.setVel(10);
		c.setCar(4);
		// Caçador: VIT 1 + VEL 2 + CAR 0
		assertThat(calc.calcular(c, Profissao.CACADOR, 2)).isEqualTo(5);
	}

	@Test
	void semCaracteristicaRetornaPeBase() {
		assertThat(calc.calcular(cidadao(), Profissao.GUERREIRO, 0)).isZero();
		assertThat(calc.calcular(cidadao(), Profissao.COMERCIANTE, 3)).isEqualTo(3);
	}

	@Test
	void ignoraCaracteristicaNaoLigada() {
		Cidadao c = cidadao();
		c.setCar(20);
		assertThat(calc.calcular(c, Profissao.MINEIRO, 1)).isEqualTo(1);
	}

	@Test
	void todasProfissoesTemCaracteristicas() {
		assertThat(Profissao.values()).hasSize(12);
		for (Profissao p : Profissao.values()) {
			assertThat(p.getCaracteristicas()).isNotEmpty();
		}
	}

	@Test
	void somaBonusDeFerramentaEDeItens() {
		Cidadao c = cidadao();
		c.setId(9L);
		when(bonus.bonusFerramenta(9L, Profissao.AGRICULTOR)).thenReturn(3);
		when(bonus.bonusProfItens(9L, Profissao.AGRICULTOR)).thenReturn(2);
		c.setInteligencia(10);
		assertThat(calc.calcular(c, Profissao.AGRICULTOR, 2)).isEqualTo(2 + 2 + 3 + 2);
	}

	@Test
	void cidadaoTransienteNaoConsultaItens() {
		assertThat(calc.calcular(cidadao(), Profissao.COMERCIANTE, 3)).isEqualTo(3);
		assertThat(calc.peItens(cidadao(), Profissao.COMERCIANTE)).isZero();
	}

}
