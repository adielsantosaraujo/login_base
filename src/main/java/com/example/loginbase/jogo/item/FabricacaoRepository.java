package com.example.loginbase.jogo.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FabricacaoRepository extends JpaRepository<Fabricacao, Long> {

	List<Fabricacao> findByConstrucaoId(Long construcaoId);

	List<Fabricacao> findByVilaId(Long vilaId);

	Optional<Fabricacao> findByArtesaoId(Long artesaoId);

	boolean existsByArtesaoId(Long artesaoId);
}
