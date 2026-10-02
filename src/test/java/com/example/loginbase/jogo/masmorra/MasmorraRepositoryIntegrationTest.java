package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@Transactional
class MasmorraRepositoryIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired MasmorraRepository repository;

	private Vila vila;

	@BeforeEach
	void setUp() {
		vila = novaVila();
	}

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private Masmorra salvar(Vila v, int regiao, int nivel, boolean ativa) {
		Masmorra m = new Masmorra(v.getId(), regiao, nivel, 12);
		m.setAtiva(ativa);
		return repository.saveAndFlush(m);
	}

	private void rejeita(String campo, int valor) {
		Masmorra m = new Masmorra(vila.getId(), 1, 1, 12);
		ReflectionTestUtils.setField(m, campo, valor);
		assertThatThrownBy(() -> repository.saveAndFlush(m)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void checkRejeitaNivelZero() {
		rejeita("nivel", 0);
	}

	@Test
	void checkRejeitaNivelOnze() {
		rejeita("nivel", 11);
	}

	@Test
	void checkRejeitaRegiaoZero() {
		rejeita("regiaoIndice", 0);
	}

	@Test
	void checkRejeitaRegiaoDezessete() {
		rejeita("regiaoIndice", 17);
	}

	@Test
	void persisteComDefaults() {
		Masmorra m = salvar(vila, 5, 3, true);
		assertThat(m.getId()).isNotNull();
		assertThat(m.getCriadoEm()).isNotNull();
		assertThat(m.getTurnosSemAtaque()).isZero();
	}

	@Test
	void indiceParcialPermiteInativaERejeitaDuasAtivas() {
		salvar(vila, 4, 1, false);
		salvar(vila, 4, 2, true);
		assertThatThrownBy(() -> salvar(vila, 4, 3, true)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void consultasConsideramSomenteAtivasDaVila() {
		Masmorra a = salvar(vila, 2, 1, true);
		Masmorra b = salvar(vila, 7, 4, true);
		salvar(vila, 9, 2, false);
		Vila outra = novaVila();
		Masmorra o = salvar(outra, 2, 1, true);

		assertThat(repository.findByVilaIdAndAtivaTrueOrderByIdAsc(vila.getId())).extracting(Masmorra::getId)
				.containsExactly(a.getId(), b.getId());
		assertThat(repository.countByVilaIdAndAtivaTrue(vila.getId())).isEqualTo(2);
		assertThat(repository.findByIdAndVilaIdAndAtivaTrue(a.getId(), vila.getId())).isPresent();
		assertThat(repository.findByIdAndVilaIdAndAtivaTrue(o.getId(), vila.getId())).isEmpty();
		assertThat(repository.findByVilaIdAndRegiaoIndiceAndAtivaTrue(vila.getId(), 7)).get().extracting(Masmorra::getId)
				.isEqualTo(b.getId());
		assertThat(repository.findByVilaIdAndRegiaoIndiceAndAtivaTrue(vila.getId(), 9)).isEmpty();
	}

}
