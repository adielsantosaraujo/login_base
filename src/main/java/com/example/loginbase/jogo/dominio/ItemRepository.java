package com.example.loginbase.jogo.dominio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginbase.jogo.catalogo.ModeloItem;

public interface ItemRepository extends JpaRepository<Item, Long> {

	List<Item> findByVilaId(Long vilaId);

	List<Item> findByOrdemId(Long ordemId);

	List<Item> findByOrdemIdOrderById(Long ordemId);

	/** Itens de uma vila, do modelo/nível/status informados, ordenados por id (menor primeiro). */
	List<Item> findByVilaIdAndModeloAndNivelAndStatusOrderByIdAsc(Long vilaId, ModeloItem modelo, int nivel,
			StatusItem status);

}
