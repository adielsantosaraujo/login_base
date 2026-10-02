package com.example.loginbase.jogo.batalha;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.item.catalogo.ArmaCatalogo;

/** Fórmulas da seção 10.1 de batalha.md. Serviço puro, sem estado. */
@Component
public class CombateAtributos {

	public AtributosCombate calcular(EntradaCombate e) {
		int pv = 30 + 5 * e.vit() + 3 * e.g() + e.somaVida();

		double atqArma = e.arma() == null ? 0 : e.arma().ataque(e.nivelArma());
		double ataque = (atqArma * (1 + 0.05 * atributoChave(e)) + 2 * e.g()) * (1 + e.somaAtkPct() / 100.0);

		int bonusEspada = e.arma() == ArmaCatalogo.ESPADA ? 2 : 0;
		double defesa = (e.somaDefesaPecas() + e.vit() + e.g() + bonusEspada) * (1 + e.somaDefPct() / 100.0);

		int modArma = e.arma() == null ? 0 : e.arma().modificadorIniciativa();
		int iniciativa = 2 * e.vel() + e.g() + modArma + e.somaIni() + e.iniciativaArmaduras();

		double critico = 5 + 0.5 * e.vel() + e.somaCrit();
		return new AtributosCombate(pv, ataque, defesa, iniciativa, critico);
	}

	/** Atributo-chave da arma; com mais de um (Lança), vale a média. */
	private double atributoChave(EntradaCombate e) {
		if (e.arma() == null) {
			return 0;
		}
		double soma = 0;
		for (Caracteristica c : e.arma().atributosChave()) {
			soma += switch (c) {
				case FOR -> e.forca();
				case VEL -> e.vel();
				case INT -> e.inteligencia();
				case VIT -> e.vit();
				case CAR -> 0;
			};
		}
		return soma / e.arma().atributosChave().size();
	}
}
