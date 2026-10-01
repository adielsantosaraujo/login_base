package com.example.loginbase.jogo.construcao;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.recurso.Recurso;

/**
 * Catálogo das fábricas: tipo → receitas (insumos → produtos) e ciclos por trabalhador/turno (tabela 4.5).
 * A ordem de {@link #ORDEM} é a de processamento no passo 1 do turno. A eficiência do trabalhador já inclui o
 * multiplicador do nível. A Fundição escolhe Ferro ou Aço em {@code construcao.configuracao}
 * (chave {@code producao}); padrão Ferro, e Aço só a partir do N2.
 */
public final class CatalogoFabricas {

	public static final String PRODUCAO_FERRO = "FERRO";
	public static final String PRODUCAO_ACO = "ACO";

	/** Receita: insumos consumidos e produtos gerados por ciclo, e ciclos base por trabalhador/turno. */
	public record Receita(Map<Recurso, Integer> insumos, Map<Recurso, Integer> produtos, double ciclosBase) {
	}

	/** Ordem determinística de processamento das fábricas. */
	public static final List<TipoConstrucao> ORDEM = List.of(TipoConstrucao.SERRARIA, TipoConstrucao.OLARIA,
			TipoConstrucao.FUNDICAO, TipoConstrucao.TECELAGEM, TipoConstrucao.CURTUME, TipoConstrucao.COZINHA);

	private CatalogoFabricas() {
	}

	public static boolean ehFabrica(TipoConstrucao tipo) {
		return ORDEM.contains(tipo);
	}

	/**
	 * Receitas do prédio, em ordem de preferência (a Tecelagem usa Fibra primeiro e Lã com os ciclos que
	 * sobrarem). Vazio se o tipo não é fábrica.
	 */
	public static List<Receita> receitas(TipoConstrucao tipo, NivelConstrucao nivel, String configuracao) {
		return switch (tipo) {
			case SERRARIA -> List.of(r(Map.of(Recurso.MADEIRA, 2), Map.of(Recurso.TABUA, 1), 3));
			case OLARIA -> List.of(r(Map.of(Recurso.ARGILA, 2, Recurso.MADEIRA, 1), Map.of(Recurso.TIJOLO, 2), 2));
			case FUNDICAO -> PRODUCAO_ACO.equals(CatalogoPrediosProducao.valor(configuracao, "producao"))
					&& nivel != NivelConstrucao.N1
							? List.of(r(Map.of(Recurso.FERRO, 2, Recurso.CARVAO, 1, Recurso.ENXOFRE, 1),
									Map.of(Recurso.ACO, 1), 1))
							: List.of(r(Map.of(Recurso.MINERIO_DE_FERRO, 2, Recurso.CARVAO, 1),
									Map.of(Recurso.FERRO, 1), 2));
			case TECELAGEM -> List.of(r(Map.of(Recurso.FIBRA, 2), Map.of(Recurso.TECIDO, 1), 2),
					r(Map.of(Recurso.LA, 2), Map.of(Recurso.TECIDO, 1), 2));
			case CURTUME -> List.of(r(Map.of(Recurso.COURO, 2, Recurso.SAL, 1), Map.of(Recurso.COURO_CURTIDO, 1), 2));
			case COZINHA -> List.of(r(Map.of(Recurso.GRAOS, 2, Recurso.CARNE, 1), Map.of(Recurso.REFEICAO, 5), 2));
			default -> List.of();
		};
	}

	private static Receita r(Map<Recurso, Integer> insumos, Map<Recurso, Integer> produtos, double ciclos) {
		return new Receita(insumos, produtos, ciclos);
	}

}
