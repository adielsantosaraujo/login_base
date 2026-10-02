package com.example.loginbase.jogo.masmorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.ItemBonusGerador;
import com.example.loginbase.jogo.item.ItemCategoria;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.masmorra.Recompensas.ItemSorteado;
import com.example.loginbase.jogo.pedra.GeradorPedras;
import com.example.loginbase.jogo.pedra.GeradorPedras.PedraSorteada;
import com.example.loginbase.jogo.recurso.Recurso;

/** Gerador puro (sem persistência) das recompensas de masmorra, determinístico pela semente da batalha. */
@Component
public class GeradorRecompensas {

	private static final List<Recurso> RECURSOS = List.of(Recurso.MADEIRA, Recurso.PEDRA, Recurso.FERRO,
			Recurso.COURO_CURTIDO, Recurso.TECIDO);
	private static final List<ItemCategoria> CATEGORIAS = List.of(ItemCategoria.ARMA, ItemCategoria.ARMADURA,
			ItemCategoria.JOIA);
	private static final List<CodigoBonus> ATRIBUTOS_ANEL = List.of(CodigoBonus.VIT, CodigoBonus.FOR, CodigoBonus.VEL,
			CodigoBonus.INT, CodigoBonus.CAR);

	private final ItemBonusGerador itemBonusGerador;
	private final GeradorPedras geradorPedras;

	public GeradorRecompensas(ItemBonusGerador itemBonusGerador, GeradorPedras geradorPedras) {
		this.itemBonusGerador = itemBonusGerador;
		this.geradorPedras = geradorPedras;
	}

	public static double chanceItem(int n) {
		return Math.min(0.8, 0.1 * n);
	}

	public Recompensas gerar(int n, long sementeBatalha) {
		Random rng = new Random(sementeBatalha ^ 0x5DEECE66DL);

		int ouro = 40 * n + rng.nextInt(20 * n + 1);

		List<Recurso> pool = new ArrayList<>(RECURSOS);
		if (n >= 6) {
			pool.add(Recurso.ACO);
		}
		Map<Recurso, Integer> recursos = new LinkedHashMap<>();
		for (int i = 0; i < n; i++) {
			recursos.merge(pool.get(rng.nextInt(pool.size())), 10 * n, Integer::sum);
		}

		ItemSorteado item = null;
		if (rng.nextDouble() < chanceItem(n)) {
			ItemCategoria cat = CATEGORIAS.get(rng.nextInt(CATEGORIAS.size()));
			List<ItemSubtipo> subtipos = Arrays.stream(ItemSubtipo.values()).filter(s -> s.getCategoria() == cat)
					.toList();
			ItemSubtipo subtipo = subtipos.get(rng.nextInt(subtipos.size()));
			Qualidade q = itemBonusGerador.gerarQualidadePorMargem(n <= 7 ? 0 : 15, true, rng.nextLong());
			var bonus = itemBonusGerador.gerarBonusIntrinsecos(q, cat, n, rng.nextLong());
			CodigoBonus atributo = subtipo == ItemSubtipo.ANEL ? ATRIBUTOS_ANEL.get(rng.nextInt(ATRIBUTOS_ANEL.size()))
					: null;
			item = new ItemSorteado(subtipo, n, q, bonus, atributo);
		}

		List<PedraSorteada> pedras = new ArrayList<>();
		for (int i = 0; i < GeradorPedras.sorteios(n); i++) {
			geradorPedras.sortear(n, rng).ifPresent(pedras::add);
		}
		return new Recompensas(ouro, recursos, item, n, pedras);
	}
}
