package com.example.loginbase.jogo.quartel;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;

/** Regras de formação de tropa (tropas.md R1–R6); puro, sem acesso a banco. */
@Component
public class TropaValidator {

	public static final int IDADE_MIN = 16;
	public static final int IDADE_MAX = 54;

	/**
	 * Motivo pelo qual o cidadão não pode entrar em tropa, ou vazio se é elegível.
	 *
	 * @param peBaseGuerreiro PE base (pontos_base) de Guerreiro
	 * @param temArma         há item equipado no slot ARMA
	 */
	public Optional<String> motivoInelegivel(Cidadao c, int peBaseGuerreiro, boolean temArma) {
		if (!c.isVivo()) {
			return Optional.of("Cidadão morto");
		}
		if (c.getEstado() != EstadoCidadao.SAUDAVEL) {
			return Optional.of("Cidadão ferido");
		}
		if (c.getIdadeAnos() < IDADE_MIN || c.getIdadeAnos() > IDADE_MAX) {
			return Optional.of("Idade fora do intervalo de combate (16–54)");
		}
		if (c.getTropaId() != null) {
			return Optional.of("Cidadão já está em tropa");
		}
		if (c.getConstrucaoId() != null) {
			return Optional.of("Cidadão alocado em prédio");
		}
		if (peBaseGuerreiro < 1) {
			return Optional.of("PE Guerreiro insuficiente");
		}
		if (!temArma) {
			return Optional.of("Cidadão não tem arma equipada");
		}
		return Optional.empty();
	}

	/** @throws JogoException 400 com o motivo se o cidadão não é elegível */
	public void validarMembro(Cidadao c, int peBaseGuerreiro, boolean temArma) {
		motivoInelegivel(c, peBaseGuerreiro, temArma).ifPresent(m -> {
			throw erro(m);
		});
	}

	/** Quartel precisa estar ATIVA e ter ao menos um instrutor para formar tropas. */
	public void validarQuartel(Construcao quartel, long instrutores) {
		if (quartel.getEstado() != EstadoConstrucao.ATIVA) {
			throw erro("Quartel não está ativo");
		}
		if (instrutores < 1) {
			throw erro("Quartel sem instrutor");
		}
	}

	/** Capacidade TOTAL do quartel (soma de membros de todas as suas tropas). */
	public void validarCapacidade(NivelConstrucao nivel, long membrosAtuais, int novosMembros) {
		if (membrosAtuais + novosMembros > ConstrucaoCatalogo.capacidadeQuartel(nivel)) {
			throw erro("Capacidade do quartel excedida");
		}
	}

	public void validarLimiteTropas(NivelConstrucao nivel, long tropasAtuais) {
		if (tropasAtuais >= ConstrucaoCatalogo.maxTropasQuartel(nivel)) {
			throw erro("Limite de tropas do quartel atingido");
		}
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}

}
