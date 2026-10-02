package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.ItemBonusGerador;
import com.example.loginbase.jogo.pedra.GeradorPedras;
import com.example.loginbase.jogo.recurso.Recurso;

class GeradorRecompensasTest {

	private final GeradorRecompensas gerador = new GeradorRecompensas(new ItemBonusGerador(), new GeradorPedras());

	@Test
	void ouroN6EntreMinEMax() {
		for (long s = 0; s < 500; s++) {
			assertThat(gerador.gerar(6, s).ouro()).isBetween(240, 360);
		}
	}

	@Test
	void recursosSeisSorteiosDe60() {
		for (long s = 0; s < 200; s++) {
			var r = gerador.gerar(6, s);
			assertThat(r.recursos().values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(360);
			assertThat(r.recursos().values()).allMatch(v -> v % 60 == 0);
		}
	}

	@Test
	void acoSoComN6OuMais() {
		boolean acoN6 = false;
		for (long s = 0; s < 300; s++) {
			assertThat(gerador.gerar(5, s).recursos()).doesNotContainKey(Recurso.ACO);
			acoN6 |= gerador.gerar(6, s).recursos().containsKey(Recurso.ACO);
		}
		assertThat(acoN6).isTrue();
	}

	@Test
	void chanceDeItemComTeto() {
		assertThat(taxaItem(6)).isBetween(0.55, 0.65);
		assertThat(taxaItem(10)).isBetween(0.75, 0.85);
		assertThat(taxaItem(1)).isBetween(0.06, 0.14);
	}

	private double taxaItem(int n) {
		int total = 4000;
		int com = 0;
		for (long s = 0; s < total; s++) {
			if (gerador.gerar(n, s * 7919).item() != null) {
				com++;
			}
		}
		return (double) com / total;
	}

	@Test
	void itemTemNivelECategoriaValida() {
		for (long s = 0; s < 300; s++) {
			var item = gerador.gerar(8, s).item();
			if (item != null) {
				assertThat(item.nivel()).isEqualTo(8);
				assertThat(item.bonus()).hasSize(item.qualidade().getQtdBonus());
				assertThat(item.subtipo().getCategoria().name()).isIn("ARMA", "ARMADURA", "JOIA");
				assertThat(item.atributoEscolhido() != null).isEqualTo(item.subtipo().name().equals("ANEL"));
			}
		}
	}

	@Test
	void pedrasTresSorteiosEmN6() {
		int max = 0;
		for (long s = 0; s < 500; s++) {
			int n = gerador.gerar(6, s).pedras().size();
			assertThat(n).isLessThanOrEqualTo(3);
			max = Math.max(max, n);
		}
		assertThat(max).isEqualTo(3);
	}

	@Test
	void xpIgualAoNivel() {
		assertThat(gerador.gerar(4, 1L).xpPorGuerreiro()).isEqualTo(4);
	}

	@Test
	void mesmaSementeMesmasRecompensas() {
		assertThat(gerador.gerar(9, 12345L)).isEqualTo(gerador.gerar(9, 12345L));
	}
}
