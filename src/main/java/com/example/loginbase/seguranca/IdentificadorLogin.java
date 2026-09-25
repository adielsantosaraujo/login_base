package com.example.loginbase.seguranca;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.loginbase.acesso.NormalizacaoContato;

/**
 * Interpreta o identificador digitado no formulário de login (campo único
 * "E-mail ou celular"), classificando-o como e-mail ou celular já
 * normalizado, conforme a regra descrita em
 * {@code specs/user-authentication/spec.md}: depois de remover espaços nas
 * pontas, presença de {@code @} indica e-mail; caso contrário, apenas os
 * dígitos são considerados e o resultado precisa ter exatamente
 * {@value com.example.loginbase.acesso.NormalizacaoContato#DIGITOS_CELULAR}
 * dígitos para ser tratado como celular.
 */
@Component
public class IdentificadorLogin {

	public enum Tipo {
		EMAIL, CELULAR
	}

	public record Identificador(Tipo tipo, String valor) {
	}

	/**
	 * @param entrada texto digitado pelo usuário no campo de login
	 * @return o identificador classificado e normalizado, ou vazio quando a
	 *         entrada é nula, em branco, ou um celular com quantidade de
	 *         dígitos diferente de {@value com.example.loginbase.acesso.NormalizacaoContato#DIGITOS_CELULAR}
	 */
	public Optional<Identificador> interpretar(String entrada) {
		if (entrada == null || entrada.isBlank()) {
			return Optional.empty();
		}

		String valor = entrada.strip();

		if (valor.contains("@")) {
			return Optional.of(new Identificador(Tipo.EMAIL, NormalizacaoContato.email(valor)));
		}

		String digitos = NormalizacaoContato.digitos(valor);
		if (digitos.length() != NormalizacaoContato.DIGITOS_CELULAR) {
			return Optional.empty();
		}

		return Optional.of(new Identificador(Tipo.CELULAR, digitos));
	}

}
