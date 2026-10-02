package com.example.loginbase.jogo.item;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.construcao.NivelConstrucao;

/** Gerador puro e determinístico (por semente) de qualidade e bônus intrínsecos de itens. */
@Component
public class ItemBonusGerador {

	/** Sorteia a qualidade pela margem de PE. Fora do N3, a chance de DIVINA soma-se à de EXCELENTE. */
	public Qualidade gerarQualidade(int peEfetivo, int nivel, NivelConstrucao nivelOficina, long semente) {
		int m = peEfetivo - RegrasFabricacao.peMinimo(nivel);
		int simples;
		int boa;
		int excelente;
		if (m < 5) {
			simples = 70;
			boa = 25;
			excelente = 5;
		} else if (m < 10) {
			simples = 55;
			boa = 33;
			excelente = 11;
		} else if (m < 15) {
			simples = 40;
			boa = 38;
			excelente = 19;
		} else {
			simples = 30;
			boa = 40;
			excelente = 25;
		}
		int divina = 100 - simples - boa - excelente;
		if (nivelOficina != NivelConstrucao.N3) {
			excelente += divina;
			divina = 0;
		}
		int r = new Random(semente).nextInt(100);
		if (r < simples) {
			return Qualidade.SIMPLES;
		}
		if (r < simples + boa) {
			return Qualidade.BOA;
		}
		if (r < simples + boa + excelente) {
			return Qualidade.EXCELENTE;
		}
		return Qualidade.DIVINA;
	}

	/** Sorteia qualidade.qtdBonus códigos distintos permitidos da categoria, com magnitude fixa da faixa do nível. */
	public List<BonusItem> gerarBonusIntrinsecos(Qualidade qualidade, ItemCategoria categoria, int nivel,
			long semente) {
		List<CodigoBonus> pool = new ArrayList<>(permitidos(categoria));
		Random rnd = new Random(semente);
		FaixaBonus faixa = RegrasFabricacao.faixa(nivel);
		List<BonusItem> r = new ArrayList<>();
		for (int i = 0; i < qualidade.getQtdBonus() && !pool.isEmpty(); i++) {
			CodigoBonus c = pool.remove(rnd.nextInt(pool.size()));
			r.add(new BonusItem(c, magnitude(c, faixa)));
		}
		return r;
	}

	/** Mantém os tipos e recalcula os valores pela faixa do novo nível (aprimoramento). */
	public List<BonusItem> recalcularMagnitudes(List<BonusItem> bonus, int novoNivel) {
		FaixaBonus faixa = RegrasFabricacao.faixa(novoNivel);
		return bonus.stream().map(b -> new BonusItem(b.codigo(), magnitude(b.codigo(), faixa))).toList();
	}

	public int magnitude(CodigoBonus codigo, FaixaBonus faixa) {
		int i = faixa.ordinal();
		return switch (codigo) {
			case VIT, FOR, VEL, INT, CAR, INI, PROF -> new int[] { 1, 2, 3 }[i];
			case ATK, DEF -> new int[] { 3, 5, 8 }[i];
			case VIDA -> new int[] { 8, 15, 25 }[i];
			case CRIT -> new int[] { 2, 3, 5 }[i];
			case PROD -> new int[] { 3, 5, 8 }[i];
		};
	}

	public Set<CodigoBonus> permitidos(ItemCategoria categoria) {
		return switch (categoria) {
			case ARMA -> EnumSet.of(CodigoBonus.FOR, CodigoBonus.VEL, CodigoBonus.INT, CodigoBonus.ATK,
					CodigoBonus.CRIT, CodigoBonus.INI);
			case ARMADURA -> EnumSet.of(CodigoBonus.VIT, CodigoBonus.DEF, CodigoBonus.VIDA, CodigoBonus.VEL);
			case JOIA -> EnumSet.of(CodigoBonus.VIT, CodigoBonus.FOR, CodigoBonus.VEL, CodigoBonus.INT,
					CodigoBonus.CAR, CodigoBonus.VIDA, CodigoBonus.CRIT, CodigoBonus.PROD);
			case FERRAMENTA -> EnumSet.of(CodigoBonus.PROF, CodigoBonus.PROD, CodigoBonus.INT, CodigoBonus.FOR,
					CodigoBonus.VEL);
		};
	}
}
