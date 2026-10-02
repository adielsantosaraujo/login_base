package com.example.loginbase.jogo.quartel;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.item.ConsultaTropas;

import lombok.RequiredArgsConstructor;

/** Cidadão em expedição = membro de tropa EM_VIAGEM_IDA ou EM_VIAGEM_VOLTA. */
@Component
@RequiredArgsConstructor
public class ConsultaTropasJpa implements ConsultaTropas {

	private final TropaRepository tropaRepository;

	@Override
	@Transactional(readOnly = true)
	public boolean emExpedicao(Long cidadaoId) {
		return cidadaoId != null && tropaRepository.cidadaoEmViagem(cidadaoId);
	}
}
