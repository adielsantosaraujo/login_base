package com.example.loginbase.jogo.construcao;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.recurso.Recurso;

/**
 * Catálogo estático dos prédios: região, profissões, custos, PO e bonificações por nível
 * (construcoes.md, seções 4.3 e 4.4).
 */
public final class ConstrucaoCatalogo {

	/** Dados de um tipo de construção. */
	public record Entrada(
			TipoConstrucao tipo,
			String nome,
			TipoTerreno terreno,
			List<TipoRegiao> regioes,
			boolean coleta,
			List<Profissao> profissoes,
			Map<Recurso, Integer> custoN1,
			int poN1) {
	}

	private static final Map<TipoConstrucao, Entrada> ENTRADAS = new EnumMap<>(TipoConstrucao.class);

	private static final int[] VAGAS = {2, 5, 10};
	private static final int[] VAGAS_INSTRUTOR_QUARTEL = {1, 2, 3};
	private static final int[] CAPACIDADE_QUARTEL = {5, 8, 10};
	private static final int[] MAX_TROPAS_QUARTEL = {1, 2, 4};
	private static final double[] MULTIPLICADOR = {1.0, 1.2, 1.5};
	private static final int[] LADRILHOS_MARCADOS = {4, 10, 20};
	private static final int[] TRABALHADORES_OBRA = {2, 4, 6};
	private static final int[] NUCLEOS_CASA = {1, 2, 4};
	private static final int[] VAGAS_CASA = {4, 10, 24};
	private static final int[] MIN_CARREGADORES_ARMAZEM = {1, 2, 4};

	static {
		def(TipoConstrucao.CASA, "Casa", TipoTerreno.DESENVOLVIMENTO, List.of(), 4, false, m(20, 10, 10, 0, 0, 0, 0));
		def(TipoConstrucao.ARMAZEM, "Armazém", null, List.of(Profissao.CARREGADOR), 6, false, m(30, 20, 0, 0, 0, 0, 0));
		def(TipoConstrucao.SERRARIA, "Serraria", TipoTerreno.INDUSTRIA, List.of(Profissao.MADEIREIRO), 6, false, m(30, 10, 0, 0, 0, 0, 0));
		def(TipoConstrucao.OLARIA, "Olaria", TipoTerreno.INDUSTRIA, List.of(Profissao.CONSTRUTOR), 6, false, m(20, 20, 10, 0, 0, 0, 0));
		def(TipoConstrucao.FUNDICAO, "Fundição", TipoTerreno.INDUSTRIA, List.of(Profissao.FERREIRO), 8, false, m(0, 30, 0, 20, 0, 20, 0));
		def(TipoConstrucao.TECELAGEM, "Tecelagem", TipoTerreno.INDUSTRIA, List.of(Profissao.COSTUREIRO), 6, false, m(0, 10, 0, 20, 0, 0, 0));
		def(TipoConstrucao.CURTUME, "Curtume", TipoTerreno.INDUSTRIA, List.of(Profissao.COSTUREIRO), 6, false, m(0, 0, 0, 20, 0, 10, 0));
		def(TipoConstrucao.COZINHA, "Cozinha", TipoTerreno.INDUSTRIA, List.of(Profissao.COZINHEIRO), 6, false, m(0, 0, 0, 15, 0, 15, 0));
		def(TipoConstrucao.FERRARIA, "Ferraria", null, List.of(Profissao.FERREIRO), 8, false, m(0, 0, 0, 20, 10, 20, 0));
		def(TipoConstrucao.ALFAIATARIA, "Alfaiataria", null, List.of(Profissao.COSTUREIRO), 6, false, m(0, 0, 0, 20, 5, 10, 5));
		def(TipoConstrucao.CARPINTARIA, "Carpintaria", null, List.of(Profissao.MADEIREIRO), 6, false, m(0, 10, 0, 30, 0, 0, 0));
		def(TipoConstrucao.MERCADO, "Mercado", TipoTerreno.COMERCIO, List.of(Profissao.COMERCIANTE), 6, false, m(0, 20, 0, 30, 0, 0, 0));
		def(TipoConstrucao.ESTALAGEM, "Estalagem", TipoTerreno.COMERCIO, List.of(Profissao.COZINHEIRO, Profissao.COMERCIANTE), 8, false, m(0, 0, 0, 30, 0, 20, 10));
		def(TipoConstrucao.QUARTEL, "Quartel", TipoTerreno.MILITAR, List.of(Profissao.GUERREIRO), 8, false, m(0, 40, 0, 30, 10, 0, 0));
		def(TipoConstrucao.FAZENDA_PLANTIO, "Fazenda de plantio", TipoTerreno.PLANTACOES, List.of(Profissao.AGRICULTOR), 4, false, m(15, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.FAZENDA_CRIACAO, "Fazenda de criação", TipoTerreno.CRIACOES, List.of(Profissao.FAZENDEIRO), 4, false, m(25, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.ACAMPAMENTO_LENHADORES, "Acampamento de lenhadores", TipoTerreno.FLORESTA, List.of(Profissao.MADEIREIRO), 4, true, m(15, 5, 0, 0, 0, 0, 0));
		def(TipoConstrucao.PEDREIRA, "Pedreira", TipoTerreno.ROCHA, List.of(Profissao.MINEIRO), 4, true, m(20, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.BARREIRO, "Barreiro", TipoTerreno.BARREIRO, List.of(Profissao.MINEIRO), 4, true, m(15, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.MINA_FERRO, "Mina de ferro", TipoTerreno.FERRO, List.of(Profissao.MINEIRO), 6, true, m(30, 20, 0, 0, 0, 0, 0));
		def(TipoConstrucao.MINA_CARVAO, "Mina de carvão", TipoTerreno.CARVAO, List.of(Profissao.MINEIRO), 6, true, m(30, 20, 0, 0, 0, 0, 0));
		def(TipoConstrucao.SALINA, "Salina", TipoTerreno.SALINAS, List.of(Profissao.MINEIRO), 4, true, m(20, 10, 0, 0, 0, 0, 0));
		def(TipoConstrucao.MINA_ENXOFRE, "Mina de enxofre", TipoTerreno.ENXOFRE, List.of(Profissao.MINEIRO), 8, true, m(30, 30, 0, 0, 5, 0, 0));
		def(TipoConstrucao.CABANA_CACA, "Cabana de caça", TipoTerreno.FLORESTA, List.of(Profissao.CACADOR), 4, true, m(15, 0, 0, 0, 0, 0, 0));
	}

	private ConstrucaoCatalogo() {
	}

	private static Map<Recurso, Integer> m(int madeira, int pedra, int argila, int tabua, int ferro, int tijolo, int tecido) {
		Map<Recurso, Integer> mapa = new EnumMap<>(Recurso.class);
		put(mapa, Recurso.MADEIRA, madeira);
		put(mapa, Recurso.PEDRA, pedra);
		put(mapa, Recurso.ARGILA, argila);
		put(mapa, Recurso.TABUA, tabua);
		put(mapa, Recurso.FERRO, ferro);
		put(mapa, Recurso.TIJOLO, tijolo);
		put(mapa, Recurso.TECIDO, tecido);
		return mapa;
	}

	private static void put(Map<Recurso, Integer> mapa, Recurso r, int q) {
		if (q > 0) {
			mapa.put(r, q);
		}
	}

	private static void def(TipoConstrucao tipo, String nome, TipoTerreno terreno, List<Profissao> profissoes, int po,
			boolean coleta, Map<Recurso, Integer> custo) {
		List<TipoRegiao> regioes;
		if (tipo == TipoConstrucao.QUARTEL) {
			regioes = List.of(TipoRegiao.URBANA, TipoRegiao.LITORAL);
		} else if (terreno == null) {
			regioes = List.of(TipoRegiao.URBANA);
		} else {
			regioes = TipoRegiao.atuais().stream().filter(t -> t.terrenos().contains(terreno)).toList();
		}
		ENTRADAS.put(tipo, new Entrada(tipo, nome, terreno, regioes, coleta, profissoes, Map.copyOf(custo), po));
	}

	private static int idx(NivelConstrucao nivel) {
		return nivel.ordinal();
	}

	public static Entrada de(TipoConstrucao tipo) {
		return ENTRADAS.get(tipo);
	}

	public static List<Entrada> todos() {
		return List.copyOf(ENTRADAS.values());
	}

	/** Terreno associado ao prédio; vazio para prédios sem terreno. */
	public static Optional<TipoTerreno> terreno(TipoConstrucao tipo) {
		return Optional.ofNullable(de(tipo).terreno());
	}

	/** Tipos de região em que o prédio pode ser construído (ordem do enum). */
	public static List<TipoRegiao> regioesPermitidas(TipoConstrucao tipo) {
		return de(tipo).regioes();
	}

	/** Verdadeiro se o tipo pode ser construído na região informada. */
	public static boolean permiteRegiao(TipoConstrucao tipo, TipoRegiao regiao) {
		return regioesPermitidas(tipo).contains(regiao);
	}

	public static List<Profissao> profissoes(TipoConstrucao tipo) {
		return de(tipo).profissoes();
	}

	public static boolean ehPredioDeColeta(TipoConstrucao tipo) {
		return de(tipo).coleta();
	}

	public static Map<Recurso, Integer> custoN1(TipoConstrucao tipo) {
		return de(tipo).custoN1();
	}

	/** Custo para chegar ao nível: N1 base; N2 = 2,5x; N3 = 5x (arredondado para cima). */
	public static Map<Recurso, Integer> custo(TipoConstrucao tipo, NivelConstrucao nivel) {
		Map<Recurso, Integer> resultado = new EnumMap<>(Recurso.class);
		de(tipo).custoN1().forEach((r, q) -> resultado.put(r, switch (nivel) {
			case N1 -> q;
			case N2 -> (int) Math.ceil(q * 2.5);
			case N3 -> q * 5;
		}));
		return resultado;
	}

	/** PO total do nível: N1 base; N2 = 2,5x; N3 = 5x (arredondado para cima). */
	public static int po(TipoConstrucao tipo, NivelConstrucao nivel) {
		int base = de(tipo).poN1();
		return switch (nivel) {
			case N1 -> base;
			case N2 -> (int) Math.ceil(base * 2.5);
			case N3 -> base * 5;
		};
	}

	public static int tamanho(NivelConstrucao nivel) {
		return idx(nivel) + 1;
	}

	/** Vagas de trabalho (2/5/10). */
	public static int vagas(NivelConstrucao nivel) {
		return VAGAS[idx(nivel)];
	}

	/** Vagas de instrutor (Guerreiro) do Quartel (1/2/3). */
	public static int vagasInstrutorQuartel(NivelConstrucao nivel) {
		return VAGAS_INSTRUTOR_QUARTEL[idx(nivel)];
	}

	/** Capacidade total de membros de tropa por Quartel (5/8/10). */
	public static int capacidadeQuartel(NivelConstrucao nivel) {
		return CAPACIDADE_QUARTEL[idx(nivel)];
	}

	/** Máximo de tropas simultâneas por Quartel (1/2/4). */
	public static int maxTropasQuartel(NivelConstrucao nivel) {
		return MAX_TROPAS_QUARTEL[idx(nivel)];
	}

	/** Multiplicador de produção (1,0/1,2/1,5). */
	public static double multiplicador(NivelConstrucao nivel) {
		return MULTIPLICADOR[idx(nivel)];
	}

	/** Máximo de ladrilhos marcados em prédio de coleta (4/10/20). */
	public static int maxLadrilhosMarcados(NivelConstrucao nivel) {
		return LADRILHOS_MARCADOS[idx(nivel)];
	}

	/** Máximo de trabalhadores na obra (2/4/6). */
	public static int maxTrabalhadoresObra(NivelConstrucao nivel) {
		return TRABALHADORES_OBRA[idx(nivel)];
	}

	/** Núcleos familiares da Casa (1/2/4). */
	public static int nucleosCasa(NivelConstrucao nivel) {
		return NUCLEOS_CASA[idx(nivel)];
	}

	/** Vagas de moradores da Casa (4/10/24). */
	public static int vagasCasa(NivelConstrucao nivel) {
		return VAGAS_CASA[idx(nivel)];
	}

	/** Regra separada do Armazém: mínimo de Carregadores (1/2/4). */
	public static int minCarregadoresArmazem(NivelConstrucao nivel) {
		return MIN_CARREGADORES_ARMAZEM[idx(nivel)];
	}
}
