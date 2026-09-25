package com.example.loginbase.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Sessao;
import com.example.loginbase.acesso.SessaoRepository;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.auditoria.UsuarioAuditorAware;

/**
 * Registra abertura e encerramento de sessões HTTP autenticadas na tabela
 * {@code sessoes}. O identificador da sessão HTTP nunca é gravado em texto
 * puro: apenas o hash SHA-256 ({@link #hashToken(String)}) é persistido,
 * permitindo localizar o registro a partir da sessão sem expor um valor
 * utilizável.
 */
@Service
public class SessaoService {

	private static final int TAMANHO_MAXIMO_IP = 45;
	private static final int TAMANHO_MAXIMO_DISPOSITIVO = 500;

	private final UsuarioRepository usuarioRepository;
	private final SessaoRepository sessaoRepository;

	public SessaoService(UsuarioRepository usuarioRepository, SessaoRepository sessaoRepository) {
		this.usuarioRepository = usuarioRepository;
		this.sessaoRepository = sessaoRepository;
	}

	/**
	 * @return hash SHA-256, em hexadecimal minúsculo (64 caracteres), do
	 *         identificador de sessão informado.
	 */
	public static String hashToken(String sessionId) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(sessionId.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Algoritmo SHA-256 indisponível", e);
		}
	}

	@Transactional
	public void registrarInicio(String email, String sessionId, String ip, String userAgent) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));

		Sessao sessao = new Sessao();
		sessao.setUsuario(usuario);
		sessao.setDataInicio(Instant.now());
		sessao.setToken(hashToken(sessionId));
		sessao.setIp(truncar(ip, TAMANHO_MAXIMO_IP));
		sessao.setDispositivo(truncar(userAgent, TAMANHO_MAXIMO_DISPOSITIVO));

		sessaoRepository.save(sessao);
	}

	@Transactional
	public void registrarFim(String sessionId) {
		sessaoRepository.findByTokenAndDataFimIsNull(hashToken(sessionId))
				.ifPresent(sessao -> sessao.setDataFim(Instant.now()));
	}

	@Transactional
	public int fecharTodasAbertas() {
		return sessaoRepository.fecharTodasAbertas(Instant.now(), UsuarioAuditorAware.SISTEMA);
	}

	private static String truncar(String valor, int tamanhoMaximo) {
		if (valor == null || valor.length() <= tamanhoMaximo) {
			return valor;
		}
		return valor.substring(0, tamanhoMaximo);
	}

}
