package com.example.loginbase.jogo.item;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.catalogo.ArmaCatalogo;
import com.example.loginbase.jogo.item.catalogo.ArmaduraCatalogo;
import com.example.loginbase.jogo.item.catalogo.FerramentaCatalogo;
import com.example.loginbase.jogo.item.catalogo.JoiaCatalogo;

/** Resolve {@link ItemSubtipo} para o {@link ItemCatalogado} correspondente. */
@Component
public class CatalogoItens {

	public Optional<? extends ItemCatalogado> de(ItemSubtipo subtipo) {
		return switch (subtipo.getCategoria()) {
			case ARMA -> ArmaCatalogo.de(subtipo);
			case FERRAMENTA -> FerramentaCatalogo.de(subtipo);
			case ARMADURA -> ArmaduraCatalogo.de(subtipo);
			case JOIA -> JoiaCatalogo.de(subtipo);
		};
	}

	public List<ItemCatalogado> subtiposDa(TipoConstrucao oficina) {
		return java.util.Arrays.stream(ItemSubtipo.values())
				.map(s -> (ItemCatalogado) de(s).orElse(null))
				.filter(i -> i != null && i.oficina() == oficina).toList();
	}
}
