package com.example.loginbase.jogo.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginbase.jogo.modelo.VilaPrevia;

/** O id é o usuarioId: usar findById, deleteById e existsById. */
public interface VilaPreviaRepository extends JpaRepository<VilaPrevia, Long> {

}
