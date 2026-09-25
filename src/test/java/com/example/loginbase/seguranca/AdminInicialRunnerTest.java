package com.example.loginbase.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.loginbase.acesso.Perfil;
import com.example.loginbase.acesso.PerfilRepository;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioPerfil;
import com.example.loginbase.acesso.UsuarioPerfilRepository;
import com.example.loginbase.acesso.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminInicialRunnerTest {

	private static final String EMAIL = "Admin@LoginBase.local";
	private static final String EMAIL_NORMALIZADO = "admin@loginbase.local";
	private static final String SENHA = "Troque123";
	private static final String SENHA_HASH = "{bcrypt}hash-fake";

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PerfilRepository perfilRepository;

	@Mock
	private UsuarioPerfilRepository usuarioPerfilRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private DefaultApplicationArguments args;

	@BeforeEach
	void setUp() {
		args = new DefaultApplicationArguments();
	}

	@Test
	void semSenhaNaoCriaAdministrador() {
		AdminInicialRunner runner = new AdminInicialRunner(EMAIL, "", usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		runner.run(args);

		verifyNoInteractions(usuarioRepository, perfilRepository, usuarioPerfilRepository, passwordEncoder);
	}

	@Test
	void senhaEmBrancoNaoCriaAdministrador() {
		AdminInicialRunner runner = new AdminInicialRunner(EMAIL, "   ", usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		runner.run(args);

		verifyNoInteractions(usuarioRepository, perfilRepository, usuarioPerfilRepository, passwordEncoder);
	}

	@Test
	void emailEmBrancoNaoCriaAdministrador() {
		AdminInicialRunner runner = new AdminInicialRunner("   ", SENHA, usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		runner.run(args);

		verifyNoInteractions(usuarioRepository, perfilRepository, usuarioPerfilRepository, passwordEncoder);
	}

	@Test
	void criaAdministradorQuandoNaoExiste() {
		AdminInicialRunner runner = new AdminInicialRunner(EMAIL, SENHA, usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		when(usuarioRepository.existsByEmail(EMAIL_NORMALIZADO)).thenReturn(false);
		when(perfilRepository.findByNome("ADMIN")).thenReturn(Optional.empty());
		when(perfilRepository.save(any(Perfil.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_HASH);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(usuarioPerfilRepository.save(any(UsuarioPerfil.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		runner.run(args);

		ArgumentCaptor<Perfil> perfilCaptor = ArgumentCaptor.forClass(Perfil.class);
		verify(perfilRepository).save(perfilCaptor.capture());
		assertThat(perfilCaptor.getValue().getNome()).isEqualTo("ADMIN");
		assertThat(perfilCaptor.getValue().getDescricao()).isEqualTo("Administrador do sistema");

		ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(usuarioCaptor.capture());
		Usuario usuarioSalvo = usuarioCaptor.getValue();
		assertThat(usuarioSalvo.getNome()).isEqualTo("Administrador");
		assertThat(usuarioSalvo.getEmail()).isEqualTo(EMAIL_NORMALIZADO);
		assertThat(usuarioSalvo.getSenha()).isEqualTo(SENHA_HASH);
		assertThat(usuarioSalvo.getCelular()).isNull();

		ArgumentCaptor<UsuarioPerfil> vinculoCaptor = ArgumentCaptor.forClass(UsuarioPerfil.class);
		verify(usuarioPerfilRepository).save(vinculoCaptor.capture());
		UsuarioPerfil vinculoSalvo = vinculoCaptor.getValue();
		assertThat(vinculoSalvo.getUsuario()).isEqualTo(usuarioSalvo);
		assertThat(vinculoSalvo.getPerfil()).isEqualTo(perfilCaptor.getValue());
		assertThat(vinculoSalvo.getDataInicial()).isEqualTo(LocalDate.now());
		assertThat(vinculoSalvo.getDataFinal()).isNull();
	}

	@Test
	void criaAdministradorReaproveitandoPerfilAdminExistente() {
		AdminInicialRunner runner = new AdminInicialRunner(EMAIL, SENHA, usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		Perfil perfilExistente = new Perfil();
		perfilExistente.setNome("ADMIN");
		perfilExistente.setDescricao("Administrador do sistema");

		when(usuarioRepository.existsByEmail(EMAIL_NORMALIZADO)).thenReturn(false);
		when(perfilRepository.findByNome("ADMIN")).thenReturn(Optional.of(perfilExistente));
		when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_HASH);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		runner.run(args);

		verify(perfilRepository, never()).save(any());
	}

	@Test
	void naoAlteraAdministradorJaExistente() {
		AdminInicialRunner runner = new AdminInicialRunner(EMAIL, SENHA, usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		when(usuarioRepository.existsByEmail(EMAIL_NORMALIZADO)).thenReturn(true);

		runner.run(args);

		verify(usuarioRepository, never()).save(any());
		verify(perfilRepository, never()).save(any());
		verify(usuarioPerfilRepository, never()).save(any());
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void naoDependeDoMetodoEncodeGenericoQuandoUsuarioJaExiste() {
		AdminInicialRunner runner = new AdminInicialRunner(EMAIL, SENHA, usuarioRepository, perfilRepository,
				usuarioPerfilRepository, passwordEncoder);

		when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

		runner.run(args);

		verify(passwordEncoder, never()).encode(anyString());
	}

}
