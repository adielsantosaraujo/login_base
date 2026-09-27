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
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configuração de segurança da aplicação: autenticação por formulário (e-mail
 * ou celular como identificador de login), controle de sessão HTTP e
 * proteção de rotas.
 *
 * <p>
 * Endpoints de API ({@code /api/**}) têm tratamento diferenciado das demais
 * rotas: acesso anônimo recebe HTTP 401 sem redirecionamento para
 * {@code /login}, e a requisição não é guardada no request cache (após o
 * login, o usuário volta para {@code /}, não para a API). A proteção CSRF
 * para esses endpoints é feita via cookie {@code XSRF-TOKEN} legível por
 * JavaScript, aceito no cabeçalho {@code X-XSRF-TOKEN}; o formulário
 * Thymeleaf de login continua usando o parâmetro oculto {@code _csrf}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RegistroSessaoSuccessHandler handler)
            throws Exception {
        RequestMatcher matcherApi = PathPatternRequestMatcher.withDefaults().matcher("/api/**");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error")
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
                        .sessionFixation(fixation -> fixation.changeSessionId()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // Com um único defaultAuthenticationEntryPointFor registrado, o Spring
                        // Security o usa como fallback global (não apenas para o matcher
                        // informado) e deixa de registrar automaticamente o
                        // LoginUrlAuthenticationEntryPoint do form login. Por isso o catch-all
                        // para as demais rotas precisa ser explícito aqui.
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                matcherApi)
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                                AnyRequestMatcher.INSTANCE))
                .requestCache(requestCache -> requestCache.requestCache(requestCacheSemApi(matcherApi)))
                .csrf(csrf -> csrf.spa());

        return http.build();
    }

    private RequestCache requestCacheSemApi(RequestMatcher matcherApi) {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(new NegatedRequestMatcher(matcherApi));
        return requestCache;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
