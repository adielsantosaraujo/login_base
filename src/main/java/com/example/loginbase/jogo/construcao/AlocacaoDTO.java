package com.example.loginbase.jogo.construcao;

import com.example.loginbase.jogo.cidadao.Profissao;

public record AlocacaoDTO(Long cidadaoId, String nome, int idadeAnos, Profissao profissao, double eficiencia) {

	static AlocacaoDTO de(ConsultaTrabalhadores.Trabalhador t) {
		return new AlocacaoDTO(t.cidadao().getId(), t.cidadao().getNome(), t.cidadao().getIdadeAnos(),
				t.profissao(), t.eficiencia());
	}
}
