package com.example.loginbase.jogo.construcao;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Jazida;
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
			BonusRegiao bonusRegiao,
			List<Profissao> profissoes,
			Map<Recurso, Integer> custoN1,
			int poN1,
			Jazida jazidaAssociada) {
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
		def(TipoConstrucao.CASA, "Casa", null, List.of(), 4, null, m(20, 10, 10, 0, 0, 0, 0));
		def(TipoConstrucao.ARMAZEM, "Armazém", null, List.of(Profissao.CARREGADOR), 6, null, m(30, 20, 0, 0, 0, 0, 0));
		def(TipoConstrucao.SERRARIA, "Serraria", BonusRegiao.INDUSTRIA, List.of(Profissao.MADEIREIRO), 6, null, m(30, 10, 0, 0, 0, 0, 0));
		def(TipoConstrucao.OLARIA, "Olaria", BonusRegiao.INDUSTRIA, List.of(Profissao.CONSTRUTOR), 6, null, m(20, 20, 10, 0, 0, 0, 0));
		def(TipoConstrucao.FUNDICAO, "Fundição", BonusRegiao.INDUSTRIA, List.of(Profissao.FERREIRO), 8, null, m(0, 30, 0, 20, 0, 20, 0));
		def(TipoConstrucao.TECELAGEM, "Tecelagem", BonusRegiao.INDUSTRIA, List.of(Profissao.COSTUREIRO), 6, null, m(0, 10, 0, 20, 0, 0, 0));
		def(TipoConstrucao.CURTUME, "Curtume", BonusRegiao.INDUSTRIA, List.of(Profissao.COSTUREIRO), 6, null, m(0, 0, 0, 20, 0, 10, 0));
		def(TipoConstrucao.COZINHA, "Cozinha", BonusRegiao.INDUSTRIA, List.of(Profissao.COZINHEIRO), 6, null, m(0, 0, 0, 15, 0, 15, 0));
		def(TipoConstrucao.FERRARIA, "Ferraria", null, List.of(Profissao.FERREIRO), 8, null, m(0, 0, 0, 20, 10, 20, 0));
		def(TipoConstrucao.ALFAIATARIA, "Alfaiataria", null, List.of(Profissao.COSTUREIRO), 6, null, m(0, 0, 0, 20, 5, 10, 5));
		def(TipoConstrucao.CARPINTARIA, "Carpintaria", null, List.of(Profissao.MADEIREIRO), 6, null, m(0, 10, 0, 30, 0, 0, 0));
		def(TipoConstrucao.MERCADO, "Mercado", null, List.of(Profissao.COMERCIANTE), 6, null, m(0, 20, 0, 30, 0, 0, 0));
		def(TipoConstrucao.ESTALAGEM, "Estalagem", null, List.of(Profissao.COZINHEIRO, Profissao.COMERCIANTE), 8, null, m(0, 0, 0, 30, 0, 20, 10));
		def(TipoConstrucao.QUARTEL, "Quartel", null, List.of(Profissao.GUERREIRO), 8, null, m(0, 40, 0, 30, 10, 0, 0));
		def(TipoConstrucao.FAZENDA_PLANTIO, "Fazenda de plantio", BonusRegiao.PLANTACOES, List.of(Profissao.AGRICULTOR), 4, null, m(15, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.FAZENDA_CRIACAO, "Fazenda de criação", BonusRegiao.CRIACOES, List.of(Profissao.FAZENDEIRO), 4, null, m(25, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.ACAMPAMENTO_LENHADORES, "Acampamento de lenhadores", BonusRegiao.FLORESTA, List.of(Profissao.MADEIREIRO), 4, Jazida.FLORESTA, m(15, 5, 0, 0, 0, 0, 0));
		def(TipoConstrucao.PEDREIRA, "Pedreira", BonusRegiao.ROCHA, List.of(Profissao.MINEIRO), 4, Jazida.ROCHA, m(20, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.BARREIRO, "Barreiro", BonusRegiao.BARREIRO, List.of(Profissao.MINEIRO), 4, Jazida.BARREIRO, m(15, 0, 0, 0, 0, 0, 0));
		def(TipoConstrucao.MINA_FERRO, "Mina de ferro", BonusRegiao.FERRO, List.of(Profissao.MINEIRO), 6, Jazida.VEIO_DE_FERRO, m(30, 20, 0, 0, 0, 0, 0));
		def(TipoConstrucao.MINA_CARVAO, "Mina de carvão", BonusRegiao.CARVAO, List.of(Profissao.MINEIRO), 6, Jazida.VEIO_DE_CARVAO, m(30, 20, 0, 0, 0, 0, 0));
		def(TipoConstrucao.SALINA, "Salina", BonusRegiao.SALINAS, List.of(Profissao.MINEIRO), 4, Jazida.SALINA, m(20, 10, 0, 0, 0, 0, 0));
		def(TipoConstrucao.MINA_ENXOFRE, "Mina de enxofre", BonusRegiao.ENXOFRE, List.of(Profissao.MINEIRO), 8, Jazida.ENXOFRE, m(30, 30, 0, 0, 5, 0, 0));
		def(TipoConstrucao.CABANA_CACA, "Cabana de caça", BonusRegiao.FLORESTA, List.of(Profissao.CACADOR), 4, Jazida.FLORESTA, m(15, 0, 0, 0, 0, 0, 0));
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

	private static void def(TipoConstrucao tipo, String nome, BonusRegiao bonus, List<Profissao> profissoes, int po,
			Jazida jazida, Map<Recurso, Integer> custo) {
		ENTRADAS.put(tipo, new Entrada(tipo, nome, bonus, profissoes, Map.copyOf(custo), po, jazida));
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

	/** Bônus de região exigido pelo prédio; vazio para prédios urbanos. */
	public static Optional<BonusRegiao> bonusRegiao(TipoConstrucao tipo) {
		return Optional.ofNullable(de(tipo).bonusRegiao());
	}

	/** Tipos de região em que o prédio pode ser construído (ordem do enum). */
	public static List<TipoRegiao> regioesPermitidas(TipoConstrucao tipo) {
		BonusRegiao bonus = de(tipo).bonusRegiao();
		if (bonus == null) {
			return List.of(TipoRegiao.URBANA);
		}
		return TipoRegiao.atuais().stream().filter(t -> t.bonus().contains(bonus)).toList();
	}

	/** Verdadeiro se o tipo pode ser construído na região informada. */
	public static boolean permiteRegiao(TipoConstrucao tipo, TipoRegiao regiao) {
		return regioesPermitidas(tipo).contains(regiao);
	}

	public static List<Profissao> profissoes(TipoConstrucao tipo) {
		return de(tipo).profissoes();
	}

	/** Jazida associada (apenas prédios de coleta). */
	public static Optional<Jazida> jazida(TipoConstrucao tipo) {
		return Optional.ofNullable(de(tipo).jazidaAssociada());
	}

	public static boolean ehPredioDeColeta(TipoConstrucao tipo) {
		return de(tipo).jazidaAssociada() != null;
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
