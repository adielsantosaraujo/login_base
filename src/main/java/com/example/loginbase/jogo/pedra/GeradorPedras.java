package com.example.loginbase.jogo.pedra;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.FaixaBonus;
import com.example.loginbase.jogo.item.ItemBonusGerador;

/** Gerador puro (não persiste nada) de pedras de masmorra, determinístico por {@link Random}. */
@Component
public class GeradorPedras {

	public record PedraSorteada(TipoPedra tipo, List<BonusPedra> bonus) {
	}

	private final ItemBonusGerador itemBonusGerador = new ItemBonusGerador();

	/** Número de sorteios de pedra de uma masmorra de nível N: 1 + N/3. */
	public static int sorteios(int nivel) {
		return 1 + nivel / 3;
	}

	/** Faixa de magnitude pelo nível da masmorra: N1-6 BAIXA, N7-9 MEDIA, N10 ALTA. */
	public static FaixaBonus faixa(int nivel) {
		if (nivel >= 10) {
			return FaixaBonus.ALTA;
		}
		return nivel >= 7 ? FaixaBonus.MEDIA : FaixaBonus.BAIXA;
	}

	/** Um sorteio: consome exatamente um nextInt(100); "Nada" retorna vazio. */
	public Optional<PedraSorteada> sortear(int nivel, Random rng) {
		// percentuais por faixa: Nada, Simples, Boa, Excelente (Divina = resto)
		int[] p = nivel >= 10 ? new int[] { 10, 20, 35, 27 }
				: nivel >= 7 ? new int[] { 20, 30, 30, 17 }
				: nivel >= 4 ? new int[] { 30, 45, 20, 5 }
				: new int[] { 50, 45, 5, 0 };
		int r = rng.nextInt(100);
		int limite = p[0];
		if (r < limite) {
			return Optional.empty();
		}
		TipoPedra tipo;
		if (r < (limite += p[1])) {
			tipo = TipoPedra.SIMPLES;
		} else if (r < (limite += p[2])) {
			tipo = TipoPedra.BOA;
		} else if (r < (limite += p[3])) {
			tipo = TipoPedra.EXCELENTE;
		} else {
			tipo = TipoPedra.DIVINA;
		}
		FaixaBonus faixa = faixa(nivel);
		List<CodigoBonus> pool = new ArrayList<>(List.of(CodigoBonus.values()));
		List<BonusPedra> bonus = new ArrayList<>();
		for (int i = 0; i < tipo.getQtdBonus(); i++) {
			CodigoBonus c = pool.remove(rng.nextInt(pool.size()));
			bonus.add(new BonusPedra(c, faixa, itemBonusGerador.magnitude(c, faixa)));
		}
		return Optional.of(new PedraSorteada(tipo, bonus));
	}
}
