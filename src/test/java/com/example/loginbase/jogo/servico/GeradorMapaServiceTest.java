package com.example.loginbase.jogo.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.servico.GeradorMapaService.BonusGerado;
import com.example.loginbase.jogo.servico.GeradorMapaService.RegiaoGerada;

class GeradorMapaServiceTest {

	private static final int SEMENTES = 1000;
	private final GeradorMapaService service = new GeradorMapaService();

	private static Map<TipoRegiao, Integer> contar(List<RegiaoGerada> mapa) {
		Map<TipoRegiao, Integer> c = new EnumMap<>(TipoRegiao.class);
		mapa.forEach(r -> c.merge(r.tipo(), 1, Integer::sum));
		return c;
	}

	@Test
	void quantidadesPorTipoRespeitamRegrasR8aR11() {
		for (long s = 0; s < SEMENTES; s++) {
			List<RegiaoGerada> mapa = service.gerar(s);
			Map<TipoRegiao, Integer> c = contar(mapa);
			assertEquals(16, mapa.size(), "R9 semente " + s);
			assertEquals(TipoRegiao.atuais().size(), c.size(), "semente " + s);
			for (TipoRegiao t : TipoRegiao.atuais()) {
				int q = c.get(t);
				assertTrue(q >= 2 && q <= 4, "R8 semente " + s + " " + t + "=" + q);
			}
			assertEquals(16, c.values().stream().mapToInt(Integer::intValue).sum(), "R9 semente " + s);
			assertTrue(c.values().stream().filter(q -> q == 4).count() <= 2, "R10 semente " + s);
			for (int i = 0; i < 16; i++) {
				assertEquals(i + 1, mapa.get(i).indice(), "R11 semente " + s);
			}
		}
	}

	@Test
	void bonusDistintosDoTipoEFaixasPorPosicao() {
		int[][] faixas = { { 35, 50 }, { 16, 34 }, { 5, 15 } };
		Set<Integer> vistos = new HashSet<>();
		for (long s = 0; s < SEMENTES; s++) {
			for (RegiaoGerada r : service.gerar(s)) {
				assertEquals(3, r.bonus().size(), "R13 semente " + s);
				Set<BonusRegiao> distintos = new HashSet<>();
				for (int p = 0; p < 3; p++) {
					BonusGerado b = r.bonus().get(p);
					assertEquals(p + 1, b.posicao(), "ordem por posição");
					assertTrue(r.tipo().bonus().contains(b.bonus()), "R13 bônus do tipo");
					distintos.add(b.bonus());
					assertTrue(b.valor() >= faixas[p][0] && b.valor() <= faixas[p][1],
							"R14/R15 semente " + s + " pos " + (p + 1) + " valor " + b.valor());
					vistos.add(b.valor());
				}
				assertEquals(3, distintos.size(), "R13 distintos");
			}
		}
		for (int limite : new int[] { 35, 50, 16, 34, 5, 15 }) {
			assertTrue(vistos.contains(limite), "limite alcançável " + limite);
		}
	}

	@Test
	void todoBonusApareceEmTodasAsPosicoes() {
		Map<TipoRegiao, Map<BonusRegiao, Set<Integer>>> vistos = new EnumMap<>(TipoRegiao.class);
		for (long s = 0; s < SEMENTES; s++) {
			for (RegiaoGerada r : service.gerar(s)) {
				Map<BonusRegiao, Set<Integer>> porBonus = vistos.computeIfAbsent(r.tipo(), t -> new EnumMap<>(BonusRegiao.class));
				for (BonusGerado b : r.bonus()) {
					porBonus.computeIfAbsent(b.bonus(), x -> new HashSet<>()).add(b.posicao());
				}
			}
		}
		for (TipoRegiao t : TipoRegiao.atuais()) {
			for (BonusRegiao b : t.bonus()) {
				assertEquals(Set.of(1, 2, 3), vistos.get(t).get(b), "R16 " + t + " " + b);
			}
		}
	}

	@Test
	void ambasAsFamiliasDeDistribuicaoAparecem() {
		Set<List<Integer>> familias = new HashSet<>();
		for (long s = 0; s < SEMENTES; s++) {
			List<Integer> q = new ArrayList<>(contar(service.gerar(s)).values());
			q.sort(java.util.Comparator.reverseOrder());
			familias.add(q);
		}
		assertTrue(familias.contains(List.of(4, 4, 3, 3, 2)), familias.toString());
		assertTrue(familias.contains(List.of(4, 3, 3, 3, 3)), familias.toString());
	}

	@Test
	void mesmaSementeGeraMapaIdentico() {
		assertEquals(service.gerar(42), service.gerar(42));
		boolean algumaDiferente = false;
		for (long s = 0; s < 20; s++) {
			algumaDiferente |= !service.gerar(s).equals(service.gerar(s + 1));
		}
		assertTrue(algumaDiferente);
	}

	@Test
	void sempreExisteSelecaoValida() {
		for (long s = 0; s < SEMENTES; s++) {
			List<RegiaoGerada> mapa = service.gerar(s);
			boolean achou = false;
			for (int a = 1; a <= 16 && !achou; a++) {
				for (int b = a + 1; b <= 16 && !achou; b++) {
					for (int c = b + 1; c <= 16 && !achou; c++) {
						boolean urbana = mapa.get(a - 1).tipo() == TipoRegiao.URBANA
								|| mapa.get(b - 1).tipo() == TipoRegiao.URBANA
								|| mapa.get(c - 1).tipo() == TipoRegiao.URBANA;
						achou = urbana && service.conectadas(List.of(a, b, c));
					}
				}
			}
			assertTrue(achou, "semente " + s);
		}
	}

	@Test
	void conectadasUsaVizinhancaOrtogonalDoConjunto() {
		assertTrue(service.conectadas(List.of(1, 3, 2)));
		assertFalse(service.conectadas(List.of(1, 3, 6)));
		assertTrue(service.conectadas(List.of(6, 7, 10)));
		assertFalse(service.conectadas(List.of(1, 6, 11)));
	}

	@Test
	void somarBonusReproduzExemplo2() {
		List<RegiaoGerada> mapa = new ArrayList<>();
		for (int i = 1; i <= 16; i++) {
			mapa.add(new RegiaoGerada(i, TipoRegiao.FLORESTA, List.of(
					new BonusGerado(BonusRegiao.FLORESTA, 1, 35),
					new BonusGerado(BonusRegiao.BARREIRO, 2, 16),
					new BonusGerado(BonusRegiao.PLANTACOES, 3, 5))));
		}
		mapa.set(5, new RegiaoGerada(6, TipoRegiao.URBANA, List.of(
				new BonusGerado(BonusRegiao.COMERCIO, 1, 47),
				new BonusGerado(BonusRegiao.INDUSTRIA, 2, 30),
				new BonusGerado(BonusRegiao.DESENVOLVIMENTO, 3, 12))));
		mapa.set(6, new RegiaoGerada(7, TipoRegiao.LITORAL, List.of(
				new BonusGerado(BonusRegiao.SALINAS, 1, 38),
				new BonusGerado(BonusRegiao.MILITAR, 2, 20),
				new BonusGerado(BonusRegiao.ENXOFRE, 3, 6))));
		mapa.set(9, new RegiaoGerada(10, TipoRegiao.PLANICIE, List.of(
				new BonusGerado(BonusRegiao.CRIACOES, 1, 41),
				new BonusGerado(BonusRegiao.FLORESTA, 2, 25),
				new BonusGerado(BonusRegiao.PLANTACOES, 3, 14))));

		Map<BonusRegiao, Integer> total = service.somarBonus(mapa, List.of(6, 7, 10));

		assertEquals(13, total.size());
		assertEquals(47, total.get(BonusRegiao.COMERCIO));
		assertEquals(41, total.get(BonusRegiao.CRIACOES));
		assertEquals(38, total.get(BonusRegiao.SALINAS));
		assertEquals(30, total.get(BonusRegiao.INDUSTRIA));
		assertEquals(25, total.get(BonusRegiao.FLORESTA));
		assertEquals(20, total.get(BonusRegiao.MILITAR));
		assertEquals(14, total.get(BonusRegiao.PLANTACOES));
		assertEquals(12, total.get(BonusRegiao.DESENVOLVIMENTO));
		assertEquals(6, total.get(BonusRegiao.ENXOFRE));
		assertEquals(0, total.get(BonusRegiao.ROCHA));
		assertEquals(0, total.get(BonusRegiao.FERRO));
		assertEquals(0, total.get(BonusRegiao.CARVAO));
		assertEquals(0, total.get(BonusRegiao.BARREIRO));
	}
}
