package com.example.loginbase.jogo.dominio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CanteiroRepository extends JpaRepository<Canteiro, Long> {

	List<Canteiro> findByVilaId(Long vilaId);

}
