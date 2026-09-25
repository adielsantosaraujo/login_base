package com.example.loginbase.acesso;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfil, Long> {

	@Query("""
			select new com.example.loginbase.acesso.PerfilPermissaoVigente(p.nome, perm.nome)
			from UsuarioPerfil up join up.perfil p
			left join PerfilPermissao pp on pp.perfil = p
			left join pp.permissao perm
			where up.usuario.id = :usuarioId and up.dataInicial <= :data
			  and (up.dataFinal is null or up.dataFinal >= :data)""")
	List<PerfilPermissaoVigente> findPerfisVigentesComPermissoes(@Param("usuarioId") Long usuarioId,
			@Param("data") LocalDate data);

}
