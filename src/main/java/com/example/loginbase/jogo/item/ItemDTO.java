package com.example.loginbase.jogo.item;

import com.example.loginbase.jogo.item.catalogo.ArmaCatalogo;
import com.example.loginbase.jogo.item.catalogo.ArmaduraCatalogo;
import com.example.loginbase.jogo.item.catalogo.JoiaCatalogo;

import java.util.List;

public record ItemDTO(Long id, ItemCategoria categoria, ItemSubtipo subtipo, String nome, int nivel,
		Qualidade qualidade, List<BonusItem> bonus, CodigoBonus atributoEscolhido, SlotEquipamento slot,
		Long cidadaoId, boolean emAprimoramento, AtributoPrincipal atributoPrincipal) {

	public record AtributoPrincipal(String tipo, double valor) {
	}

	public static ItemDTO de(Item i) {
		return new ItemDTO(i.getId(), i.getCategoria(), i.getSubtipo(), i.getSubtipo().getNomeExibicao(),
				i.getNivel(), i.getQualidade(), List.copyOf(i.getBonus()), i.getAtributoEscolhido(), i.getSlot(),
				i.getCidadaoId(), i.isEmAprimoramento(), principal(i));
	}

	private static AtributoPrincipal principal(Item i) {
		int l = i.getNivel();
		return switch (i.getCategoria()) {
			case ARMA -> ArmaCatalogo.de(i.getSubtipo()).map(a -> new AtributoPrincipal("ATAQUE", a.ataque(l)))
					.orElse(null);
			case ARMADURA -> ArmaduraCatalogo.de(i.getSubtipo())
					.map(a -> new AtributoPrincipal("DEFESA", a.defesa(l))).orElse(null);
			case FERRAMENTA -> new AtributoPrincipal("PE", l);
			case JOIA -> i.getSubtipo() == ItemSubtipo.COLAR
					? new AtributoPrincipal("VIDA", JoiaCatalogo.vidaColar(l))
					: i.getAtributoEscolhido() == null ? null
							: new AtributoPrincipal(i.getAtributoEscolhido().name(), JoiaCatalogo.bonusAnel(l));
		};
	}
}
