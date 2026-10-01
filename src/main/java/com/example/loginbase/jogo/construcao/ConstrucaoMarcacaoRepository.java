package com.example.loginbase.jogo.construcao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstrucaoMarcacaoRepository extends JpaRepository<ConstrucaoMarcacao, Long> {

	List<ConstrucaoMarcacao> findByConstrucaoId(Long construcaoId);

	List<ConstrucaoMarcacao> findByVilaIdAndRegiaoIndice(Long vilaId, Integer regiaoIndice);

	boolean existsByVilaIdAndRegiaoIndiceAndXAndY(Long vilaId, Integer regiaoIndice, Integer x, Integer y);

}
