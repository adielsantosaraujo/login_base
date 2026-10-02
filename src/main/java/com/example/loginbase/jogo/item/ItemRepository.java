package com.example.loginbase.jogo.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	List<Item> findByVilaId(Long vilaId);

	List<Item> findByCidadaoId(Long cidadaoId);

	Optional<Item> findByCidadaoIdAndSlot(Long cidadaoId, SlotEquipamento slot);

	Page<Item> findByVilaIdAndCidadaoIdIsNull(Long vilaId, Pageable pageable);

	Page<Item> findByVilaIdAndCidadaoIdIsNullAndCategoria(Long vilaId, ItemCategoria categoria, Pageable pageable);
}
