package com.example.loginbase.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configuração de segurança da aplicação: autenticação por formulário (e-mail
 * ou celular como identificador de login), controle de sessão HTTP e
 * proteção de rotas.
 *
 * <p>Documentação OpenAPI: {@code /v3/api-docs/**} e {@code /swagger-ui/**}
 * exigem autenticação (nenhum {@code permitAll} novo). O JSON da API sem login
 * recebe 401 em vez de redirecionamento e não entra no cache de requisições;
 * a UI do Swagger sem login redireciona para {@code /login} e, após entrar,
 * o usuário volta a ela.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RegistroSessaoSuccessHandler handler)
            throws Exception {
        RequestMatcher api = PathPatternRequestMatcher.withDefaults().matcher("/api/**");
        RequestMatcher apiDocs = PathPatternRequestMatcher.withDefaults().matcher("/v3/api-docs/**");
        // Requisições automáticas (DevTools, assets, favicon) não devem virar
        // "página salva" e sequestrar o redirecionamento pós-login.
        RequestMatcher naoSalvar = new OrRequestMatcher(
                api,
                apiDocs,
                PathPatternRequestMatcher.withDefaults().matcher("/.well-known/**"),
                PathPatternRequestMatcher.withDefaults().matcher("/patrimonio/**"),
                PathPatternRequestMatcher.withDefaults().matcher("/cadastro_usuario/**"),
                PathPatternRequestMatcher.withDefaults().matcher("/favicon.ico"));
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(request -> !naoSalvar.matches(request));

        http
                .csrf(csrf -> csrf.spa())
                .requestCache(cache -> cache.requestCache(requestCache))
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new OrRequestMatcher(api, apiDocs))
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                                AnyRequestMatcher.INSTANCE))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error",
                                "/.well-known/**", "/cadastro_usuario/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("login")
                        .passwordParameter("senha")
                        .successHandler(handler)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
