package com.example.loginbase.jogo.cidadao;

import java.util.List;
import java.util.Map;

/** Dados completos do cidadão (painel). Equipamento ainda vazio (Fase 3/4). */
public record CidadaoDTO(
		Long id,
		String nome,
		Sexo sexo,
		int idadeAnos,
		boolean vivo,
		EstadoCidadao estado,
		boolean faminto,
		Long familiaId,
		String familiaNome,
		Conjuge conjuge,
		Map<String, Integer> caracteristicas,
		int pontosCarPendentes,
		int pontosProfPendentes,
		List<ProfissaoDTO> profissoes,
		Long construcaoId,
		Profissao profissaoTrabalho,
		Map<String, Object> equipamento) {

	public record Conjuge(Long id, String nome) {
	}

	public record ProfissaoDTO(Profissao profissao, int peBase, int peEfetivo, double eficiencia) {
	}

}
