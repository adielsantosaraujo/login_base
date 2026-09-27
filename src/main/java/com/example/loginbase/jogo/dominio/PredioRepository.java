package com.example.loginbase.jogo.dominio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PredioRepository extends JpaRepository<Predio, Long> {

	List<Predio> findByVilaId(Long vilaId);

}
