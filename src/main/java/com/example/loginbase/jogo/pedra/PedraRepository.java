package com.example.loginbase.jogo.pedra;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedraRepository extends JpaRepository<Pedra, Long> {

	/** Pedras do inventário (não engastadas). */
	List<Pedra> findByVilaIdAndItemIdIsNullOrderByIdAsc(Long vilaId);

	List<Pedra> findByItemId(Long itemId);

	List<Pedra> findByItemIdIn(Collection<Long> itemIds);

	long countByItemId(Long itemId);
}
