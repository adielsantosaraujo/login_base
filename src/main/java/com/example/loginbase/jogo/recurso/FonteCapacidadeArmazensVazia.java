package com.example.loginbase.jogo.recurso;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;

/** Implementação padrão: nenhum Armazém ativo. */
@Component
@ConditionalOnMissingBean(value = FonteCapacidadeArmazens.class, ignored = FonteCapacidadeArmazensVazia.class)
public class FonteCapacidadeArmazensVazia implements FonteCapacidadeArmazens {

	@Override
	public List<ArmazemAtivo> armazensAtivos(Vila vila) {
		return List.of();
	}

}
