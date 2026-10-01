package com.example.loginbase.jogo.cidadao;

import java.util.List;

/** Listas fixas de nomes usadas na geração das famílias iniciais. */
public final class NomesFixos {

	public static final List<String> SOBRENOMES = List.of("Silva", "Santos", "Oliveira", "Pereira", "Souza",
			"Costa", "Rodrigues", "Almeida", "Nascimento", "Lima", "Araújo", "Ribeiro");

	public static final List<String> MASCULINOS = List.of("João", "Pedro", "Paulo", "Carlos", "Lucas", "Mateus",
			"Rafael", "Tiago", "Bruno", "André", "Felipe", "Gabriel", "Daniel", "Marcos", "Henrique", "Vitor");

	public static final List<String> FEMININOS = List.of("Maria", "Ana", "Paula", "Carolina", "Beatriz", "Júlia",
			"Laura", "Camila", "Fernanda", "Helena", "Luiza", "Renata", "Sofia", "Clara", "Isabela", "Marta");

	private NomesFixos() {
	}

}
