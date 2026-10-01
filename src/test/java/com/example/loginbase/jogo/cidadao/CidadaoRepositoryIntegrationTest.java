package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class CidadaoRepositoryIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired EntityManager em;

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	@Test
	void persisteEConsultaCidadaosEFamilias() {
		Vila vila = novaVila();
		assertThat(vila.isPopulacaoConfirmada()).isFalse();
		Familia f = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		Cidadao pai = new Cidadao(vila.getId(), f.getId(), "Joao", Sexo.M, 480);
		Cidadao mae = new Cidadao(vila.getId(), f.getId(), "Maria", Sexo.F, 480);
		Cidadao morto = new Cidadao(vila.getId(), f.getId(), "Velho", Sexo.M, 900);
		morto.setVivo(false);
		cidadaoRepository.saveAllAndFlush(java.util.List.of(pai, mae, morto));
		pai.setConjugeId(mae.getId());
		mae.setConjugeId(pai.getId());
		pai.setForca(7);
		cidadaoRepository.saveAllAndFlush(java.util.List.of(pai, mae));
		profissaoRepository.saveAndFlush(new CidadaoProfissao(pai.getId(), Profissao.MINEIRO, 3));
		em.clear();

		assertThat(cidadaoRepository.findByVilaId(vila.getId())).hasSize(3);
		assertThat(cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId())).hasSize(2);
		assertThat(cidadaoRepository.findByFamiliaId(f.getId())).hasSize(3);
		assertThat(cidadaoRepository.findCasaisVivos(vila.getId())).extracting(Cidadao::getNome).containsExactly("Joao");
		assertThat(cidadaoRepository.findByConjugeId(pai.getId())).extracting(Cidadao::getNome).containsExactly("Maria");
		assertThat(cidadaoRepository.findByConstrucaoId(999L)).isEmpty();
		Cidadao lido = cidadaoRepository.findById(pai.getId()).orElseThrow();
		assertThat(lido.getForca()).isEqualTo(7);
		assertThat(lido.getEstado()).isEqualTo(EstadoCidadao.SAUDAVEL);
		assertThat(lido.getPontosCarPendentes()).isZero();
		assertThat(profissaoRepository.findByCidadaoIdAndProfissao(pai.getId(), Profissao.MINEIRO))
				.get().extracting(CidadaoProfissao::getPontosBase).isEqualTo(3);
		assertThat(familiaRepository.findByVilaId(vila.getId())).hasSize(1);
	}

	@Test
	void familiaLiderTemFk() {
		Vila vila = novaVila();
		Familia f = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Souza", null));
		vila.setFamiliaLiderId(f.getId());
		vilaRepository.saveAndFlush(vila);
		em.clear();
		assertThat(vilaRepository.findById(vila.getId()).orElseThrow().getFamiliaLiderId()).isEqualTo(f.getId());
	}

}
