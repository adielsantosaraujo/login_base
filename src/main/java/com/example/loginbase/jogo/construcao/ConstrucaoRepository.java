package com.example.loginbase.jogo.construcao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstrucaoRepository extends JpaRepository<Construcao, Long> {

	List<Construcao> findByVilaId(Long vilaId);

	List<Construcao> findByVilaIdAndRegiaoIndice(Long vilaId, Integer regiaoIndice);

}
