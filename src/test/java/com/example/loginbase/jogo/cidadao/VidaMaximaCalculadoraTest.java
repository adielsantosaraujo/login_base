package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.BonusEquipamentoService;

class VidaMaximaCalculadoraTest {

	private final BonusEquipamentoService bonus = mock(BonusEquipamentoService.class);
	private final CidadaoProfissaoRepository repo = mock(CidadaoProfissaoRepository.class);
	private final CalculadoraPeEfetivo pe = new CalculadoraPeEfetivo(bonus);
	private final VidaMaximaCalculadora calc = new VidaMaximaCalculadora(pe, bonus, repo);

	private Cidadao cidadao(int vit, int forca, int vel) {
		Cidadao c = new Cidadao(1L, 1L, "A", Sexo.M, 480);
		c.setId(7L);
		c.setVit(vit);
		c.setForca(forca);
		c.setVel(vel);
		return c;
	}

	@Test
	void formulaDaDocumentacao() {
		// 30 + 5x10 + 3x11 = 113
		Cidadao c = cidadao(10, 5, 0);
		when(repo.findByCidadaoIdAndProfissao(7L, Profissao.GUERREIRO))
				.thenReturn(Optional.of(new CidadaoProfissao(7L, Profissao.GUERREIRO, 8)));
		// G = 8 + floor(5/5) + floor(10/5) + 0 = 11
		assertThat(calc.calcular(c)).isEqualTo(30 + 50 + 33);
	}

	@Test
	void colarSomaVidaEIntrinsecos() {
		Cidadao c = cidadao(0, 0, 0);
		when(bonus.vidaItens(7L)).thenReturn(27);
		assertThat(calc.calcular(c)).isEqualTo(57);
	}

	@Test
	void usaCaracteristicaTotal() {
		Cidadao c = cidadao(0, 0, 0);
		when(bonus.bonusCaracteristica(eq(7L), eq(Caracteristica.VIT))).thenReturn(5);
		// VIT 5 -> 25; G = floor(5/5) = 1 -> 3
		assertThat(calc.calcular(c)).isEqualTo(30 + 25 + 3);
	}

	@Test
	void transienteSemItens() {
		Cidadao c = new Cidadao(1L, 1L, "A", Sexo.M, 480);
		assertThat(calc.calcular(c)).isEqualTo(30);
	}
}
