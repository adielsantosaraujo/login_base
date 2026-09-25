package com.example.loginbase.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.loginbase.acesso.PerfilPermissaoVigente;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioPerfilRepository;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.seguranca.IdentificadorLogin;
import com.example.loginbase.seguranca.RegistroSessaoSuccessHandler;
import com.example.loginbase.seguranca.SecurityConfig;
import com.example.loginbase.seguranca.SessaoService;
import com.example.loginbase.seguranca.UsuarioDetailsService;

/**
 * Testes de fatia web (Task 4.4) cobrindo autenticação por formulário,
 * proteção de rotas, retorno à página salva e logout. A lógica de
 * autenticação (SecurityConfig, UsuarioDetailsService, IdentificadorLogin) é
 * real; apenas a persistência (UsuarioRepository, UsuarioPerfilRepository) é
 * mockada.
 */
@WebMvcTest(PaginaController.class)
@Import({ SecurityConfig.class, UsuarioDetailsService.class, IdentificadorLogin.class,
		RegistroSessaoSuccessHandler.class })
class AutenticacaoWebMvcTest {

	private static final String SENHA = "Senha123!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private UsuarioPerfilRepository usuarioPerfilRepository;

	@MockitoBean
	private SessaoService sessaoService;

	private Usuario ana;

	@BeforeEach
	void configurarUsuario() {
		ana = new Usuario();
		ana.setId(1L);
		ana.setNome("Ana");
		ana.setEmail("ana@exemplo.com");
		ana.setCelular("11987654321");
		ana.setSenha(passwordEncoder.encode(SENHA));

		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana));
		when(usuarioRepository.findByCelular("11987654321")).thenReturn(Optional.of(ana));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of(new PerfilPermissaoVigente("ADMIN", null)));
	}

	@Test
	void anonimoEmRaizRedirecionaParaLogin() throws Exception {
		// AntPathMatcher exige pelo menos um segmento antes de "**", então
		// "**/login" só bate com URLs absolutas; aqui o Location é relativo
		// ("/login"), daí "/**/login".
		mockMvc.perform(get("/"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("/**/login"));
	}

	@Test
	void getLoginRetorna200ComCampoDeLogin() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("E-mail ou celular")));
	}

	@Test
	void getLoginComErrorMostraMensagemGenerica() throws Exception {
		mockMvc.perform(get("/login").param("error", ""))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Usuário ou senha inválidos.")));
	}

	@Test
	void getLoginComLogoutMostraMensagemDeSaida() throws Exception {
		mockMvc.perform(get("/login").param("logout", ""))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Você saiu do sistema.")));
	}

	@Test
	void loginPorEmailAutentica() throws Exception {
		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("ana@exemplo.com").password(SENHA))
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("ana@exemplo.com"));

		verify(sessaoService).registrarInicio(eq("ana@exemplo.com"), anyString(), eq("127.0.0.1"), any());
	}

	@Test
	void loginPorEmailComCaixaEEspacosDiferentesAutentica() throws Exception {
		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user(" Ana@Exemplo.COM ").password(SENHA))
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("ana@exemplo.com"));

		verify(sessaoService).registrarInicio(eq("ana@exemplo.com"), anyString(), eq("127.0.0.1"), any());
	}

	@Test
	void loginPorCelularSemMascaraAutentica() throws Exception {
		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("11987654321").password(SENHA))
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("ana@exemplo.com"));

		verify(sessaoService).registrarInicio(eq("ana@exemplo.com"), anyString(), eq("127.0.0.1"), any());
	}

	@Test
	void loginPorCelularComMascaraAutentica() throws Exception {
		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("(11) 98765-4321").password(SENHA))
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("ana@exemplo.com"));

		verify(sessaoService).registrarInicio(eq("ana@exemplo.com"), anyString(), eq("127.0.0.1"), any());
	}

	@Test
	void senhaErradaRedirecionaParaLoginComErro() throws Exception {
		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("ana@exemplo.com").password("senha-errada"))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());

		verify(sessaoService, never()).registrarInicio(any(), any(), any(), any());
	}

	@Test
	void emailInexistenteRedirecionaParaLoginComErro() throws Exception {
		when(usuarioRepository.findByEmail("naoexiste@exemplo.com")).thenReturn(Optional.empty());

		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("naoexiste@exemplo.com").password(SENHA))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());

		verify(sessaoService, never()).registrarInicio(any(), any(), any(), any());
	}

	@Test
	void celularInexistenteRedirecionaParaLoginComErro() throws Exception {
		when(usuarioRepository.findByCelular("11900000000")).thenReturn(Optional.empty());

		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("11900000000").password(SENHA))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());

		verify(sessaoService, never()).registrarInicio(any(), any(), any(), any());
	}

	@Test
	void celularIncompletoRedirecionaParaLoginComErro() throws Exception {
		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("1198765432").password(SENHA))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());

		verify(sessaoService, never()).registrarInicio(any(), any(), any(), any());
	}

	@Test
	void contaSemPerfilVigenteRedirecionaParaLoginComErro() throws Exception {
		Usuario semPerfil = new Usuario();
		semPerfil.setId(2L);
		semPerfil.setNome("Bia");
		semPerfil.setEmail("bia@exemplo.com");
		semPerfil.setSenha(passwordEncoder.encode(SENHA));

		when(usuarioRepository.findByEmail("bia@exemplo.com")).thenReturn(Optional.of(semPerfil));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(2L), any(LocalDate.class)))
				.thenReturn(List.of());

		mockMvc.perform(formLogin("/login").userParameter("login").passwordParam("senha")
						.user("bia@exemplo.com").password(SENHA))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());

		verify(sessaoService, never()).registrarInicio(any(), any(), any(), any());
	}

	@Test
	void postLoginSemCsrfERejeitado() throws Exception {
		mockMvc.perform(post("/login").param("login", "ana@exemplo.com").param("senha", SENHA))
				.andExpect(status().isForbidden());
	}

	@Test
	void retornaParaPaginaSalvaAposLogin() throws Exception {
		MvcResult anonimo = mockMvc.perform(get("/qualquer"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("/**/login"))
				.andReturn();

		MockHttpSession sessao = (MockHttpSession) anonimo.getRequest().getSession(false);
		assertThat(sessao).isNotNull();

		mockMvc.perform(post("/login").session(sessao).with(csrf())
						.param("login", "ana@exemplo.com").param("senha", SENHA))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", containsString("/qualquer")));

		verify(sessaoService).registrarInicio(eq("ana@exemplo.com"), anyString(), eq("127.0.0.1"), any());
	}

	@Test
	void usuarioAutenticadoVeSejaBemVindo() throws Exception {
		mockMvc.perform(get("/").with(user("ana@exemplo.com")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Seja bem vindo")));
	}

	@Test
	void logoutRedirecionaParaLoginComMensagem() throws Exception {
		mockMvc.perform(post("/logout").with(csrf()).with(user("ana@exemplo.com")))
				.andExpect(redirectedUrl("/login?logout"));
	}

	@Test
	void recursoEstaticoPublicoNaoRedirecionaParaLogin() throws Exception {
		MvcResult resultado = mockMvc.perform(get("/css/x.css")).andReturn();

		assertThat(resultado.getResponse().getStatus()).isNotEqualTo(302);
	}

}
