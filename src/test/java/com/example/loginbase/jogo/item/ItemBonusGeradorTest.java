package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.construcao.NivelConstrucao;

class ItemBonusGeradorTest {

	private static final int N = 20000;
	private final ItemBonusGerador gerador = new ItemBonusGerador();

	private Map<Qualidade, Double> distribuicao(int pe, int nivel, NivelConstrucao oficina) {
		Map<Qualidade, Double> d = new EnumMap<>(Qualidade.class);
		for (Qualidade q : Qualidade.values()) {
			d.put(q, 0.0);
		}
		for (long s = 0; s < N; s++) {
			d.merge(gerador.gerarQualidade(pe, nivel, oficina, s * 7919 + 13), 1.0, Double::sum);
		}
		d.replaceAll((q, v) -> v / N);
		return d;
	}

	private void confere(Map<Qualidade, Double> d, double s, double b, double e, double dv) {
		assertThat(d.get(Qualidade.SIMPLES)).isBetween(s - 0.05, s + 0.05);
		assertThat(d.get(Qualidade.BOA)).isBetween(b - 0.05, b + 0.05);
		assertThat(d.get(Qualidade.EXCELENTE)).isBetween(e - 0.05, e + 0.05);
		assertThat(d.get(Qualidade.DIVINA)).isBetween(dv - 0.05, dv + 0.05);
	}

	@Test
	void qualidadePorMargem() {
		// nivel 5: peMinimo 8
		confere(distribuicao(10, 5, NivelConstrucao.N3), .70, .25, .05, 0);
		confere(distribuicao(14, 5, NivelConstrucao.N3), .55, .33, .11, .01);
		confere(distribuicao(19, 5, NivelConstrucao.N3), .40, .38, .19, .03);
		confere(distribuicao(30, 5, NivelConstrucao.N3), .30, .40, .25, .05);
	}

	@Test
	void divinaSoN3() {
		Map<Qualidade, Double> n1 = distribuicao(30, 5, NivelConstrucao.N1);
		assertThat(n1.get(Qualidade.DIVINA)).isZero();
		assertThat(n1.get(Qualidade.EXCELENTE)).isBetween(.25, .35);
		assertThat(distribuicao(30, 5, NivelConstrucao.N2).get(Qualidade.DIVINA)).isZero();
		assertThat(distribuicao(30, 5, NivelConstrucao.N3).get(Qualidade.DIVINA)).isGreaterThan(0.0);
	}

	@Test
	void bonusDistintosEPermitidos() {
		for (ItemCategoria c : ItemCategoria.values()) {
			for (long s = 0; s < 1000; s++) {
				List<BonusItem> b = gerador.gerarBonusIntrinsecos(Qualidade.DIVINA, c, 6, s);
				assertThat(b).hasSize(3);
				Set<CodigoBonus> cods = b.stream().map(BonusItem::codigo).collect(Collectors.toSet());
				assertThat(cods).hasSize(3).isSubsetOf(gerador.permitidos(c));
			}
		}
		assertThat(gerador.gerarBonusIntrinsecos(Qualidade.SIMPLES, ItemCategoria.ARMA, 1, 1)).isEmpty();
		assertThat(gerador.gerarBonusIntrinsecos(Qualidade.BOA, ItemCategoria.ARMA, 1, 1)).hasSize(1);
		assertThat(gerador.gerarBonusIntrinsecos(Qualidade.EXCELENTE, ItemCategoria.JOIA, 1, 1)).hasSize(2);
	}

	@Test
	void distribuicaoUniformeDosCodigos() {
		Map<CodigoBonus, Integer> cont = new EnumMap<>(CodigoBonus.class);
		for (long s = 0; s < N; s++) {
			gerador.gerarBonusIntrinsecos(Qualidade.BOA, ItemCategoria.ARMADURA, 1, s * 31 + 5)
					.forEach(b -> cont.merge(b.codigo(), 1, Integer::sum));
		}
		assertThat(cont.keySet()).containsExactlyInAnyOrderElementsOf(gerador.permitidos(ItemCategoria.ARMADURA));
		cont.values().forEach(v -> assertThat(v / (double) N).isBetween(.20, .30));
	}

	@Test
	void reproducivelComSemente() {
		assertThat(gerador.gerarBonusIntrinsecos(Qualidade.DIVINA, ItemCategoria.JOIA, 9, 42L))
				.isEqualTo(gerador.gerarBonusIntrinsecos(Qualidade.DIVINA, ItemCategoria.JOIA, 9, 42L));
		assertThat(gerador.gerarQualidade(20, 5, NivelConstrucao.N3, 99L))
				.isEqualTo(gerador.gerarQualidade(20, 5, NivelConstrucao.N3, 99L));
	}

	@Test
	void magnitudesPorFaixa() {
		assertThat(gerador.magnitude(CodigoBonus.FOR, FaixaBonus.BAIXA)).isEqualTo(1);
		assertThat(gerador.magnitude(CodigoBonus.ATK, FaixaBonus.MEDIA)).isEqualTo(5);
		assertThat(gerador.magnitude(CodigoBonus.VIDA, FaixaBonus.ALTA)).isEqualTo(25);
		assertThat(gerador.magnitude(CodigoBonus.CRIT, FaixaBonus.ALTA)).isEqualTo(5);
		assertThat(gerador.magnitude(CodigoBonus.PROD, FaixaBonus.BAIXA)).isEqualTo(3);
		assertThat(gerador.magnitude(CodigoBonus.INI, FaixaBonus.MEDIA)).isEqualTo(2);
		assertThat(gerador.magnitude(CodigoBonus.PROF, FaixaBonus.ALTA)).isEqualTo(3);
		assertThat(gerador.magnitude(CodigoBonus.DEF, FaixaBonus.ALTA)).isEqualTo(8);
		List<BonusItem> b = gerador.gerarBonusIntrinsecos(Qualidade.EXCELENTE, ItemCategoria.ARMA, 3, 7L);
		b.forEach(x -> assertThat(x.valor()).isEqualTo(gerador.magnitude(x.codigo(), FaixaBonus.BAIXA)));
	}

	@Test
	void recalcularMagnitudesMantemTipos() {
		List<BonusItem> orig = List.of(new BonusItem(CodigoBonus.ATK, 3), new BonusItem(CodigoBonus.VIDA, 8));
		List<BonusItem> novo = gerador.recalcularMagnitudes(orig, 8);
		assertThat(novo).containsExactly(new BonusItem(CodigoBonus.ATK, 8), new BonusItem(CodigoBonus.VIDA, 25));
		assertThat(gerador.recalcularMagnitudes(novo, 5))
				.containsExactly(new BonusItem(CodigoBonus.ATK, 5), new BonusItem(CodigoBonus.VIDA, 15));
	}
}
