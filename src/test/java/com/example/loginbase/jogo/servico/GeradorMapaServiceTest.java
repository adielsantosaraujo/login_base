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

import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.servico.GeradorMapaService.TerrenoGerado;
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
	void terrenosDistintosDoTipoEPercentuaisNosLimitesDoD2() {
		Set<Integer> vistosB1 = new HashSet<>();
		Set<Integer> vistosB2 = new HashSet<>();
		Set<Integer> vistosB3 = new HashSet<>();
		for (long s = 0; s < SEMENTES; s++) {
			for (RegiaoGerada r : service.gerar(s)) {
				assertEquals(3, r.terrenos().size(), "R13 semente " + s);
				Set<TipoTerreno> distintos = new HashSet<>();
				int soma = 0;
				for (int p = 0; p < 3; p++) {
					TerrenoGerado t = r.terrenos().get(p);
					assertEquals(p + 1, t.posicao(), "ordem por posição");
					assertTrue(r.tipo().terrenos().contains(t.terreno()), "R13 terreno do tipo");
					distintos.add(t.terreno());
					soma += t.percentual();
				}
				int b1 = r.terrenos().get(0).percentual();
				int b2 = r.terrenos().get(1).percentual();
				int b3 = r.terrenos().get(2).percentual();
				assertEquals(100, soma, "soma semente " + s);
				assertTrue(b1 >= 20 && b1 <= 60, "b1 semente " + s + " = " + b1);
				assertTrue(b2 >= 20 && b2 <= 90 - b1, "b2 semente " + s + " = " + b2);
				assertTrue(b3 >= 10 && b3 <= 60, "b3 semente " + s + " = " + b3);
				assertEquals(3, distintos.size(), "R13 distintos");
				vistosB1.add(b1);
				vistosB2.add(b2);
				vistosB3.add(b3);
			}
		}
		assertTrue(vistosB1.contains(20) || vistosB1.contains(21), "b1 perto do mínimo");
		assertTrue(vistosB1.stream().anyMatch(v -> v >= 59), "b1 perto do máximo");
		assertTrue(vistosB2.stream().anyMatch(v -> v <= 21), "b2 perto do mínimo");
		assertTrue(vistosB3.stream().anyMatch(v -> v <= 11), "b3 perto do mínimo");
	}

	@Test
	void todoTerrenoApareceEmTodasAsPosicoes() {
		Map<TipoRegiao, Map<TipoTerreno, Set<Integer>>> vistos = new EnumMap<>(TipoRegiao.class);
		for (long s = 0; s < SEMENTES; s++) {
			for (RegiaoGerada r : service.gerar(s)) {
				Map<TipoTerreno, Set<Integer>> porTerreno = vistos.computeIfAbsent(r.tipo(), t -> new EnumMap<>(TipoTerreno.class));
				for (TerrenoGerado t : r.terrenos()) {
					porTerreno.computeIfAbsent(t.terreno(), x -> new HashSet<>()).add(t.posicao());
				}
			}
		}
		for (TipoRegiao t : TipoRegiao.atuais()) {
			for (TipoTerreno b : t.terrenos()) {
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
}
