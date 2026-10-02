package com.example.loginbase.jogo.item.catalogo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.recurso.Recurso;

class JoiaCatalogoTest {

	@Test
	void receitasBaseEOficina() {
		assertThat(JoiaCatalogo.COLAR.receitaBase()).containsEntry(Recurso.FERRO, 1).containsEntry(Recurso.OURO, 20);
		assertThat(JoiaCatalogo.ANEL.receitaBase()).containsEntry(Recurso.FERRO, 1).containsEntry(Recurso.OURO, 15);
		assertThat(JoiaCatalogo.COLAR.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
		assertThat(JoiaCatalogo.ANEL.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
	}

	@Test
	void vidaColar() {
		assertThat(JoiaCatalogo.vidaColar(1)).isEqualTo(5);
		assertThat(JoiaCatalogo.vidaColar(10)).isEqualTo(50);
	}

	@Test
	void bonusAnelPorFaixa() {
		int[] esperado = {1, 1, 1, 2, 2, 2, 3, 3, 3, 4};
		for (int l = 1; l <= 10; l++) {
			assertThat(JoiaCatalogo.bonusAnel(l)).as("nivel %d", l).isEqualTo(esperado[l - 1]);
		}
	}

	@Test
	void caracteristicasEExigeAtributo() {
		for (Caracteristica c : Caracteristica.values()) {
			assertThat(JoiaCatalogo.caracteristicaPermitida(c)).isTrue();
		}
		assertThat(JoiaCatalogo.caracteristicaPermitida(null)).isFalse();
		assertThat(JoiaCatalogo.ANEL.exigeAtributo()).isTrue();
		assertThat(JoiaCatalogo.COLAR.exigeAtributo()).isFalse();
	}

	@Test
	void buscaPorSubtipo() {
		assertThat(JoiaCatalogo.de(ItemSubtipo.COLAR)).contains(JoiaCatalogo.COLAR);
		assertThat(JoiaCatalogo.de(ItemSubtipo.ANEL)).contains(JoiaCatalogo.ANEL);
		assertThat(JoiaCatalogo.de(ItemSubtipo.ESPADA)).isEqualTo(Optional.empty());
	}

	@Test
	void receitaPorNivelUsaRegrasDeFabricacao() {
		assertThat(JoiaCatalogo.COLAR.receita(1)).containsEntry(Recurso.OURO, 20);
	}
}
