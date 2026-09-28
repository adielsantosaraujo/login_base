package com.example.loginbase.jogo.catalogo;

/**
 * Slot de equipamento de uma {@code Unidade}: define os 9 slots exibidos na
 * tela de detalhe, em ordem de exibição, com o rótulo e a categoria de item
 * aceita em cada um (nula para os slots futuros, ainda sem persistência).
 * <p>
 * Sem persistência própria: ARMA e ARMADURA derivam de
 * {@code Unidade.armaItemId}/{@code armaduraItemId}; os demais permanecem
 * sempre vazios até extensão futura.
 */
public enum SlotEquipamento {

	ARMA("Arma", CategoriaItem.ARMA),
	ARMADURA("Armadura", CategoriaItem.ARMADURA),
	CABECA("Capacete/chapéu", null),
	BOTA("Bota", null),
	LUVA("Luva", null),
	COLAR("Colar", null),
	ANEL_1("Anel 1", null),
	ANEL_2("Anel 2", null),
	ANEL_3("Anel 3", null);

	private final String rotulo;
	private final CategoriaItem categoriaAceita;

	SlotEquipamento(String rotulo, CategoriaItem categoriaAceita) {
		this.rotulo = rotulo;
		this.categoriaAceita = categoriaAceita;
	}

	public String getRotulo() {
		return rotulo;
	}

	public CategoriaItem getCategoriaAceita() {
		return categoriaAceita;
	}

	/**
	 * Indica se este slot aceita a categoria informada. Slots sem categoria
	 * aceita (futuros) nunca aceitam nada.
	 */
	public boolean aceita(CategoriaItem categoria) {
		return categoriaAceita != null && categoriaAceita.equals(categoria);
	}

}
