package com.example.loginbase.jogo.item.catalogo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemCatalogado;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.RegrasFabricacao;
import com.example.loginbase.jogo.recurso.Recurso;

/** Catálogo de armas (tabela 7.8). Dados puros, sem validação. */
public enum ArmaCatalogo implements ItemCatalogado {

	ESPADA(ItemSubtipo.ESPADA, TipoConstrucao.FERRARIA, Map.of(Recurso.FERRO, 3, Recurso.TABUA, 1),
			10, List.of(Caracteristica.FOR), 0, Alcance.CORPO_A_CORPO_FRENTE, "+2 Defesa ao portador"),
	LANCA(ItemSubtipo.LANCA, TipoConstrucao.FERRARIA, Map.of(Recurso.FERRO, 2, Recurso.TABUA, 2),
			9, List.of(Caracteristica.FOR, Caracteristica.VEL), 1, Alcance.CORPO_A_CORPO_FLEX, null),
	ARCO(ItemSubtipo.ARCO, TipoConstrucao.CARPINTARIA, Map.of(Recurso.TABUA, 3, Recurso.TECIDO, 1),
			8, List.of(Caracteristica.VEL), 2, Alcance.DISTANCIA, null),
	BESTA(ItemSubtipo.BESTA, TipoConstrucao.CARPINTARIA,
			Map.of(Recurso.FERRO, 2, Recurso.TABUA, 3, Recurso.TECIDO, 1),
			13, List.of(Caracteristica.INT), -4, Alcance.DISTANCIA, "Ignora 25% da defesa do alvo");

	private final ItemSubtipo subtipo;
	private final TipoConstrucao oficina;
	private final Map<Recurso, Integer> receitaBase;
	private final int ataqueBase;
	private final List<Caracteristica> atributosChave;
	private final int modificadorIniciativa;
	private final Alcance alcance;
	private final String especial;

	ArmaCatalogo(ItemSubtipo subtipo, TipoConstrucao oficina, Map<Recurso, Integer> receitaBase, int ataqueBase,
			List<Caracteristica> atributosChave, int modificadorIniciativa, Alcance alcance, String especial) {
		this.subtipo = subtipo;
		this.oficina = oficina;
		this.receitaBase = receitaBase;
		this.ataqueBase = ataqueBase;
		this.atributosChave = atributosChave;
		this.modificadorIniciativa = modificadorIniciativa;
		this.alcance = alcance;
		this.especial = especial;
	}

	@Override
	public ItemSubtipo subtipo() {
		return subtipo;
	}

	@Override
	public TipoConstrucao oficina() {
		return oficina;
	}

	@Override
	public Map<Recurso, Integer> receitaBase() {
		return receitaBase;
	}

	public int ataqueBase() {
		return ataqueBase;
	}

	/** Atributo-chave; com mais de um (Lança: FOR e VEL), vale a média entre eles. */
	public List<Caracteristica> atributosChave() {
		return atributosChave;
	}

	public int modificadorIniciativa() {
		return modificadorIniciativa;
	}

	public Alcance alcance() {
		return alcance;
	}

	/** Texto do especial, ou vazio se a arma não tem. */
	public Optional<String> especial() {
		return Optional.ofNullable(especial);
	}

	/** Ataque da arma no nível: ataque base x multiplicador de atributo do nível. */
	public double ataque(int nivel) {
		return ataqueBase * RegrasFabricacao.multiplicadorAtributo(nivel);
	}

	public static Optional<ArmaCatalogo> de(ItemSubtipo subtipo) {
		return Arrays.stream(values()).filter(a -> a.subtipo == subtipo).findFirst();
	}
}
