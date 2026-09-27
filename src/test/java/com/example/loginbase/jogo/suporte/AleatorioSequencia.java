package com.example.loginbase.jogo.suporte;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.loginbase.jogo.config.Aleatorio;

/**
 * {@link Aleatorio} determinístico para testes: retorna os valores
 * configurados em sequência (ignorando o parâmetro {@code limite}), na ordem
 * em que {@link #proximoInt(int)} é chamado.
 */
public class AleatorioSequencia implements Aleatorio {

	private final List<Integer> valores;
	private final AtomicInteger indice = new AtomicInteger(0);

	public AleatorioSequencia(Integer... valores) {
		this.valores = List.of(valores);
	}

	public AleatorioSequencia(List<Integer> valores) {
		this.valores = List.copyOf(valores);
	}

	@Override
	public int proximoInt(int limite) {
		int posicao = indice.getAndIncrement();
		if (posicao >= valores.size()) {
			throw new IllegalStateException(
					"AleatorioSequencia esgotada: apenas " + valores.size() + " valor(es) configurado(s)");
		}
		return valores.get(posicao);
	}

}
