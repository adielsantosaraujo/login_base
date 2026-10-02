package com.example.loginbase.jogo.item.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.recurso.Recurso;

class ArmaCatalogoTest {

	@Test
	void receitaMultiplicaBasePorNivel() {
		assertThat(ArmaCatalogo.ESPADA.receita(5)).isEqualTo(Map.of(Recurso.FERRO, 15, Recurso.TABUA, 5));
	}

	@Test
	void receitaTrocaFerroPorAcoEmL6() {
		assertThat(ArmaCatalogo.ESPADA.receita(6)).isEqualTo(Map.of(Recurso.ACO, 18, Recurso.TABUA, 6));
		assertThat(ArmaCatalogo.BESTA.receita(10))
				.isEqualTo(Map.of(Recurso.ACO, 20, Recurso.TABUA, 30, Recurso.TECIDO, 10));
	}

	@Test
	void arcoNaoUsaFerro() {
		assertThat(ArmaCatalogo.ARCO.receita(10)).isEqualTo(Map.of(Recurso.TABUA, 30, Recurso.TECIDO, 10));
	}

	@Test
	void tabela78() {
		assertThat(ArmaCatalogo.ESPADA.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
		assertThat(ArmaCatalogo.ESPADA.ataqueBase()).isEqualTo(10);
		assertThat(ArmaCatalogo.ESPADA.atributosChave()).containsExactly(Caracteristica.FOR);
		assertThat(ArmaCatalogo.ESPADA.modificadorIniciativa()).isZero();
		assertThat(ArmaCatalogo.ESPADA.alcance()).isEqualTo(Alcance.CORPO_A_CORPO_FRENTE);
		assertThat(ArmaCatalogo.ESPADA.especial()).contains("+2 Defesa ao portador");

		assertThat(ArmaCatalogo.LANCA.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
		assertThat(ArmaCatalogo.LANCA.ataqueBase()).isEqualTo(9);
		assertThat(ArmaCatalogo.LANCA.atributosChave()).containsExactly(Caracteristica.FOR, Caracteristica.VEL);
		assertThat(ArmaCatalogo.LANCA.modificadorIniciativa()).isEqualTo(1);
		assertThat(ArmaCatalogo.LANCA.alcance()).isEqualTo(Alcance.CORPO_A_CORPO_FLEX);
		assertThat(ArmaCatalogo.LANCA.especial()).isEmpty();

		assertThat(ArmaCatalogo.ARCO.oficina()).isEqualTo(TipoConstrucao.CARPINTARIA);
		assertThat(ArmaCatalogo.ARCO.ataqueBase()).isEqualTo(8);
		assertThat(ArmaCatalogo.ARCO.atributosChave()).containsExactly(Caracteristica.VEL);
		assertThat(ArmaCatalogo.ARCO.modificadorIniciativa()).isEqualTo(2);
		assertThat(ArmaCatalogo.ARCO.alcance()).isEqualTo(Alcance.DISTANCIA);

		assertThat(ArmaCatalogo.BESTA.oficina()).isEqualTo(TipoConstrucao.CARPINTARIA);
		assertThat(ArmaCatalogo.BESTA.ataqueBase()).isEqualTo(13);
		assertThat(ArmaCatalogo.BESTA.atributosChave()).containsExactly(Caracteristica.INT);
		assertThat(ArmaCatalogo.BESTA.modificadorIniciativa()).isEqualTo(-4);
		assertThat(ArmaCatalogo.BESTA.especial()).contains("Ignora 25% da defesa do alvo");
		assertThat(ArmaCatalogo.BESTA.receitaBase())
				.isEqualTo(Map.of(Recurso.FERRO, 2, Recurso.TABUA, 3, Recurso.TECIDO, 1));
	}

	@Test
	void ataqueAplicaMultiplicadorDeNivel() {
		assertThat(ArmaCatalogo.ESPADA.ataque(1)).isCloseTo(10.0, within(1e-9));
		assertThat(ArmaCatalogo.ESPADA.ataque(6)).isCloseTo(20.0, within(1e-9));
		assertThat(ArmaCatalogo.BESTA.ataque(10)).isCloseTo(13 * 2.8, within(1e-9));
	}

	@Test
	void buscaPorSubtipo() {
		assertThat(ArmaCatalogo.de(ItemSubtipo.LANCA)).contains(ArmaCatalogo.LANCA);
		assertThat(ArmaCatalogo.de(ItemSubtipo.MARTELO)).isEmpty();
	}
}
