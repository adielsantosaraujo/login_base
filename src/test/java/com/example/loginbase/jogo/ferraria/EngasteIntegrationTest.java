package com.example.loginbase.jogo.ferraria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
import com.example.loginbase.jogo.item.BonusEquipamentoService;
import com.example.loginbase.jogo.item.BonusItem;
import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.FaixaBonus;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.pedra.BonusPedra;
import com.example.loginbase.jogo.pedra.Pedra;
import com.example.loginbase.jogo.pedra.PedraRepository;
import com.example.loginbase.jogo.pedra.TipoPedra;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EngasteIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired EstoqueService estoque;
	@Autowired ItemRepository itemRepository;
	@Autowired PedraRepository pedraRepository;
	@Autowired BonusEquipamentoService bonusEquipamento;

	private Usuario usuario;
	private Vila vila;
	private Familia familia;
	private int seq;

	private void novaVila(boolean comFerraria, int ouro) {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		usuario = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		if (comFerraria) {
			ferraria(EstadoConstrucao.ATIVA);
		}
		if (ouro > 0) {
			estoque.creditar(vila, Recurso.OURO, BigDecimal.valueOf(ouro));
		}
	}

	private void ferraria(EstadoConstrucao estado) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.FERRARIA);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		construcaoRepository.saveAndFlush(c);
	}

	private Item item(Qualidade q) {
		return itemRepository.saveAndFlush(new Item(vila.getId(), ItemSubtipo.ESPADA, q, 1, List.of()));
	}

	private Pedra pedra(TipoPedra t, CodigoBonus codigo, int valor) {
		return pedraRepository.saveAndFlush(
				new Pedra(vila.getId(), t, List.of(new BonusPedra(codigo, FaixaBonus.BAIXA, valor))));
	}

	private ResultActions engastar(Long pedraId, Long itemId) throws Exception {
		return mvc.perform(post("/api/jogo/ferraria/engaste").with(user(usuario.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"pedraId\":%d,\"itemId\":%d}".formatted(pedraId, itemId)));
	}

	private ResultActions remover(Long itemId, Long pedraId) throws Exception {
		return mvc.perform(post("/api/jogo/ferraria/remover-pedra").with(user(usuario.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"itemId\":%d,\"pedraId\":%d}".formatted(itemId, pedraId)));
	}

	private ResultActions consultar(String url) throws Exception {
		return mvc.perform(get(url).with(user(usuario.getEmail()).roles("USER")));
	}

	@Test
	void engasteComSucessoDebitaOuroEAparecemListagens() throws Exception {
		novaVila(true, 100);
		Item it = item(Qualidade.EXCELENTE);
		Pedra p = pedra(TipoPedra.BOA, CodigoBonus.ATK, 3);
		consultar("/api/jogo/pedras").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].custoEngaste").value(25)).andExpect(jsonPath("$[0].bonus[0].codigo").value("ATK"));
		consultar("/api/jogo/ferraria/itens-compativeis").andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].slotsTotais").value(3)).andExpect(jsonPath("$[0].slotsLivres").value(3));

		engastar(p.getId(), it.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.custoOuro").value(25))
				.andExpect(jsonPath("$.qualidadePedra").value("BOA")).andExpect(jsonPath("$.slotsLivresApos").value(2))
				.andExpect(jsonPath("$.pedraId").value(p.getId()));

		assertThat(estoque.quantidade(vila, Recurso.OURO)).isEqualByComparingTo("75");
		assertThat(pedraRepository.findById(p.getId()).orElseThrow().getItemId()).isEqualTo(it.getId());
		consultar("/api/jogo/pedras").andExpect(jsonPath("$", hasSize(0)));
		consultar("/api/jogo/ferraria/itens-compativeis").andExpect(jsonPath("$[0].slotsLivres").value(2));
		consultar("/api/jogo/inventario/" + it.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.pedras", hasSize(1))).andExpect(jsonPath("$.pedras[0].id").value(p.getId()));
	}

	@Test
	void erros() throws Exception {
		novaVila(true, 1000);
		Item simples = item(Qualidade.SIMPLES);
		Item boa = item(Qualidade.BOA);
		Pedra p = pedra(TipoPedra.SIMPLES, CodigoBonus.ATK, 2);
		Pedra p2 = pedra(TipoPedra.SIMPLES, CodigoBonus.DEF, 2);

		engastar(p.getId() + 9999, boa.getId()).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.erro").value("Pedra não encontrada"));
		engastar(p.getId(), boa.getId() + 9999).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.erro").value("Item não encontrado"));
		engastar(p.getId(), simples.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Itens Simples não aceitam pedras"));

		Item emAprim = item(Qualidade.BOA);
		emAprim.setEmAprimoramento(true);
		itemRepository.saveAndFlush(emAprim);
		engastar(p.getId(), emAprim.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Item em aprimoramento"));

		Item equipado = item(Qualidade.BOA);
		Cidadao c = cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), familia.getId(), "X", Sexo.M, 360));
		equipado.setCidadaoId(c.getId());
		equipado.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(equipado);
		engastar(p.getId(), equipado.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Desequipe o item antes de engastar"));

		engastar(p.getId(), boa.getId()).andExpect(status().isOk());
		engastar(p.getId(), boa.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("A pedra já está engastada em outro item"));
		engastar(p2.getId(), boa.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Sem slots de pedra disponíveis"));
	}

	@Test
	void semFerrariaAtivaEOuroInsuficiente() throws Exception {
		novaVila(false, 100);
		Item it = item(Qualidade.EXCELENTE);
		Pedra divina = pedra(TipoPedra.DIVINA, CodigoBonus.ATK, 2);
		engastar(divina.getId(), it.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("É necessária uma Ferraria ativa"));
		ferraria(EstadoConstrucao.EM_OBRA);
		engastar(divina.getId(), it.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("É necessária uma Ferraria ativa"));
		ferraria(EstadoConstrucao.ATIVA);
		engastar(divina.getId(), it.getId()).andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("Ouro insuficiente"));
		assertThat(pedraRepository.findById(divina.getId()).orElseThrow().getItemId()).isNull();
	}

	@Test
	void outraVilaRetorna404() throws Exception {
		novaVila(true, 100);
		Item it = item(Qualidade.BOA);
		Pedra p = pedra(TipoPedra.SIMPLES, CodigoBonus.ATK, 2);
		Usuario antigo = usuario;
		novaVila(true, 100);
		engastar(p.getId(), it.getId()).andExpect(status().isNotFound());
		assertThat(antigo).isNotEqualTo(usuario);
	}

	@Test
	void remocaoDestroiPedraELiberaSlot() throws Exception {
		novaVila(true, 100);
		Item it = item(Qualidade.BOA);
		Pedra p = pedra(TipoPedra.SIMPLES, CodigoBonus.ATK, 2);
		Pedra outra = pedra(TipoPedra.SIMPLES, CodigoBonus.DEF, 2);
		remover(it.getId(), p.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("O item não contém esta pedra"));
		engastar(p.getId(), it.getId()).andExpect(status().isOk());
		remover(it.getId(), outra.getId()).andExpect(status().isBadRequest());
		remover(it.getId(), p.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.pedraDestruida").value(true))
				.andExpect(jsonPath("$.slotsLivresApos").value(1)).andExpect(jsonPath("$.pedraRemovidaId").value(p.getId()));
		assertThat(pedraRepository.findById(p.getId())).isEmpty();
		consultar("/api/jogo/inventario/" + it.getId()).andExpect(jsonPath("$.pedras", hasSize(0)));
	}

	@Test
	void pedraEmItemEquipadoSomaNosBonusDoEquipamento() throws Exception {
		novaVila(true, 100);
		Item it = itemRepository.saveAndFlush(new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.BOA, 1,
				List.of(new BonusItem(CodigoBonus.ATK, 3))));
		Cidadao c = cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), familia.getId(), "X", Sexo.M, 360));
		it.setCidadaoId(c.getId());
		it.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(it);
		assertThat(bonusEquipamento.somaBonus(c.getId(), CodigoBonus.ATK)).isEqualTo(3);
		Pedra p = pedra(TipoPedra.SIMPLES, CodigoBonus.ATK, 4);
		p.setItemId(it.getId());
		pedraRepository.saveAndFlush(p);
		assertThat(bonusEquipamento.somaBonus(c.getId(), CodigoBonus.ATK)).isEqualTo(7);
		assertThat(bonusEquipamento.somaBonus(c.getId(), CodigoBonus.DEF)).isZero();
	}
}
