package com.example.loginbase.jogo.masmorra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MasmorraRepository extends JpaRepository<Masmorra, Long> {

	List<Masmorra> findByVilaIdAndAtivaTrueOrderByIdAsc(Long vilaId);

	Optional<Masmorra> findByIdAndVilaIdAndAtivaTrue(Long id, Long vilaId);

	Optional<Masmorra> findByVilaIdAndRegiaoIndiceAndAtivaTrue(Long vilaId, int regiaoIndice);

	long countByVilaIdAndAtivaTrue(Long vilaId);

}
