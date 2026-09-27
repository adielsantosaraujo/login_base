package com.example.loginbase.jogo.dominio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemRepository extends JpaRepository<Ordem, Long> {

	List<Ordem> findByVilaId(Long vilaId);

}
