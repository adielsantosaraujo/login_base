package com.example.loginbase.jogo.item;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.CalculadoraPeEfetivo;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PeArtesao {

	private final CidadaoProfissaoRepository profissaoRepository;
	private final CalculadoraPeEfetivo calculadora;

	public int peEfetivo(Cidadao cidadao, Profissao profissao) {
		int base = profissaoRepository.findByCidadaoIdAndProfissao(cidadao.getId(), profissao)
				.map(CidadaoProfissao::getPontosBase).orElse(0);
		return calculadora.calcular(cidadao, profissao, base);
	}
}
