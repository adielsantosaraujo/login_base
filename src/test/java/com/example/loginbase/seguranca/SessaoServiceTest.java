package com.example.loginbase.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.loginbase.acesso.Sessao;
import com.example.loginbase.acesso.SessaoRepository;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.auditoria.UsuarioAuditorAware;

class SessaoServiceTest {

	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final SessaoRepository sessaoRepository = mock(SessaoRepository.class);
	private final SessaoService sessaoService = new SessaoService(usuarioRepository, sessaoRepository);

	@Test
	void hashTokenNaoRepeteOIdDaSessao() {
		String sessionId = "ABCDEF1234567890";

		String hash = SessaoService.hashToken(sessionId);

		assertThat(hash).isNotEqualTo(sessionId);
	}

	@Test
	void hashTokenTem64Caracteres() {
		String hash = SessaoService.hashToken("qualquer-id-de-sessao");

		assertThat(hash).hasSize(64);
	}

	@Test
	void hashTokenEDeterministico() {
		String sessionId = "mesmo-id-de-sessao";

		String hash1 = SessaoService.hashToken(sessionId);
		String hash2 = SessaoService.hashToken(sessionId);

		assertThat(hash1).isEqualTo(hash2);
	}

	@Test
	void registrarInicioSalvaSessaoComTokenIpEDispositivoTruncado() {
		Usuario usuario = new Usuario();
		usuario.setEmail("ana@exemplo.com");
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario));

		String sessionId = "id-da-sessao-http";
		String userAgentLongo = "A".repeat(600);

		sessaoService.registrarInicio("ana@exemplo.com", sessionId, "127.0.0.1", userAgentLongo);

		ArgumentCaptor<Sessao> captor = ArgumentCaptor.forClass(Sessao.class);
		verify(sessaoRepository).save(captor.capture());

		Sessao sessaoSalva = captor.getValue();
		assertThat(sessaoSalva.getUsuario()).isSameAs(usuario);
		assertThat(sessaoSalva.getToken()).isEqualTo(SessaoService.hashToken(sessionId));
		assertThat(sessaoSalva.getIp()).isEqualTo("127.0.0.1");
		assertThat(sessaoSalva.getDispositivo()).hasSize(500);
		assertThat(sessaoSalva.getDispositivo()).isEqualTo(userAgentLongo.substring(0, 500));
		assertThat(sessaoSalva.getDataInicio()).isNotNull();
	}

	@Test
	void registrarInicioComUsuarioInexistenteLancaIllegalStateException() {
		when(usuarioRepository.findByEmail("inexistente@exemplo.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessaoService.registrarInicio("inexistente@exemplo.com", "id", "127.0.0.1", "ua"))
				.isInstanceOf(IllegalStateException.class);

		verify(sessaoRepository, never()).save(any());
	}

	@Test
	void registrarFimPreencheDataFimDaSessaoAberta() {
		String sessionId = "id-da-sessao-http";
		Sessao sessao = new Sessao();
		when(sessaoRepository.findByTokenAndDataFimIsNull(SessaoService.hashToken(sessionId)))
				.thenReturn(Optional.of(sessao));

		sessaoService.registrarFim(sessionId);

		assertThat(sessao.getDataFim()).isNotNull();
	}

	@Test
	void registrarFimSemSessaoAbertaNaoFalha() {
		when(sessaoRepository.findByTokenAndDataFimIsNull(any())).thenReturn(Optional.empty());

		sessaoService.registrarFim("id-qualquer");
	}

	@Test
	void fecharTodasAbertasDelegaAoRepositorioComSistema() {
		when(sessaoRepository.fecharTodasAbertas(any(Instant.class), eq(UsuarioAuditorAware.SISTEMA)))
				.thenReturn(3);

		int fechadas = sessaoService.fecharTodasAbertas();

		assertThat(fechadas).isEqualTo(3);
		verify(sessaoRepository, times(1)).fecharTodasAbertas(any(Instant.class), eq(UsuarioAuditorAware.SISTEMA));
	}

}
