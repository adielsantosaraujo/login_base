package com.example.loginbase.jogo.item.catalogo;

import java.util.Map;
import java.util.Optional;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemCatalogado;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.RegrasFabricacao;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.recurso.Recurso;

/** Catálogo das 6 peças de armadura (tabela 7.10). */
public enum ArmaduraCatalogo implements ItemCatalogado {
	PEITORAL(ItemSubtipo.PEITORAL, TipoConstrucao.FERRARIA, 8, 0, Map.of(Recurso.FERRO, 5)),
	CAPACETE(ItemSubtipo.CAPACETE, TipoConstrucao.FERRARIA, 4, 0, Map.of(Recurso.FERRO, 3)),
	OMBREIRAS(ItemSubtipo.OMBREIRAS, TipoConstrucao.FERRARIA, 3, 0, Map.of(Recurso.FERRO, 3)),
	LUVAS(ItemSubtipo.LUVAS, TipoConstrucao.ALFAIATARIA, 2, 0, Map.of(Recurso.COURO_CURTIDO, 2)),
	CALCAS(ItemSubtipo.CALCAS, TipoConstrucao.ALFAIATARIA, 5, 0,
			Map.of(Recurso.COURO_CURTIDO, 3, Recurso.TECIDO, 1)),
	SAPATO(ItemSubtipo.SAPATO, TipoConstrucao.ALFAIATARIA, 2, 1, Map.of(Recurso.COURO_CURTIDO, 2));

	private final ItemSubtipo subtipo;
	private final TipoConstrucao oficina;
	private final int defesaBase;
	private final int iniciativaExtra;
	private final Map<Recurso, Integer> receitaBase;

	ArmaduraCatalogo(ItemSubtipo subtipo, TipoConstrucao oficina, int defesaBase, int iniciativaExtra,
			Map<Recurso, Integer> receitaBase) {
		this.subtipo = subtipo;
		this.oficina = oficina;
		this.defesaBase = defesaBase;
		this.iniciativaExtra = iniciativaExtra;
		this.receitaBase = receitaBase;
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

	public int defesaBase() {
		return defesaBase;
	}

	public int iniciativaExtra() {
		return iniciativaExtra;
	}

	public SlotEquipamento slot() {
		return SlotEquipamento.valueOf(subtipo.name());
	}

	/** Defesa no nível L: base x (1 + 0,2 x (L - 1)). */
	public double defesa(int nivel) {
		return defesaBase * RegrasFabricacao.multiplicadorAtributo(nivel);
	}

	public int pf(int nivel) {
		return RegrasFabricacao.pf(nivel);
	}

	public int nivelMaximo(NivelConstrucao nivelOficina) {
		return RegrasFabricacao.nivelMaximo(nivelOficina);
	}

	public static Optional<ArmaduraCatalogo> de(ItemSubtipo subtipo) {
		for (ArmaduraCatalogo a : values()) {
			if (a.subtipo == subtipo) {
				return Optional.of(a);
			}
		}
		return Optional.empty();
	}
}
