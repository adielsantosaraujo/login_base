package com.example.loginbase.auditoria;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita o Spring Data JPA Auditing usando {@link UsuarioAuditorAware} como
 * fonte do autor. Mantido fora de {@code LoginBaseApplication} para não
 * carregar o auditing em testes de fatia (por exemplo, {@code @WebMvcTest}).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditoriaConfig {
}
