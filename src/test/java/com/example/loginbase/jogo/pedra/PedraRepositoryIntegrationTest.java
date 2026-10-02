package com.example.loginbase.jogo.pedra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.item.BonusItem;
import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.FaixaBonus;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class PedraRepositoryIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired PedraRepository pedraRepository;
	@Autowired EntityManager em;

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		return vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
	}

	private Item novoItem(Vila vila) {
		return itemRepository.saveAndFlush(new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.BOA, 3,
				List.of(new BonusItem(CodigoBonus.ATK, 4))));
	}

	private Pedra pedra(Vila vila, TipoPedra tipo) {
		return new Pedra(vila.getId(), tipo, List.of(new BonusPedra(CodigoBonus.VIT, FaixaBonus.MEDIA, 2),
				new BonusPedra(CodigoBonus.ATK, FaixaBonus.MEDIA, 5)));
	}

	@Test
	void persisteBonusJsonb() {
		Vila vila = novaVila();
		Pedra p = pedraRepository.saveAndFlush(pedra(vila, TipoPedra.BOA));
		em.clear();
		Pedra lida = pedraRepository.findById(p.getId()).orElseThrow();
		assertThat(lida.getQualidade()).isEqualTo(TipoPedra.BOA);
		assertThat(lida.getBonus()).containsExactly(new BonusPedra(CodigoBonus.VIT, FaixaBonus.MEDIA, 2),
				new BonusPedra(CodigoBonus.ATK, FaixaBonus.MEDIA, 5));
		assertThat(lida.getItemId()).isNull();
		assertThat(lida.getCriadaEm()).isNotNull();
	}

	@Test
	void consultas() {
		Vila vila = novaVila();
		Vila outra = novaVila();
		Item item = novoItem(vila);
		Item item2 = novoItem(vila);
		Pedra a = pedraRepository.saveAndFlush(pedra(vila, TipoPedra.SIMPLES));
		Pedra b = pedraRepository.saveAndFlush(pedra(vila, TipoPedra.DIVINA));
		Pedra c = pedra(vila, TipoPedra.BOA);
		c.setItemId(item.getId());
		c = pedraRepository.saveAndFlush(c);
		Pedra d = pedra(vila, TipoPedra.BOA);
		d.setItemId(item2.getId());
		d = pedraRepository.saveAndFlush(d);
		pedraRepository.saveAndFlush(pedra(outra, TipoPedra.SIMPLES));
		em.clear();

		assertThat(pedraRepository.findByVilaIdAndItemIdIsNullOrderByIdAsc(vila.getId()))
				.extracting(Pedra::getId).containsExactly(a.getId(), b.getId());
		assertThat(pedraRepository.findByItemId(item.getId())).extracting(Pedra::getId).containsExactly(c.getId());
		assertThat(pedraRepository.findByItemIdIn(List.of(item.getId(), item2.getId())))
				.extracting(Pedra::getId).containsExactlyInAnyOrder(c.getId(), d.getId());
		assertThat(pedraRepository.countByItemId(item.getId())).isEqualTo(1);
		assertThat(pedraRepository.countByItemId(-1L)).isZero();
	}

	@Test
	void apagarItemApagaPedrasEngastadas() {
		Vila vila = novaVila();
		Item item = novoItem(vila);
		Pedra engastada = pedra(vila, TipoPedra.EXCELENTE);
		engastada.setItemId(item.getId());
		engastada = pedraRepository.saveAndFlush(engastada);
		Pedra livre = pedraRepository.saveAndFlush(pedra(vila, TipoPedra.SIMPLES));
		em.clear();

		itemRepository.deleteById(item.getId());
		itemRepository.flush();
		em.clear();

		assertThat(pedraRepository.findById(engastada.getId())).isEmpty();
		assertThat(pedraRepository.findById(livre.getId())).isPresent();
	}
}
