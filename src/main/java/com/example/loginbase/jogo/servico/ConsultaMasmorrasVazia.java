package com.example.loginbase.jogo.servico;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;

/** Implementação padrão: nenhuma masmorra ativa. */
@Component
@ConditionalOnMissingBean(value = ConsultaMasmorras.class, ignored = ConsultaMasmorrasVazia.class)
public class ConsultaMasmorrasVazia implements ConsultaMasmorras {

	@Override
	public Optional<Integer> nivelMasmorraAtiva(Vila vila, int indice) {
		return Optional.empty();
	}

}
