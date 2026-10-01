package com.example.loginbase.jogo.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginbase.jogo.modelo.LadrilhoJazida;

public interface LadrilhoJazidaRepository extends JpaRepository<LadrilhoJazida, LadrilhoJazida.Chave> {

	List<LadrilhoJazida> findAllByRegiaoId(Long regiaoId);

	void deleteAllByRegiaoId(Long regiaoId);

}
