package com.example.loginbase.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Teste de integração (contexto completo, exige o Postgres) da documentação
 * OpenAPI: o JSON e a UI do Swagger respondem para usuário autenticado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegracaoTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsAutenticadoRetornaDocumento() throws Exception {
		mockMvc.perform(get("/v3/api-docs").with(user("ana@exemplo.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.info.title").value("login-base API"))
				.andExpect(jsonPath("$.components.securitySchemes.sessao").exists());
	}

	@Test
	void swaggerUiAutenticadoRetorna200() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html").with(user("ana@exemplo.com")))
				.andExpect(status().isOk());
	}

	@Test
	void swaggerUiHtmlRedirecionaParaIndex() throws Exception {
		mockMvc.perform(get("/swagger-ui.html").with(user("ana@exemplo.com")))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/swagger-ui/index.html"));
	}
}
