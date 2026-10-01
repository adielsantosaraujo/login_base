package com.example.loginbase.jogo.cidadao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadaoProfissaoRepository extends JpaRepository<CidadaoProfissao, CidadaoProfissao.Chave> {

	List<CidadaoProfissao> findByCidadaoId(Long cidadaoId);

	Optional<CidadaoProfissao> findByCidadaoIdAndProfissao(Long cidadaoId, Profissao profissao);

}
