package com.example.loginbase.acesso;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

	Optional<Perfil> findByNome(String nome);

}
