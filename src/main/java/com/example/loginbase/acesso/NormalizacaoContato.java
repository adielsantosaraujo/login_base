package com.example.loginbase.acesso;

import java.util.Locale;

/**
 * Normalização de e-mail e celular usada por {@link Usuario} e por quem
 * precisar buscar um usuário pelo identificador de login. Centralizada aqui
 * para que a mesma regra seja aplicada na gravação e na busca.
 */
public final class NormalizacaoContato {

	public static final int DIGITOS_CELULAR = 11;

	private NormalizacaoContato() {
	}

	/**
	 * Remove espaços nas pontas e converte para minúsculas. {@code null}
	 * permanece {@code null}.
	 */
	public static String email(String s) {
		if (s == null) {
			return null;
		}
		return s.strip().toLowerCase(Locale.ROOT);
	}

	/**
	 * Mantém apenas os dígitos da string. {@code null} permanece
	 * {@code null}.
	 */
	public static String digitos(String s) {
		if (s == null) {
			return null;
		}
		return s.replaceAll("\\D", "");
	}

}
