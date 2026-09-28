package com.example.loginbase.jogo.dominio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContadorNomeRepository extends JpaRepository<ContadorNome, Long> {

	Optional<ContadorNome> findByVilaIdAndNomeAndSobrenome(Long vilaId, String nome, String sobrenome);

}
