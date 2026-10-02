package com.example.loginbase.jogo.item.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.recurso.Recurso;

class ArmaduraCatalogoTest {

	@Test
	void receitaPeitoralConverteFerroEmAcoDoNivel6() {
		var p = ArmaduraCatalogo.PEITORAL;
		assertThat(p.receita(1)).isEqualTo(Map.of(Recurso.FERRO, 5));
		assertThat(p.receita(5)).isEqualTo(Map.of(Recurso.FERRO, 25));
		assertThat(p.receita(6)).isEqualTo(Map.of(Recurso.ACO, 30));
		assertThat(p.receita(10)).isEqualTo(Map.of(Recurso.ACO, 50));
	}

	@Test
	void receitaLuvasECalcas() {
		assertThat(ArmaduraCatalogo.LUVAS.receita(5)).isEqualTo(Map.of(Recurso.COURO_CURTIDO, 10));
		assertThat(ArmaduraCatalogo.LUVAS.receita(6)).isEqualTo(Map.of(Recurso.COURO_CURTIDO, 12));
		assertThat(ArmaduraCatalogo.CALCAS.receita(2))
				.isEqualTo(Map.of(Recurso.COURO_CURTIDO, 6, Recurso.TECIDO, 2));
	}

	@Test
	void defesaBaseEOficinas() {
		assertThat(ArmaduraCatalogo.values()).extracting(ArmaduraCatalogo::defesaBase)
				.containsExactly(8, 4, 3, 2, 5, 2);
		assertThat(ArmaduraCatalogo.PEITORAL.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
		assertThat(ArmaduraCatalogo.OMBREIRAS.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
		assertThat(ArmaduraCatalogo.SAPATO.oficina()).isEqualTo(TipoConstrucao.ALFAIATARIA);
	}

	@Test
	void defesaPorNivelESomaDoConjunto() {
		assertThat(ArmaduraCatalogo.PEITORAL.defesa(5)).isCloseTo(14.4, within(1e-9));
		double total = 0;
		for (ArmaduraCatalogo a : ArmaduraCatalogo.values()) {
			total += a.defesa(10);
		}
		assertThat(total).isCloseTo(67.2, within(1e-9));
	}

	@Test
	void iniciativaExtraSoNoSapato() {
		for (ArmaduraCatalogo a : ArmaduraCatalogo.values()) {
			assertThat(a.iniciativaExtra()).isEqualTo(a == ArmaduraCatalogo.SAPATO ? 1 : 0);
		}
	}

	@Test
	void pfENivelMaximoDelegamParaRegras() {
		assertThat(ArmaduraCatalogo.PEITORAL.pf(1)).isEqualTo(2);
		assertThat(ArmaduraCatalogo.PEITORAL.pf(10)).isEqualTo(11);
		assertThat(ArmaduraCatalogo.PEITORAL.nivelMaximo(NivelConstrucao.N1)).isEqualTo(3);
		assertThat(ArmaduraCatalogo.PEITORAL.nivelMaximo(NivelConstrucao.N2)).isEqualTo(6);
		assertThat(ArmaduraCatalogo.PEITORAL.nivelMaximo(NivelConstrucao.N3)).isEqualTo(10);
	}

	@Test
	void slotESubtipoCorrespondem() {
		for (ArmaduraCatalogo a : ArmaduraCatalogo.values()) {
			assertThat(a.slot().name()).isEqualTo(a.subtipo().name());
			assertThat(ArmaduraCatalogo.de(a.subtipo())).contains(a);
		}
		assertThat(ArmaduraCatalogo.CALCAS.slot()).isEqualTo(SlotEquipamento.CALCAS);
		assertThat(ArmaduraCatalogo.de(ItemSubtipo.ESPADA)).isEmpty();
	}
}
