package com.example.loginbase.jogo.quartel;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.dominio.ContadorNome;
import com.example.loginbase.jogo.dominio.ContadorNomeRepository;

/**
 * Calcula o ordinal de nome/sobrenome de uma nova {@code Unidade} dentro de
 * uma vila (design.md D11), a partir do contador histórico persistido em
 * {@code jogo_contadores_nome}: a primeira unidade da vila com um par
 * nome-sobrenome recebe ordinal 1; cada unidade seguinte com o mesmo par
 * recebe o próximo ordinal. O contador nunca é decrementado — a morte de
 * uma unidade apaga a unidade, não o contador ({@link ContadorNome}) — então
 * um ordinal nunca é reaproveitado.
 *
 * <p>
 * Roda na transação do chamador (sem {@code REQUIRES_NEW}): a trava
 * pessimista da vila ({@code VilaService#obterParaAtualizacao}) já
 * serializa as conclusões de ordens que criam unidades, e a restrição
 * {@code UNIQUE (vila_id, nome, sobrenome)} garante integridade em caso de
 * concorrência.
 */
@Component
public class NumeradorNomes {

	private final ContadorNomeRepository contadorNomeRepository;

	public NumeradorNomes(ContadorNomeRepository contadorNomeRepository) {
		this.contadorNomeRepository = contadorNomeRepository;
	}

	/**
	 * Retorna o próximo ordinal do par {@code nome}/{@code sobrenome} na vila
	 * informada: grava (e retorna) 1 se for a primeira ocorrência; senão
	 * incrementa o contador existente e retorna o novo valor.
	 */
	public int proximoOrdinal(long vilaId, String nome, String sobrenome) {
		ContadorNome contador = contadorNomeRepository.findByVilaIdAndNomeAndSobrenome(vilaId, nome, sobrenome)
				.orElse(null);
		if (contador == null) {
			contador = new ContadorNome();
			contador.setVilaId(vilaId);
			contador.setNome(nome);
			contador.setSobrenome(sobrenome);
			contador.setUltimoOrdinal(1);
			contadorNomeRepository.save(contador);
			return 1;
		}
		contador.setUltimoOrdinal(contador.getUltimoOrdinal() + 1);
		contadorNomeRepository.save(contador);
		return contador.getUltimoOrdinal();
	}

}
