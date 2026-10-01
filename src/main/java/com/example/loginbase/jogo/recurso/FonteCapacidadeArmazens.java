package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.util.List;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.modelo.Vila;

/**
 * Porta para os dados dos Armazéns ativos da vila e seus Carregadores. A implementação real
 * virá com as construções/profissões; a padrão ({@link FonteCapacidadeArmazensVazia}) não tem armazéns.
 */
public interface FonteCapacidadeArmazens {

	List<ArmazemAtivo> armazensAtivos(Vila vila);

	/**
	 * @param nivel nível do Armazém
	 * @param carregadores número de Carregadores alocados
	 * @param eficienciaMedia eficiência média dos Carregadores (1,0 = 100%)
	 */
	record ArmazemAtivo(NivelConstrucao nivel, int carregadores, BigDecimal eficienciaMedia) {
	}

}
