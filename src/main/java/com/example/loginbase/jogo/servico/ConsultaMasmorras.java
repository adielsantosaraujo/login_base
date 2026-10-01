package com.example.loginbase.jogo.servico;

import java.util.Optional;

import com.example.loginbase.jogo.modelo.Vila;

/**
 * Porta de consulta de masmorras ativas por região (implementada pela v1-001).
 */
public interface ConsultaMasmorras {

	/** Nível da masmorra ativa na região, ou vazio se não houver. */
	Optional<Integer> nivelMasmorraAtiva(Vila vila, int indice);

}
