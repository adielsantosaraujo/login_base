package com.example.loginbase.jogo.dominio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueSementeRepository extends JpaRepository<EstoqueSemente, Long> {

	List<EstoqueSemente> findByVilaId(Long vilaId);

}
