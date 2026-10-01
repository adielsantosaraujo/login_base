package com.example.loginbase.jogo.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginbase.jogo.modelo.Regiao;

public interface RegiaoRepository extends JpaRepository<Regiao, Long> {

	Optional<Regiao> findByVilaIdAndIndice(Long vilaId, Integer indice);

	List<Regiao> findAllByVilaId(Long vilaId);

}
