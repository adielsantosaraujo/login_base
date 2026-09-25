package com.example.loginbase.acesso;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	/**
	 * @param email e-mail já normalizado (ver {@link NormalizacaoContato#email(String)})
	 */
	Optional<Usuario> findByEmail(String email);

	/**
	 * @param celular celular já normalizado (só dígitos)
	 */
	Optional<Usuario> findByCelular(String celular);

	boolean existsByEmail(String email);

}
