package com.example.loginbase.jogo.comum;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.seguranca.RegistroSessaoSuccessHandler;
import com.example.loginbase.seguranca.SecurityConfig;

@WebMvcTest(ApiInfraWebMvcTest.ControllerTeste.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class, ApiInfraWebMvcTest.ControllerTeste.class })
class ApiInfraWebMvcTest {

	@RestController
	static class ControllerTeste {
		@GetMapping("/api/teste/erro")
		String erro() {
			throw new JogoException(HttpStatus.CONFLICT, "conflito de teste");
		}
	}

	@Autowired
	MockMvc mvc;

	@MockitoBean
	RegistroSessaoSuccessHandler handler;

	@Test
	void apiSemAutenticacaoDevolve401() throws Exception {
		mvc.perform(get("/api/teste/erro")).andExpect(status().isUnauthorized());
	}

	@Test
	void jogoExceptionViraErroComStatus() throws Exception {
		mvc.perform(get("/api/teste/erro").with(user("a@b.c").roles("USER")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("conflito de teste"));
	}
}
