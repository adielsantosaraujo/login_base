package com.example.loginbase.seguranca;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.NormalizacaoContato;
import com.example.loginbase.acesso.Perfil;
import com.example.loginbase.acesso.PerfilRepository;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioPerfil;
import com.example.loginbase.acesso.UsuarioPerfilRepository;
import com.example.loginbase.acesso.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Garante, na inicialização, a existência de um usuário administrador com
 * vínculo vigente ao perfil {@code ADMIN} (criado pela migração V2). Não
 * altera um usuário já existente com o e-mail configurado, nem sua senha.
 */
@Component
@Slf4j
public class AdminInicialRunner implements ApplicationRunner {

	private static final String NOME_PERFIL_ADMIN = "ADMIN";

	private final String email;

	private final String senha;

	private final UsuarioRepository usuarioRepository;

	private final PerfilRepository perfilRepository;

	private final UsuarioPerfilRepository usuarioPerfilRepository;

	private final PasswordEncoder passwordEncoder;

	public AdminInicialRunner(@Value("${app.admin.email}") String email,
			@Value("${app.admin.password}") String senha, UsuarioRepository usuarioRepository,
			PerfilRepository perfilRepository, UsuarioPerfilRepository usuarioPerfilRepository,
			PasswordEncoder passwordEncoder) {
		this.email = email;
		this.senha = senha;
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.usuarioPerfilRepository = usuarioPerfilRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (senha == null || senha.isBlank()) {
			log.warn("ADMIN_PASSWORD não definida: administrador inicial não será criado. "
					+ "Defina a variável de ambiente ADMIN_PASSWORD para criar o administrador inicial.");
			return;
		}

		if (email == null || email.isBlank()) {
			log.warn("ADMIN_EMAIL não definido: administrador inicial não será criado.");
			return;
		}

		String emailNormalizado = NormalizacaoContato.email(email);

		if (usuarioRepository.existsByEmail(emailNormalizado)) {
			log.info("Administrador inicial já existe ({}); nada foi alterado.", emailNormalizado);
			return;
		}

		Perfil perfilAdmin = perfilRepository.findByNome(NOME_PERFIL_ADMIN).orElseGet(() -> {
			Perfil perfil = new Perfil();
			perfil.setNome(NOME_PERFIL_ADMIN);
			perfil.setDescricao("Administrador do sistema");
			return perfilRepository.save(perfil);
		});

		Usuario usuario = new Usuario();
		usuario.setNome("Administrador");
		usuario.setEmail(emailNormalizado);
		usuario.setSenha(passwordEncoder.encode(senha));
		usuario = usuarioRepository.save(usuario);

		UsuarioPerfil usuarioPerfil = new UsuarioPerfil();
		usuarioPerfil.setUsuario(usuario);
		usuarioPerfil.setPerfil(perfilAdmin);
		usuarioPerfil.setDataInicial(LocalDate.now());
		usuarioPerfil.setDataFinal(null);
		usuarioPerfilRepository.save(usuarioPerfil);

		log.info("Administrador inicial criado com e-mail {}.", emailNormalizado);
	}

}
