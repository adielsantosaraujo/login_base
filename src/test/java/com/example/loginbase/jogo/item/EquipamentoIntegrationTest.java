package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.MorteService;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EquipamentoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired MorteService morteService;

	private Usuario usuario;
	private Vila vila;
	private Familia familia;
	private int seq;

	private void novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		usuario = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
	}

	private Cidadao cidadao(int idadeAnos) {
		return cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, idadeAnos * 12));
	}

	private Item item(ItemSubtipo st, int nivel) {
		return itemRepository.saveAndFlush(new Item(vila.getId(), st, Qualidade.SIMPLES, nivel, List.of()));
	}

	private ResultActions equipar(Cidadao c, String slot, Item i) throws Exception {
		return mvc.perform(put("/api/jogo/cidadao/" + c.getId() + "/equipamento/" + slot)
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"itemId\":" + i.getId() + "}"));
	}

	private ResultActions desequipar(Cidadao c, String slot) throws Exception {
		return mvc.perform(delete("/api/jogo/cidadao/" + c.getId() + "/equipamento/" + slot)
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()));
	}

	private void pe(Cidadao c, Profissao p, int base) {
		profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), p, base));
	}

	private void erro(ResultActions r, String msg) throws Exception {
		r.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(msg));
	}

	@Test
	void sucessoArmaDevolveMapa() throws Exception {
		novaVila();
		Cidadao c = cidadao(20);
		Item espada = item(ItemSubtipo.ESPADA, 1);
		equipar(c, "ARMA", espada).andExpect(status().isOk())
				.andExpect(jsonPath("$.ARMA.id").value(espada.getId()))
				.andExpect(jsonPath("$.ARMA.nome").value("Espada"))
				.andExpect(jsonPath("$.ARMA.atributoPrincipal.tipo").value("ATAQUE"));
		assertThat(itemRepository.findById(espada.getId()).orElseThrow().getSlot()).isEqualTo(SlotEquipamento.ARMA);
	}

	@Test
	void categoriaVersusSlot() throws Exception {
		novaVila();
		Cidadao c = cidadao(20);
		erro(equipar(c, "FERRAMENTA", item(ItemSubtipo.ESPADA, 1)), "Espada deve ir em slot Arma");
		erro(equipar(c, "CAPACETE", item(ItemSubtipo.PEITORAL, 1)), "Peitoral deve ir em slot Peitoral");
		erro(equipar(c, "COLAR", item(ItemSubtipo.ANEL, 1)), "Anel deve ir em slot Anel");
		erro(equipar(c, "XPTO", item(ItemSubtipo.ANEL, 1)), "Slot inválido: XPTO");
	}

	@Test
	void idadeMinima() throws Exception {
		novaVila();
		Cidadao c = cidadao(13);
		erro(equipar(c, "FERRAMENTA", item(ItemSubtipo.MARTELO, 1)), "Mínimo 14 anos para equipar ferramentas");
		erro(equipar(c, "COLAR", item(ItemSubtipo.COLAR, 1)), "Mínimo 14 anos para equipar joias");
		Cidadao c15 = cidadao(15);
		erro(equipar(c15, "ARMA", item(ItemSubtipo.ESPADA, 1)), "Mínimo 16 anos para equipar armas");
		erro(equipar(c15, "PEITORAL", item(ItemSubtipo.PEITORAL, 1)), "Mínimo 16 anos para equipar armaduras");
		equipar(c15, "COLAR", item(ItemSubtipo.COLAR, 1)).andExpect(status().isOk())
				.andExpect(jsonPath("$.COLAR.atributoPrincipal.tipo").value("VIDA"))
				.andExpect(jsonPath("$.COLAR.atributoPrincipal.valor").value(5));
	}

	@Test
	void peMinimoArmaEArmadura() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		pe(c, Profissao.GUERREIRO, 2);
		erro(equipar(c, "ARMA", item(ItemSubtipo.ESPADA, 5)), "PE efetivo mínimo 4 necessário");
		erro(equipar(c, "PEITORAL", item(ItemSubtipo.PEITORAL, 5)), "PE efetivo mínimo 4 necessário");
		pe(c, Profissao.GUERREIRO, 0);
		profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), Profissao.GUERREIRO).ifPresent(p -> {
			p.setPontosBase(10);
			profissaoRepository.saveAndFlush(p);
		});
		equipar(c, "ARMA", item(ItemSubtipo.ESPADA, 5)).andExpect(status().isOk());
	}

	@Test
	void peBaseFerramenta() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item martelo = item(ItemSubtipo.MARTELO, 4);
		erro(equipar(c, "FERRAMENTA", martelo), "PE base mínimo 3 necessário em CONSTRUTOR");
		pe(c, Profissao.CONSTRUTOR, 3);
		equipar(c, "FERRAMENTA", martelo).andExpect(status().isOk())
				.andExpect(jsonPath("$.FERRAMENTA.atributoPrincipal.tipo").value("PE"))
				.andExpect(jsonPath("$.FERRAMENTA.atributoPrincipal.valor").value(4));
	}

	@Test
	void itemEmAprimoramentoOutraVilaEInexistente() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item i = item(ItemSubtipo.COLAR, 1);
		i.setEmAprimoramento(true);
		itemRepository.saveAndFlush(i);
		erro(equipar(c, "COLAR", i), "Item em aprimoramento");
		Usuario u2 = new Usuario();
		u2.setNome("K");
		u2.setEmail(UUID.randomUUID() + "@teste.com");
		u2.setSenha("x");
		u2 = usuarioRepository.saveAndFlush(u2);
		Vila vila2 = vilaRepository.saveAndFlush(new Vila(u2.getId(), "Outra", 2L, 1));
		Item outra = itemRepository.saveAndFlush(new Item(vila2.getId(), ItemSubtipo.COLAR, Qualidade.SIMPLES, 1,
				List.of()));
		equipar(c, "COLAR", outra).andExpect(status().isNotFound());
		Cidadao fantasma = new Cidadao();
		fantasma.setId(-5L);
		equipar(fantasma, "COLAR", i).andExpect(status().isNotFound());
	}

	@Test
	void trocaDevolveAnteriorAoInventario() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item a = item(ItemSubtipo.COLAR, 1);
		Item b = item(ItemSubtipo.COLAR, 2);
		equipar(c, "COLAR", a).andExpect(status().isOk());
		equipar(c, "COLAR", b).andExpect(status().isOk()).andExpect(jsonPath("$.COLAR.id").value(b.getId()));
		Item antigo = itemRepository.findById(a.getId()).orElseThrow();
		assertThat(antigo.getCidadaoId()).isNull();
		assertThat(antigo.getSlot()).isNull();
	}

	@Test
	void aneisGenericosELimite() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item a1 = item(ItemSubtipo.ANEL, 1);
		Item a2 = item(ItemSubtipo.ANEL, 1);
		Item a3 = item(ItemSubtipo.ANEL, 1);
		equipar(c, "ANEL", a1).andExpect(status().isOk()).andExpect(jsonPath("$.ANEL_1.id").value(a1.getId()));
		equipar(c, "ANEL", a2).andExpect(status().isOk()).andExpect(jsonPath("$.ANEL_2.id").value(a2.getId()));
		erro(equipar(c, "ANEL", a3), "Máximo 2 anéis por pessoa");
		// explícito troca o ocupante
		equipar(c, "ANEL_1", a3).andExpect(status().isOk()).andExpect(jsonPath("$.ANEL_1.id").value(a3.getId()));
		assertThat(itemRepository.findById(a1.getId()).orElseThrow().getCidadaoId()).isNull();
	}

	@Test
	void moverItemEntreSlotsDoMesmoCidadao() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item a = item(ItemSubtipo.ANEL, 1);
		equipar(c, "ANEL_1", a).andExpect(status().isOk());
		equipar(c, "ANEL_2", a).andExpect(status().isOk()).andExpect(jsonPath("$.ANEL_2.id").value(a.getId()))
				.andExpect(jsonPath("$.ANEL_1").doesNotExist());
	}

	@Test
	void itemDeOutroCidadaoRejeitado() throws Exception {
		novaVila();
		Cidadao c1 = cidadao(25);
		Cidadao c2 = cidadao(25);
		Item a = item(ItemSubtipo.COLAR, 1);
		equipar(c1, "COLAR", a).andExpect(status().isOk());
		erro(equipar(c2, "COLAR", a), "Item já equipado por outro cidadão");
	}

	@Test
	void desequipar() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item a = item(ItemSubtipo.COLAR, 1);
		equipar(c, "COLAR", a).andExpect(status().isOk());
		desequipar(c, "COLAR").andExpect(status().isOk()).andExpect(jsonPath("$.COLAR").doesNotExist());
		assertThat(itemRepository.findById(a.getId()).orElseThrow().getCidadaoId()).isNull();
		erro(desequipar(c, "COLAR"), "Slot vazio");
	}

	@Test
	void cidadaoMortoRejeitado() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		c.setVivo(false);
		cidadaoRepository.saveAndFlush(c);
		erro(equipar(c, "COLAR", item(ItemSubtipo.COLAR, 1)), "Cidadão morto não pode equipar itens");
	}

	@Test
	void morteDevolveItensEPainelTrazEquipamento() throws Exception {
		novaVila();
		Cidadao c = cidadao(25);
		Item a = item(ItemSubtipo.COLAR, 1);
		equipar(c, "COLAR", a).andExpect(status().isOk());
		mvc.perform(get("/api/jogo/cidadao/" + c.getId()).with(user(usuario.getEmail()).roles("USER")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.equipamento.COLAR.id").value(a.getId()));
		morteService.morrer(vila, c, "teste", 1);
		Item depois = itemRepository.findById(a.getId()).orElseThrow();
		assertThat(depois.getCidadaoId()).isNull();
		assertThat(depois.getSlot()).isNull();
	}
}
