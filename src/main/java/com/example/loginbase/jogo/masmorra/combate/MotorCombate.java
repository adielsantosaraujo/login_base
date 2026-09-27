package com.example.loginbase.jogo.masmorra.combate;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.MapaMasmorra;

/**
 * Motor puro de combate tático: processa ações do jogador (mover, atacar,
 * defender, encerrar turno, render), executa a IA determinística dos
 * inimigos e controla o fim da batalha (vitória/derrota). Sem persistência
 * e sem efeitos colaterais — cada método devolve um novo
 * {@link EstadoBatalha}, sem mutar o anterior.
 */
public final class MotorCombate {

	private MotorCombate() {
	}

	/**
	 * Processa {@code acao} sobre {@code estado} e devolve o novo estado.
	 * Lança {@link RegraJogoException} com {@link CodigoErro#TURNO_DESATUALIZADO}
	 * (409) se {@code acao.turno()} não corresponder ao turno atual, com
	 * {@link CodigoErro#BATALHA_ENCERRADA} se a batalha já terminou, ou com
	 * {@link CodigoErro#ACAO_INVALIDA} se a ação não puder ser executada.
	 */
	public static EstadoBatalha processar(EstadoBatalha estado, AcaoCombate acao) {
		if (terminou(estado)) {
			throw new RegraJogoException(CodigoErro.BATALHA_ENCERRADA, "A batalha já terminou.");
		}
		if (acao.turno() != estado.turno()) {
			throw new RegraJogoException(CodigoErro.TURNO_DESATUALIZADO,
					"Turno informado (" + acao.turno() + ") não corresponde ao turno atual (" + estado.turno()
							+ ").");
		}
		return switch (acao.tipo()) {
			case MOVER -> mover(estado, acao);
			case ATACAR -> atacar(estado, acao);
			case DEFENDER -> defender(estado, acao);
			case ENCERRAR_TURNO -> encerrarTurno(estado);
			case RENDER -> render(estado);
		};
	}

	public static boolean terminou(EstadoBatalha estado) {
		return estado.resultado() != EstadoBatalha.Resultado.NULO;
	}

	public static EstadoBatalha.Resultado resultado(EstadoBatalha estado) {
		return estado.resultado();
	}

	/**
	 * Executa a IA de todos os inimigos vivos, em ordem lexicográfica de id
	 * (I1..I5): escolhe alvo, ataca se ao alcance ou move (BFS) e ataca em
	 * seguida se possível. Encerra a batalha em {@code DERROTA} imediata se,
	 * a qualquer momento, todas as unidades do jogador morrerem.
	 */
	public static EstadoBatalha executarIA(EstadoBatalha estadoInicial) {
		EstadoBatalha estado = estadoInicial;
		List<Combatente> ordem = estado.combatentesDoLado(Lado.INIMIGO).stream()
				.sorted(Comparator.comparing(Combatente::id))
				.toList();
		for (Combatente referencia : ordem) {
			if (estado.todosMortos(Lado.JOGADOR)) {
				return estado.encerrar(EstadoBatalha.Resultado.DERROTA);
			}
			Combatente inimigo = estado.buscar(referencia.id()).orElse(null);
			if (inimigo == null || !inimigo.vivo()) {
				continue;
			}
			Combatente alvo = escolherAlvo(estado, inimigo);
			if (alvo == null) {
				continue;
			}
			if (inimigo.posicao().distanciaManhattan(alvo.posicao()) > inimigo.alcance()) {
				MapaMasmorra mapa = estado.mapa();
				Set<Posicao> obstaculos = obstaculosDe(mapa);
				Set<Posicao> alcancaveis = Caminhos.casasAlcancaveis(inimigo.posicao(), inimigo.movimento(),
						mapa.largura(), mapa.altura(), obstaculos, estado.combatentes());
				Posicao melhor = alcancaveis.stream()
						.min(Comparator
								.comparingInt((Posicao p) -> p.distanciaManhattan(alvo.posicao()))
								.thenComparingInt(Posicao::y)
								.thenComparingInt(Posicao::x))
						.orElse(inimigo.posicao());
				if (!melhor.equals(inimigo.posicao())) {
					Combatente movido = inimigo.moverPara(melhor);
					estado = estado.atualizarCombatente(movido)
							.adicionarLog(movido.id() + " moveu para (" + melhor.x() + "," + melhor.y() + ").");
					inimigo = movido;
				}
			}
			if (inimigo.posicao().distanciaManhattan(alvo.posicao()) <= inimigo.alcance()) {
				estado = aplicarAtaque(estado, inimigo, alvo);
				if (estado.todosMortos(Lado.JOGADOR)) {
					return estado.encerrar(EstadoBatalha.Resultado.DERROTA);
				}
			}
		}
		return estado;
	}

	// ---- Ações do jogador ----

	private static EstadoBatalha mover(EstadoBatalha estado, AcaoCombate acao) {
		Combatente combatente = combatenteDoJogador(estado, acao.combatenteId());
		if (combatente.moveu() || combatente.agiu()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Combatente já moveu ou agiu neste turno.");
		}
		Posicao destino = new Posicao(acao.x(), acao.y());
		MapaMasmorra mapa = estado.mapa();
		if (destino.x() < 0 || destino.x() >= mapa.largura() || destino.y() < 0 || destino.y() >= mapa.altura()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Destino fora da grade.");
		}
		Set<Posicao> obstaculos = obstaculosDe(mapa);
		if (obstaculos.contains(destino) || ocupado(estado, destino)) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Destino é um obstáculo ou está ocupado.");
		}
		List<Posicao> caminho = Caminhos.caminhoMaisCurto(combatente.posicao(), destino, combatente.movimento(),
				mapa.largura(), mapa.altura(), obstaculos, estado.combatentes());
		if (caminho.isEmpty()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Destino não alcançável.");
		}
		Combatente movido = combatente.moverPara(destino);
		return estado.atualizarCombatente(movido)
				.adicionarLog(movido.id() + " moveu para (" + destino.x() + "," + destino.y() + ").");
	}

	private static EstadoBatalha atacar(EstadoBatalha estado, AcaoCombate acao) {
		Combatente atacante = combatenteDoJogador(estado, acao.combatenteId());
		if (atacante.agiu()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Combatente já agiu neste turno.");
		}
		Combatente alvo = estado.buscar(acao.alvoId())
				.filter(Combatente::vivo)
				.orElseThrow(() -> new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Alvo inválido ou morto."));
		if (alvo.lado() == atacante.lado()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Alvo não é inimigo.");
		}
		if (atacante.posicao().distanciaManhattan(alvo.posicao()) > atacante.alcance()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Alvo fora de alcance.");
		}
		EstadoBatalha novoEstado = aplicarAtaque(estado, atacante, alvo);
		if (atacante.lado() == Lado.JOGADOR && novoEstado.todosMortos(Lado.INIMIGO)) {
			novoEstado = novoEstado.encerrar(EstadoBatalha.Resultado.VITORIA);
		}
		return novoEstado;
	}

	private static EstadoBatalha defender(EstadoBatalha estado, AcaoCombate acao) {
		Combatente combatente = combatenteDoJogador(estado, acao.combatenteId());
		if (combatente.agiu()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Combatente já agiu neste turno.");
		}
		Combatente defendendo = combatente.defender();
		return estado.atualizarCombatente(defendendo).adicionarLog(defendendo.id() + " defendeu.");
	}

	private static EstadoBatalha render(EstadoBatalha estado) {
		return estado.encerrar(EstadoBatalha.Resultado.DERROTA).adicionarLog("Jogador se rendeu.");
	}

	private static EstadoBatalha encerrarTurno(EstadoBatalha estado) {
		EstadoBatalha aposIa = executarIA(estado);
		if (terminou(aposIa)) {
			return aposIa;
		}
		if (aposIa.turno() >= aposIa.turnoMaximo() && !aposIa.todosMortos(Lado.INIMIGO)) {
			return aposIa.encerrar(EstadoBatalha.Resultado.DERROTA).adicionarLog("Turno máximo atingido. Derrota.");
		}
		List<Combatente> reiniciados = aposIa.combatentes().stream().map(Combatente::resetarTurno).toList();
		EstadoBatalha proximoTurno = new EstadoBatalha(reiniciados, aposIa.turno() + 1, aposIa.turnoMaximo(),
				aposIa.mapa(), aposIa.resultado(), aposIa.log());
		return proximoTurno.adicionarLog("Turno " + proximoTurno.turno() + " começou.");
	}

	// ---- IA: escolha de alvo ----

	private static Combatente escolherAlvo(EstadoBatalha estado, Combatente inimigo) {
		return estado.combatentesDoLado(Lado.JOGADOR).stream()
				.min(Comparator
						.comparingInt((Combatente c) -> c.posicao().distanciaManhattan(inimigo.posicao()))
						.thenComparingInt(Combatente::hp)
						.thenComparing(Combatente::id))
				.orElse(null);
	}

	// ---- Dano ----

	private static EstadoBatalha aplicarAtaque(EstadoBatalha estado, Combatente atacante, Combatente alvo) {
		int dano = Math.max(1, atacante.ataque() - alvo.defesaEfetiva());
		Combatente atacanteAgiu = atacante.atacar();
		Combatente alvoFerido = alvo.recebeDano(dano);
		EstadoBatalha novoEstado = estado.atualizarCombatente(atacanteAgiu)
				.adicionarLog(atacante.id() + " atacou " + alvo.id() + " causando " + dano + " dano.");
		if (alvoFerido.vivo()) {
			return novoEstado.atualizarCombatente(alvoFerido);
		}
		return novoEstado.removerCombatente(alvo.id()).adicionarLog(alvo.id() + " morreu.");
	}

	// ---- Auxiliares ----

	private static boolean ocupado(EstadoBatalha estado, Posicao posicao) {
		return estado.combatentes().stream().anyMatch(c -> c.vivo() && c.posicao().equals(posicao));
	}

	private static Combatente combatenteDoJogador(EstadoBatalha estado, String id) {
		Combatente combatente = estado.buscar(id)
				.orElseThrow(() -> new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Combatente inválido: " + id));
		if (combatente.lado() != Lado.JOGADOR) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Ação permitida apenas para combatentes do jogador.");
		}
		if (!combatente.vivo()) {
			throw new RegraJogoException(CodigoErro.ACAO_INVALIDA, "Combatente morto: " + id);
		}
		return combatente;
	}

	private static Set<Posicao> obstaculosDe(MapaMasmorra mapa) {
		return mapa.obstaculos().stream()
				.map(p -> new Posicao(p.x(), p.y()))
				.collect(Collectors.toUnmodifiableSet());
	}

}
