package com.example.loginbase.jogo.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.servico.GeradorLadrilhoService.LadrilhoGerado;
import com.example.loginbase.jogo.servico.GeradorLadrilhoService.PercentualTerreno;

class GeradorLadrilhoServiceTest {

	private final GeradorLadrilhoService gerador = new GeradorLadrilhoService();

	private static List<PercentualTerreno> perc(TipoTerreno a, int pa, TipoTerreno b, int pb, TipoTerreno c, int pc) {
		return List.of(new PercentualTerreno(a, pa), new PercentualTerreno(b, pb), new PercentualTerreno(c, pc));
	}

	private static final List<PercentualTerreno> PADRAO = perc(TipoTerreno.FLORESTA, 40, TipoTerreno.PLANTACOES, 35,
			TipoTerreno.BARREIRO, 25);

	private static void verificaQuantidades(List<LadrilhoGerado> grade, List<PercentualTerreno> esperado) {
		assertThat(grade).hasSize(100);
		Map<TipoTerreno, Integer> contagem = new EnumMap<>(TipoTerreno.class);
		grade.forEach(l -> contagem.merge(l.terreno(), 1, Integer::sum));
		for (PercentualTerreno p : esperado) {
			assertThat(contagem.get(p.terreno())).isEqualTo(p.percentual());
		}
	}

	@Test
	void quantidadesPorTerrenoIguaisAosPercentuais() {
		List<List<PercentualTerreno>> conjuntos = List.of(PADRAO,
				perc(TipoTerreno.ROCHA, 60, TipoTerreno.FERRO, 30, TipoTerreno.CARVAO, 10),
				perc(TipoTerreno.SALINAS, 20, TipoTerreno.ENXOFRE, 20, TipoTerreno.ROCHA, 60),
				perc(TipoTerreno.MILITAR, 50, TipoTerreno.INDUSTRIA, 30, TipoTerreno.COMERCIO, 20));
		for (List<PercentualTerreno> c : conjuntos) {
			verificaQuantidades(gerador.gerar(42L, 3, c), c);
		}
	}

	@Test
	void ordemDeVarredura() {
		List<LadrilhoGerado> grade = gerador.gerar(1L, 1, PADRAO);
		for (int i = 0; i < 100; i++) {
			assertThat(grade.get(i).x()).isEqualTo(i % 10);
			assertThat(grade.get(i).y()).isEqualTo(i / 10);
		}
	}

	@Test
	void deterministicoESensivelASementeEIndice() {
		assertThat(gerador.gerar(7L, 5, PADRAO)).isEqualTo(gerador.gerar(7L, 5, PADRAO));
		assertThat(gerador.gerar(7L, 5, PADRAO)).isNotEqualTo(gerador.gerar(8L, 5, PADRAO));
		assertThat(gerador.gerar(7L, 5, PADRAO)).isNotEqualTo(gerador.gerar(7L, 6, PADRAO));
	}

	@Test
	void bonusBaseEntre0E100ComExtremosAparecendo() {
		boolean viu0 = false;
		boolean viu100 = false;
		for (long s = 0; s < 200; s++) {
			for (LadrilhoGerado l : gerador.gerar(s, 1 + (int) (s % 16), PADRAO)) {
				assertThat(l.bonusBase()).isBetween(0, 100);
				viu0 |= l.bonusBase() == 0;
				viu100 |= l.bonusBase() == 100;
			}
		}
		assertThat(viu0).isTrue();
		assertThat(viu100).isTrue();
	}

	@Test
	void bonusAdjacenteBateComRecalculoERespeitaLimites() {
		for (long s = 0; s < 50; s++) {
			List<LadrilhoGerado> grade = gerador.gerar(s, 1 + (int) (s % 16), PADRAO);
			TipoTerreno[][] t = new TipoTerreno[10][10];
			grade.forEach(l -> t[l.y()][l.x()] = l.terreno());
			for (LadrilhoGerado l : grade) {
				int iguais = 0;
				int[][] ds = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
				for (int[] d : ds) {
					int nx = l.x() + d[0];
					int ny = l.y() + d[1];
					if (nx >= 0 && nx < 10 && ny >= 0 && ny < 10 && t[ny][nx] == l.terreno()) {
						iguais++;
					}
				}
				assertThat(l.bonusAdjacente()).isEqualTo(25 * iguais);
				assertThat(l.bonusAdjacente() % 25).isZero();
				assertThat(l.bonusAdjacente()).isBetween(0, 100);
				assertThat(l.bonusTotal()).isEqualTo(l.bonusBase() + l.bonusAdjacente()).isBetween(0, 200);
				boolean canto = (l.x() == 0 || l.x() == 9) && (l.y() == 0 || l.y() == 9);
				boolean borda = l.x() == 0 || l.x() == 9 || l.y() == 0 || l.y() == 9;
				if (canto) {
					assertThat(l.bonusAdjacente()).isLessThanOrEqualTo(50);
				} else if (borda) {
					assertThat(l.bonusAdjacente()).isLessThanOrEqualTo(75);
				}
			}
		}
	}

	@Test
	void entradasInvalidasLancamExcecao() {
		assertThatThrownBy(() -> gerador.gerar(1L, 0, PADRAO)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerar(1L, 17, PADRAO)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerar(1L, 1, List.of(new PercentualTerreno(TipoTerreno.FLORESTA, 50),
				new PercentualTerreno(TipoTerreno.BARREIRO, 50)))).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerar(1L, 1,
				perc(TipoTerreno.FLORESTA, 40, TipoTerreno.FLORESTA, 35, TipoTerreno.BARREIRO, 25)))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerar(1L, 1,
				perc(TipoTerreno.FLORESTA, 40, TipoTerreno.PLANTACOES, 35, TipoTerreno.BARREIRO, 30)))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerar(1L, 1, null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void listaRetornadaImutavel() {
		List<LadrilhoGerado> grade = gerador.gerar(1L, 1, PADRAO);
		assertThatThrownBy(() -> grade.add(grade.get(0))).isInstanceOf(UnsupportedOperationException.class);
	}
}
