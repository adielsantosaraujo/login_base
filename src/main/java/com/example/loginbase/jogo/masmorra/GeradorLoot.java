package com.example.loginbase.jogo.masmorra;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.config.Aleatorio;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusItem;

/**
 * Gerador puro de loot de masmorra (design.md §11): dada uma vitória no
 * nível {@code N}, calcula o ganho garantido de recursos e realiza
 * {@code 1 + ceil(N/2)} rolagens, cada uma sorteando uma semente (faixa
 * 0–34), ferro extra (faixa 35–59) ou um item forjável (faixa 60–99), com
 * uma fonte de aleatoriedade injetada ({@link Aleatorio}).
 *
 * <p>
 * Classe sem estado (apenas tabelas estáticas); {@link #gerar(int, Aleatorio)}
 * não muta a entrada nem tem efeitos colaterais.
 */
public class GeradorLoot {

	private static final int NIVEL_MASMORRA_MINIMO = 1;
	private static final int NIVEL_MASMORRA_MAXIMO = 5;

	/**
	 * Nível máximo de item sorteado como loot ({@code min(5, N + d)}). Fixo em 5 e desacoplado de
	 * {@link ModeloItem#NIVEL_MAXIMO} (23, faixa da Forja), para que o loot não mude quando o nível
	 * máximo forjável aumenta.
	 */
	private static final int NIVEL_MAXIMO_ITEM_LOOT = 5;

	private static final int LIMITE_ROLAGEM = 100;
	private static final int LIMITE_SUPERIOR_SEMENTE = 34;
	private static final int LIMITE_SUPERIOR_MATERIAL = 59;

	private static final long FERRO_EXTRA_POR_NIVEL_MILESIMOS = 30_000L;
	private static final long COMIDA_GARANTIDA_POR_NIVEL_MILESIMOS = 40_000L;
	private static final long MADEIRA_GARANTIDA_POR_NIVEL_MILESIMOS = 50_000L;
	private static final long PEDRA_GARANTIDA_POR_NIVEL_MILESIMOS = 50_000L;
	private static final long FERRO_GARANTIDO_POR_NIVEL_MILESIMOS = 20_000L;

	private static final List<ModeloItem> MODELOS_ITEM_ORDEM = List.of(
			ModeloItem.ESPADA,
			ModeloItem.LANCA,
			ModeloItem.ARCO,
			ModeloItem.ARMADURA_COURO,
			ModeloItem.ARMADURA_FERRO);

	/** Cultivos sorteáveis como semente de loot, na ordem de escolha por peso. */
	private static final List<Cultivo> ORDEM_SEMENTES = List.of(
			Cultivo.MILHO,
			Cultivo.BATATA,
			Cultivo.ABOBORA_DOURADA);

	private static final Map<Cultivo, Integer> PESOS_SEMENTES = Map.of(
			Cultivo.MILHO, 60,
			Cultivo.BATATA, 30,
			Cultivo.ABOBORA_DOURADA, 10);

	/**
	 * Gera o loot de uma vitória na masmorra de nível {@code nivelMasmorra}
	 * (1 a 5), consumindo a quantidade de sorteios de {@code aleatorio}
	 * correspondente às regras do nível.
	 */
	public Loot gerar(int nivelMasmorra, Aleatorio aleatorio) {
		if (nivelMasmorra < NIVEL_MASMORRA_MINIMO || nivelMasmorra > NIVEL_MASMORRA_MAXIMO) {
			throw new IllegalArgumentException("Nível de masmorra inválido: " + nivelMasmorra);
		}
		Objects.requireNonNull(aleatorio, "aleatorio");

		Map<TipoRecurso, Long> recursos = recursosGarantidos(nivelMasmorra);
		Map<Cultivo, Integer> sementes = sementesZeradas();
		List<Item> itens = new ArrayList<>();

		int rolagens = quantidadeDeRolagens(nivelMasmorra);
		for (int i = 0; i < rolagens; i++) {
			int d = aleatorio.proximoInt(LIMITE_ROLAGEM);
			if (d <= LIMITE_SUPERIOR_SEMENTE) {
				Cultivo sorteada = sortearSemente(nivelMasmorra, aleatorio);
				sementes.merge(sorteada, 1, Integer::sum);
			} else if (d <= LIMITE_SUPERIOR_MATERIAL) {
				recursos.merge(TipoRecurso.FERRO, FERRO_EXTRA_POR_NIVEL_MILESIMOS * nivelMasmorra, Long::sum);
			} else {
				itens.add(sortearItem(nivelMasmorra, aleatorio));
			}
		}

		return new Loot(recursos, sementes, itens);
	}

	/** {@code 1 + ceil(N/2)}, tabela: N1=2, N2=2, N3=3, N4=3, N5=4. */
	private static int quantidadeDeRolagens(int nivelMasmorra) {
		return 1 + ((nivelMasmorra + 1) / 2);
	}

	private static Map<TipoRecurso, Long> recursosGarantidos(int nivelMasmorra) {
		Map<TipoRecurso, Long> recursos = new EnumMap<>(TipoRecurso.class);
		recursos.put(TipoRecurso.COMIDA, COMIDA_GARANTIDA_POR_NIVEL_MILESIMOS * nivelMasmorra);
		recursos.put(TipoRecurso.MADEIRA, MADEIRA_GARANTIDA_POR_NIVEL_MILESIMOS * nivelMasmorra);
		recursos.put(TipoRecurso.PEDRA, PEDRA_GARANTIDA_POR_NIVEL_MILESIMOS * nivelMasmorra);
		recursos.put(TipoRecurso.FERRO, FERRO_GARANTIDO_POR_NIVEL_MILESIMOS * nivelMasmorra);
		return recursos;
	}

	private static Map<Cultivo, Integer> sementesZeradas() {
		Map<Cultivo, Integer> sementes = new EnumMap<>(Cultivo.class);
		for (Cultivo cultivo : ORDEM_SEMENTES) {
			sementes.put(cultivo, 0);
		}
		return sementes;
	}

	private Cultivo sortearSemente(int nivelMasmorra, Aleatorio aleatorio) {
		List<Cultivo> liberadas = new ArrayList<>();
		int somaPesos = 0;
		for (Cultivo cultivo : ORDEM_SEMENTES) {
			if (nivelMasmorra >= cultivo.nivelMasmorraParaSemente()) {
				liberadas.add(cultivo);
				somaPesos += PESOS_SEMENTES.get(cultivo);
			}
		}
		int r = aleatorio.proximoInt(somaPesos);
		int acumulado = 0;
		for (Cultivo cultivo : liberadas) {
			acumulado += PESOS_SEMENTES.get(cultivo);
			if (r < acumulado) {
				return cultivo;
			}
		}
		throw new IllegalStateException("Falha ao sortear semente: r=" + r + ", somaPesos=" + somaPesos);
	}

	private Item sortearItem(int nivelMasmorra, Aleatorio aleatorio) {
		ModeloItem modelo = MODELOS_ITEM_ORDEM.get(aleatorio.proximoInt(MODELOS_ITEM_ORDEM.size()));
		int nivel = Math.min(NIVEL_MAXIMO_ITEM_LOOT, nivelMasmorra + aleatorio.proximoInt(2));

		Item item = new Item();
		item.setModelo(modelo);
		item.setNivel(nivel);
		item.setOrigem(OrigemItem.MASMORRA);
		item.setStatus(StatusItem.DISPONIVEL);
		return item;
	}

}
