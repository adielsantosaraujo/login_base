package com.example.loginbase.jogo.item;

import java.util.Map;

public record ReceitaDTO(String subtipo, String nome, Map<String, Integer> receitaBase,
		Map<Integer, Map<String, Integer>> custoPorNivel, Map<Integer, Integer> peMinimoPorNivel,
		boolean exigeAtributo) {
}
