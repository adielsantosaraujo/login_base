package com.example.loginbase.jogo.batalha;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatalhaRepository extends JpaRepository<Batalha, Long> {

	List<Batalha> findByVilaIdOrderByTurnoDescIdDesc(Long vilaId);

	Optional<Batalha> findByIdAndVilaId(Long id, Long vilaId);

}
