package com.example.loginbase.jogo.item;

/** Porta para o módulo de tropas: informa se o cidadão está em expedição (viagem de ida ou volta). */
public interface ConsultaTropas {

	boolean emExpedicao(Long cidadaoId);
}
