package com.example.loginbase.jogo.batalha;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * Resolve uma batalha por rodadas de forma determinística (puro, sem banco).
 *
 * <p>Ordem fixa de consumo do {@link Random}(semente): por rodada, 1d6 de cada vivo (tropa na ordem recebida, depois
 * inimigos); por ação: sorteio de alvo (só inimigos, {@code nextInt}), dano ({@code nextDouble}), crítico
 * ({@code nextDouble}). Quem não tem alvo válido perde a ação sem consumir sorteios.
 */
@Component
public class MotorBatalha {

	public static final int MAX_RODADAS = 30;
	private static final double MULT_CRITICO = 1.5;

	public ResultadoBatalha resolver(List<Combatente> tropa, List<Combatente> inimigos, long semente) {
		Random rng = new Random(semente);
		Map<Combatente, Integer> pv = new IdentityHashMap<>();
		List<Combatente> todos = new ArrayList<>(tropa);
		todos.addAll(inimigos);
		for (Combatente c : todos) {
			pv.put(c, c.pvMax());
		}
		List<AcaoBatalha> log = new ArrayList<>();
		List<Long> abatidosTropa = new ArrayList<>();

		int rodada = 0;
		ResultadoCombate resultado = null;
		if (inimigos.isEmpty()) {
			resultado = ResultadoCombate.VITORIA;
		} else if (tropa.isEmpty()) {
			resultado = ResultadoCombate.DERROTA;
		}
		while (resultado == null && rodada < MAX_RODADAS) {
			rodada++;
			List<Combatente> vivos = todos.stream().filter(c -> pv.get(c) > 0).toList();
			for (Combatente ator : CalculadorIniciativa.ordenar(vivos, rng)) {
				if (pv.get(ator) <= 0) {
					continue;
				}
				List<Combatente> oponentes = vivosDoLado(todos, pv,
						ator.lado() == LadoCombate.TROPA ? LadoCombate.INIMIGO : LadoCombate.TROPA);
				List<Combatente> permitidos = ValidadorAlvo.alvosPermitidos(ator, oponentes);
				if (permitidos.isEmpty()) {
					continue;
				}
				Combatente alvo = ator.lado() == LadoCombate.TROPA ? menorPv(permitidos, pv)
						: permitidos.get(rng.nextInt(permitidos.size()));
				int dano = CalculoDano.dano(ator.ataque(), CalculoDano.defesaEfetiva(alvo.defesa(), ator.ignoraDefesa25()),
						rng);
				boolean critico = rng.nextDouble() * 100 < ator.criticoPp();
				if (critico) {
					dano = (int) Math.round(dano * MULT_CRITICO);
				}
				int antes = pv.get(alvo);
				int depois = Math.max(0, antes - dano);
				pv.put(alvo, depois);
				boolean abatido = depois == 0;
				log.add(new AcaoBatalha(rodada, ator.id(), ator.lado(), alvo.id(), alvo.lado(), dano, critico, antes,
						depois, abatido));
				if (abatido && alvo.lado() == LadoCombate.TROPA) {
					abatidosTropa.add(alvo.id());
				}
				if (vivosDoLado(todos, pv, alvo.lado()).isEmpty()) {
					resultado = alvo.lado() == LadoCombate.INIMIGO ? ResultadoCombate.VITORIA
							: ResultadoCombate.DERROTA;
					break;
				}
			}
		}
		if (resultado == null) {
			resultado = ResultadoCombate.DERROTA; // 30 rodadas sem vencedor: a tropa recua
		}
		List<ResultadoBatalha.PvFinal> finais = todos.stream()
				.map(c -> new ResultadoBatalha.PvFinal(c.id(), c.lado(), c.nome(), c.pvMax(), pv.get(c))).toList();
		return new ResultadoBatalha(resultado, rodada, List.copyOf(log), List.copyOf(abatidosTropa), finais);
	}

	private static List<Combatente> vivosDoLado(List<Combatente> todos, Map<Combatente, Integer> pv, LadoCombate lado) {
		return todos.stream().filter(c -> c.lado() == lado && pv.get(c) > 0).toList();
	}

	/** Menor PV atual; empate resolvido pela ordem da lista (estável). */
	private static Combatente menorPv(List<Combatente> alvos, Map<Combatente, Integer> pv) {
		Combatente melhor = alvos.get(0);
		for (Combatente c : alvos) {
			if (pv.get(c) < pv.get(melhor)) {
				melhor = c;
			}
		}
		return melhor;
	}
}
