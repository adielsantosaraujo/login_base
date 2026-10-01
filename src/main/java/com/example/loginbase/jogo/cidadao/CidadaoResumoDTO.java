package com.example.loginbase.jogo.cidadao;

public record CidadaoResumoDTO(Long id, String nome, Sexo sexo, int idadeAnos, Long familiaId, Long construcaoId,
		Profissao profissaoTrabalho, boolean faminto) {

	public static CidadaoResumoDTO de(Cidadao c) {
		return new CidadaoResumoDTO(c.getId(), c.getNome(), c.getSexo(), c.getIdadeAnos(), c.getFamiliaId(),
				c.getConstrucaoId(), c.getProfissaoTrabalho(), c.getFamintoTurnos() > 0);
	}

}
