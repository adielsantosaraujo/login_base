package com.example.loginbase.jogo.masmorra;

import com.example.loginbase.jogo.batalha.Combatente;
import com.example.loginbase.jogo.batalha.TipoInimigo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

/** Gera os inimigos de uma masmorra de nível N de forma determinística pela semente. */
@Component
public class GeradorInimigos {

	/** Comuns primeiro (min(8; 2 + N)) e chefe por último (a partir de N3); índices 0..n-1. */
	public List<Combatente> gerar(int nivel, long semente) {
		Random rng = new Random(semente);
		List<TipoInimigo> grupo = GrupoInimigos.comuns(nivel);
		int quantidade = Math.min(8, 2 + nivel);
		List<Combatente> inimigos = new ArrayList<>();
		for (int i = 0; i < quantidade; i++) {
			TipoInimigo tipo = grupo.get(rng.nextInt(grupo.size()));
			inimigos.add(tipo.criar(i, nivel));
		}
		GrupoInimigos.chefe(nivel).ifPresent(c -> inimigos.add(c.criar(inimigos.size(), nivel)));
		return inimigos;
	}
}
