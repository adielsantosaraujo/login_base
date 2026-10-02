package com.example.loginbase.jogo.pedra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.FaixaBonus;
import com.example.loginbase.jogo.pedra.GeradorPedras.PedraSorteada;

class GeradorPedrasTest {

	private final GeradorPedras gerador = new GeradorPedras();

	@Test
	void sorteiosPorNivel() {
		int[] esperado = { 1, 1, 2, 2, 2, 3, 3, 3, 4, 4 };
		for (int n = 1; n <= 10; n++) {
			assertThat(GeradorPedras.sorteios(n)).as("nivel %d", n).isEqualTo(esperado[n - 1]);
		}
	}

	@Test
	void faixaPorNivel() {
		for (int n = 1; n <= 6; n++) {
			assertThat(GeradorPedras.faixa(n)).isEqualTo(FaixaBonus.BAIXA);
		}
		for (int n = 7; n <= 9; n++) {
			assertThat(GeradorPedras.faixa(n)).isEqualTo(FaixaBonus.MEDIA);
		}
		assertThat(GeradorPedras.faixa(10)).isEqualTo(FaixaBonus.ALTA);
	}

	@Test
	void numeroDeBonusPorTipoEFaixaPorNivel() {
		Random rng = new Random(7);
		for (int n : new int[] { 1, 5, 8, 10 }) {
			for (int i = 0; i < 2000; i++) {
				Optional<PedraSorteada> p = gerador.sortear(n, rng);
				p.ifPresent(s -> {
					assertThat(s.bonus()).hasSize(s.tipo().getQtdBonus());
					assertThat(s.bonus()).allMatch(b -> b.magnitude() == GeradorPedras.faixa(n) && b.valor() > 0);
				});
			}
		}
	}

	@Test
	void bonusDistintosEValorDoGeradorDeItens() {
		Random rng = new Random(99);
		int divinas = 0;
		for (int i = 0; i < 3000; i++) {
			Optional<PedraSorteada> p = gerador.sortear(10, rng);
			if (p.isPresent() && p.get().tipo() == TipoPedra.DIVINA) {
				divinas++;
				Set<CodigoBonus> codigos = new HashSet<>();
				p.get().bonus().forEach(b -> codigos.add(b.codigo()));
				assertThat(codigos).hasSize(4);
				p.get().bonus().forEach(b -> assertThat(b.valor())
						.isEqualTo(new com.example.loginbase.jogo.item.ItemBonusGerador().magnitude(b.codigo(), FaixaBonus.ALTA)));
			}
		}
		assertThat(divinas).isPositive();
	}

	@Test
	void reprodutivelComMesmaSemente() {
		Random a = new Random(12345);
		Random b = new Random(12345);
		for (int i = 0; i < 200; i++) {
			assertThat(gerador.sortear(8, a)).isEqualTo(gerador.sortear(8, b));
		}
	}

	@Test
	void nadaConsomeOSorteio() {
		// um sorteio consome 1 nextInt(100) se Nada, mais N nextInt se pedra; o rng avança de forma consistente
		Random rng = new Random(1);
		Random ref = new Random(1);
		int r = ref.nextInt(100);
		Optional<PedraSorteada> p = gerador.sortear(1, rng);
		assertThat(p.isEmpty()).isEqualTo(r < 50);
	}

	@Test
	void distribuicaoAproximada() {
		verificar(3, new double[] { 50, 45, 5, 0, 0 });
		verificar(5, new double[] { 30, 45, 20, 5, 0 });
		verificar(8, new double[] { 20, 30, 30, 17, 3 });
		verificar(10, new double[] { 10, 20, 35, 27, 8 });
	}

	private void verificar(int nivel, double[] pct) {
		int total = 100_000;
		Random rng = new Random(12345);
		int nada = 0;
		Map<TipoPedra, Integer> cont = new EnumMap<>(TipoPedra.class);
		for (int i = 0; i < total; i++) {
			Optional<PedraSorteada> p = gerador.sortear(nivel, rng);
			if (p.isEmpty()) {
				nada++;
			} else {
				cont.merge(p.get().tipo(), 1, Integer::sum);
			}
		}
		assertThat(nada * 100.0 / total).as("nada N%d", nivel).isCloseTo(pct[0], org.assertj.core.data.Offset.offset(1.0));
		TipoPedra[] tipos = TipoPedra.values();
		for (int i = 0; i < tipos.length; i++) {
			assertThat(cont.getOrDefault(tipos[i], 0) * 100.0 / total).as("%s N%d", tipos[i], nivel)
					.isCloseTo(pct[i + 1], org.assertj.core.data.Offset.offset(1.0));
		}
	}
}
