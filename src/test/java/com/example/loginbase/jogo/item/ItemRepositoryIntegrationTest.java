package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ItemRepositoryIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired FabricacaoRepository fabricacaoRepository;
	@Autowired EntityManager em;

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private Cidadao novoCidadao(Vila vila, String nome) {
		Familia f = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		return cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), f.getId(), nome, Sexo.M, 480));
	}

	private Item item(Vila vila, ItemSubtipo subtipo) {
		return new Item(vila.getId(), subtipo, Qualidade.BOA, 3, List.of(new BonusItem(CodigoBonus.ATK, 4)));
	}

	@Test
	void persisteItemComBonusJsonb() {
		Vila vila = novaVila();
		Item i = itemRepository.saveAndFlush(new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.EXCELENTE, 5,
				List.of(new BonusItem(CodigoBonus.ATK, 7), new BonusItem(CodigoBonus.CRIT, 2))));
		em.clear();
		Item lido = itemRepository.findById(i.getId()).orElseThrow();
		assertThat(lido.getCategoria()).isEqualTo(ItemCategoria.ARMA);
		assertThat(lido.getBonus()).containsExactly(new BonusItem(CodigoBonus.ATK, 7), new BonusItem(CodigoBonus.CRIT, 2));
		assertThat(lido.isEmAprimoramento()).isFalse();
		assertThat(lido.getCriadoEm()).isNotNull();
	}

	@Test
	void inventarioEConsultas() {
		Vila vila = novaVila();
		Cidadao c = novoCidadao(vila, "Joao");
		itemRepository.saveAndFlush(item(vila, ItemSubtipo.ESPADA));
		itemRepository.saveAndFlush(item(vila, ItemSubtipo.PEITORAL));
		Item eq = item(vila, ItemSubtipo.LANCA);
		eq.setCidadaoId(c.getId());
		eq.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(eq);
		em.clear();
		assertThat(itemRepository.findByVilaId(vila.getId())).hasSize(3);
		assertThat(itemRepository.findByCidadaoId(c.getId())).hasSize(1);
		assertThat(itemRepository.findByCidadaoIdAndSlot(c.getId(), SlotEquipamento.ARMA)).isPresent();
		assertThat(itemRepository.findByVilaIdAndCidadaoIdIsNull(vila.getId(), PageRequest.of(0, 10))
				.getTotalElements()).isEqualTo(2);
		assertThat(itemRepository.findByVilaIdAndCidadaoIdIsNullAndCategoria(vila.getId(), ItemCategoria.ARMADURA,
				PageRequest.of(0, 10)).getContent()).hasSize(1);
	}

	@Test
	void slotUnicoPorCidadao() {
		Vila vila = novaVila();
		Cidadao c = novoCidadao(vila, "Joao");
		Item a = item(vila, ItemSubtipo.ESPADA);
		a.setCidadaoId(c.getId());
		a.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(a);
		Item b = item(vila, ItemSubtipo.LANCA);
		b.setCidadaoId(c.getId());
		b.setSlot(SlotEquipamento.ARMA);
		assertThatThrownBy(() -> itemRepository.saveAndFlush(b)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void checkCidadaoSlotConsistentes() {
		Vila vila = novaVila();
		Cidadao c = novoCidadao(vila, "Joao");
		Item a = item(vila, ItemSubtipo.ESPADA);
		a.setCidadaoId(c.getId());
		assertThatThrownBy(() -> itemRepository.saveAndFlush(a)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void fabricacaoPersisteEArtesaoUnico() {
		Vila vila = novaVila();
		Cidadao c = novoCidadao(vila, "Joao");
		Construcao oficina = new Construcao();
		oficina.setVilaId(vila.getId());
		oficina.setTipo(TipoConstrucao.FERRARIA);
		oficina.setNivel(NivelConstrucao.N1);
		oficina.setRegiaoIndice(1);
		oficina.setX(0);
		oficina.setY(0);
		oficina.setTamanho(1);
		oficina.setEstado(EstadoConstrucao.ATIVA);
		oficina.setPoTotal(1);
		oficina.setPoAtual(1);
		oficina = construcaoRepository.saveAndFlush(oficina);

		Fabricacao f = fabricacaoRepository.saveAndFlush(
				new Fabricacao(vila.getId(), oficina.getId(), c.getId(), ItemSubtipo.ESPADA, 2, 3, 1));
		em.clear();
		Fabricacao lida = fabricacaoRepository.findByArtesaoId(c.getId()).orElseThrow();
		assertThat(lida.getId()).isEqualTo(f.getId());
		assertThat(lida.getEstado()).isEqualTo(EstadoFabricacao.EM_ANDAMENTO);
		assertThat(lida.getPfAtual()).isEqualByComparingTo("0");
		assertThat(fabricacaoRepository.existsByArtesaoId(c.getId())).isTrue();
		assertThat(fabricacaoRepository.findByConstrucaoId(oficina.getId())).hasSize(1);
		assertThat(fabricacaoRepository.findByVilaId(vila.getId())).hasSize(1);

		Fabricacao outra = new Fabricacao(vila.getId(), oficina.getId(), c.getId(), ItemSubtipo.LANCA, 1, 2, 1);
		assertThatThrownBy(() -> fabricacaoRepository.saveAndFlush(outra))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
