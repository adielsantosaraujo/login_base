package com.example.loginbase.jogo.masmorra.combate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.loginbase.jogo.catalogo.MapaMasmorra;

/**
 * Estado imutável de uma batalha tática. Cada método devolve um novo
 * {@link EstadoBatalha}, sem mutar o anterior.
 *
 * @param combatentes  combatentes vivos na batalha (jogador e inimigos).
 * @param turno        turno atual (inicia em 1).
 * @param turnoMaximo  turno máximo antes de derrota automática (30).
 * @param mapa         mapa tático (grade, obstáculos, spawns).
 * @param resultado    {@code NULO} enquanto a batalha está em andamento.
 * @param log          log textual acumulado, uma linha por evento.
 */
public record EstadoBatalha(List<Combatente> combatentes, int turno, int turnoMaximo, MapaMasmorra mapa,
		Resultado resultado, List<String> log) {

	public enum Resultado {
		NULO,
		VITORIA,
		DERROTA
	}

	public EstadoBatalha {
		combatentes = List.copyOf(combatentes);
		log = List.copyOf(log);
	}

	public Optional<Combatente> buscar(String id) {
		return combatentes.stream().filter(c -> c.id().equals(id)).findFirst();
	}

	/** Combatentes vivos do {@code lado} informado. */
	public List<Combatente> combatentesDoLado(Lado lado) {
		return combatentes.stream().filter(c -> c.lado() == lado && c.vivo()).toList();
	}

	public boolean todosMortos(Lado lado) {
		return combatentesDoLado(lado).isEmpty();
	}

	/** Novo estado com {@code atualizado} substituindo o combatente de mesmo id. */
	public EstadoBatalha atualizarCombatente(Combatente atualizado) {
		List<Combatente> novos = new ArrayList<>(combatentes);
		for (int i = 0; i < novos.size(); i++) {
			if (novos.get(i).id().equals(atualizado.id())) {
				novos.set(i, atualizado);
				break;
			}
		}
		return new EstadoBatalha(novos, turno, turnoMaximo, mapa, resultado, log);
	}

	/** Novo estado sem o combatente de id {@code id} (morto, "sai do mapa"). */
	public EstadoBatalha removerCombatente(String id) {
		List<Combatente> novos = combatentes.stream().filter(c -> !c.id().equals(id)).toList();
		return new EstadoBatalha(novos, turno, turnoMaximo, mapa, resultado, log);
	}

	/** Novo estado com uma linha adicionada ao log. */
	public EstadoBatalha adicionarLog(String linha) {
		List<String> novoLog = new ArrayList<>(log);
		novoLog.add(linha);
		return new EstadoBatalha(combatentes, turno, turnoMaximo, mapa, resultado, novoLog);
	}

	/** Novo estado com {@code novoResultado} (fim da batalha). */
	public EstadoBatalha encerrar(Resultado novoResultado) {
		return new EstadoBatalha(combatentes, turno, turnoMaximo, mapa, novoResultado, log);
	}

}
