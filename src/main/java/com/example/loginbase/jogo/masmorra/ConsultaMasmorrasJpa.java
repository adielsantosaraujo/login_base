package com.example.loginbase.jogo.masmorra;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.ConsultaMasmorras;

/** Consulta o nível da masmorra ativa de uma região via JPA. */
@Component
public class ConsultaMasmorrasJpa implements ConsultaMasmorras {

	private final MasmorraRepository repository;

	public ConsultaMasmorrasJpa(MasmorraRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Integer> nivelMasmorraAtiva(Vila vila, int indice) {
		return repository.findByVilaIdAndRegiaoIndiceAndAtivaTrue(vila.getId(), indice).map(Masmorra::getNivel);
	}

}
