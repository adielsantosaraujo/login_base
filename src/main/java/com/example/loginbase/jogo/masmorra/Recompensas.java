package com.example.loginbase.jogo.masmorra;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.item.BonusItem;
import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.pedra.GeradorPedras.PedraSorteada;
import com.example.loginbase.jogo.recurso.Recurso;

/** Recompensas sorteadas (ainda não persistidas) de uma vitória em masmorra. */
public record Recompensas(int ouro, Map<Recurso, Integer> recursos, ItemSorteado item, int xpPorGuerreiro,
		List<PedraSorteada> pedras) {

	public record ItemSorteado(ItemSubtipo subtipo, int nivel, Qualidade qualidade, List<BonusItem> bonus,
			CodigoBonus atributoEscolhido) {
	}
}
