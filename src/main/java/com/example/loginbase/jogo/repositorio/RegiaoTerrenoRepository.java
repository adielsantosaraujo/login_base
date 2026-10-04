package com.example.loginbase.jogo.repositorio;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginbase.jogo.modelo.RegiaoTerreno;

public interface RegiaoTerrenoRepository extends JpaRepository<RegiaoTerreno, Long> {

	List<RegiaoTerreno> findByRegiaoIdOrderByPosicao(long regiaoId);

	List<RegiaoTerreno> findByRegiaoIdIn(Collection<Long> regiaoIds);

}
