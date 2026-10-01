package com.example.loginbase.jogo.cidadao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FamiliaRepository extends JpaRepository<Familia, Long> {

	List<Familia> findByVilaId(Long vilaId);

}
