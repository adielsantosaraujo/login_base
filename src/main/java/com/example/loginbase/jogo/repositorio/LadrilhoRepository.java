package com.example.loginbase.jogo.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.TipoTerreno;

public interface LadrilhoRepository extends JpaRepository<Ladrilho, Long> {

	List<Ladrilho> findByRegiaoIdOrderByYAscXAsc(long regiaoId);

	Optional<Ladrilho> findByRegiaoIdAndXAndY(long regiaoId, int x, int y);

	List<Ladrilho> findByRegiaoIdAndTerrenoOrderByYAscXAsc(long regiaoId, TipoTerreno terreno);

	boolean existsByRegiaoId(long regiaoId);

}
