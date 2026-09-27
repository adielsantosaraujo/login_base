package com.example.loginbase.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.loginbase.seguranca.RegistroSessaoSuccessHandler;
import com.example.loginbase.seguranca.SecurityConfig;
import com.example.loginbase.seguranca.SessaoService;

/**
 * Testes de fatia web (Task 1.1) cobrindo o tratamento diferenciado dado a
 * {@code /api/**} pelo {@link SecurityConfig}: 401 sem redirecionamento para
 * anônimo, 403 para autenticado sem token CSRF, 200 para autenticado com
 * token CSRF válido, request cache ignorando essas requisições e rotas
 * não-API continuando a redirecionar anônimos para {@code /login}. O
 * endpoint {@code /api/teste} usado nos testes é servido por
 * {@link ControladorTesteApi}, controlador de apoio existente apenas para
 * esta fatia de teste.
 */
@WebMvcTest(ControladorTesteApi.class)
@Import({ SecurityConfig.class, RegistroSessaoSuccessHandler.class })
class ApiSegurancaWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessaoService sessaoService;

	@Test
	void getApiAnonimoRecebe401SemRedirecionamento() throws Exception {
		mockMvc.perform(get("/api/teste"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void postApiAutenticadoSemTokenCsrfRecebe403() throws Exception {
		mockMvc.perform(post("/api/teste").with(user("ana@exemplo.com")))
				.andExpect(status().isForbidden());
	}

	@Test
	void postApiAutenticadoComTokenCsrfValidoRecebe200() throws Exception {
		mockMvc.perform(post("/api/teste").with(user("ana@exemplo.com")).with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	void rotaNaoApiAnonimaRedirecionaParaLogin() throws Exception {
		// AntPathMatcher exige pelo menos um segmento antes de "**", então
		// "**/login" só bate com URLs absolutas; aqui o Location é relativo
		// ("/login"), daí "/**/login".
		mockMvc.perform(get("/pagina-generica"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrlPattern("/**/login"));
	}

	@Test
	void requisicaoApiAnonimaNaoEGuardadaNoRequestCache() throws Exception {
		// HttpSessionRequestCache só cria sessão para guardar a requisição
		// quando o RequestMatcher configurado bate com o request; como
		// "/api/**" foi excluído desse matcher, nenhuma sessão é criada aqui
		// e, após o login, o usuário não é redirecionado de volta para a API.
		MvcResult resultado = mockMvc.perform(get("/api/teste"))
				.andExpect(status().isUnauthorized())
				.andReturn();

		assertThat(resultado.getRequest().getSession(false)).isNull();
	}

}
