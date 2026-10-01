package com.example.loginbase.jogo.construcao;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.loginbase.jogo.recurso.Recurso;

/**
 * Catálogo de produção dos prédios de coleta e rurais: tipo → recursos produzidos por trabalhador/turno
 * (N1, eficiência 1,0; tabela 4.5). O multiplicador do nível já está na eficiência do trabalhador.
 * Fazendas escolhem cultura/rebanho em {@code construcao.configuracao} (JSON, chaves {@code cultura} e
 * {@code rebanho}); padrão Grãos e Gado.
 */
public final class CatalogoPrediosProducao {

	public static final String CULTURA_GRAOS = "GRAOS";
	public static final String CULTURA_FIBRA = "FIBRA";
	public static final String REBANHO_GADO = "GADO";
	public static final String REBANHO_OVELHAS = "OVELHAS";

	/** Quantidade de um recurso por trabalhador/turno. */
	public record Saida(Recurso recurso, double base) {
	}

	private static final Map<TipoConstrucao, List<Saida>> FIXOS = new EnumMap<>(TipoConstrucao.class);

	static {
		FIXOS.put(TipoConstrucao.ACAMPAMENTO_LENHADORES, List.of(new Saida(Recurso.MADEIRA, 5)));
		FIXOS.put(TipoConstrucao.PEDREIRA, List.of(new Saida(Recurso.PEDRA, 4)));
		FIXOS.put(TipoConstrucao.BARREIRO, List.of(new Saida(Recurso.ARGILA, 4)));
		FIXOS.put(TipoConstrucao.MINA_FERRO, List.of(new Saida(Recurso.MINERIO_DE_FERRO, 3)));
		FIXOS.put(TipoConstrucao.MINA_CARVAO, List.of(new Saida(Recurso.CARVAO, 3)));
		FIXOS.put(TipoConstrucao.SALINA, List.of(new Saida(Recurso.SAL, 3)));
		FIXOS.put(TipoConstrucao.MINA_ENXOFRE, List.of(new Saida(Recurso.ENXOFRE, 2)));
		FIXOS.put(TipoConstrucao.CABANA_CACA, List.of(new Saida(Recurso.CARNE, 2), new Saida(Recurso.COURO, 1)));
	}

	private CatalogoPrediosProducao() {
	}

	public static boolean ehColeta(TipoConstrucao tipo) {
		return FIXOS.containsKey(tipo);
	}

	public static boolean ehRural(TipoConstrucao tipo) {
		return tipo == TipoConstrucao.FAZENDA_PLANTIO || tipo == TipoConstrucao.FAZENDA_CRIACAO;
	}

	public static boolean produz(TipoConstrucao tipo) {
		return ehColeta(tipo) || ehRural(tipo);
	}

	/** Saídas por trabalhador/turno do prédio (vazio se o tipo não é de coleta/rural). */
	public static List<Saida> saidas(TipoConstrucao tipo, String configuracao) {
		if (tipo == TipoConstrucao.FAZENDA_PLANTIO) {
			return CULTURA_FIBRA.equals(valor(configuracao, "cultura")) ? List.of(new Saida(Recurso.FIBRA, 4))
					: List.of(new Saida(Recurso.GRAOS, 6));
		}
		if (tipo == TipoConstrucao.FAZENDA_CRIACAO) {
			return REBANHO_OVELHAS.equals(valor(configuracao, "rebanho"))
					? List.of(new Saida(Recurso.LA, 2), new Saida(Recurso.CARNE, 1))
					: List.of(new Saida(Recurso.CARNE, 3), new Saida(Recurso.COURO, 1));
		}
		return FIXOS.getOrDefault(tipo, List.of());
	}

	/** Lê um valor de texto simples de um JSON plano (ex.: {"cultura":"FIBRA"}); maiúsculo ou null. */
	public static String valor(String json, String chave) {
		if (json == null) {
			return null;
		}
		Matcher m = Pattern.compile("\"" + Pattern.quote(chave) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
		return m.find() ? m.group(1).trim().toUpperCase() : null;
	}

}
