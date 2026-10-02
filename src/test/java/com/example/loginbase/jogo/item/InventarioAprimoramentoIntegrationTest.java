package com.example.loginbase.jogo.item;

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
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.etapas.EtapaFabricacao;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventarioAprimoramentoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired EstoqueService estoque;
	@Autowired FabricacaoRepository fabricacaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired PeArtesao peArtesao;
	@Autowired EtapaFabricacao etapa;

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

	private Construcao oficina(TipoConstrucao tipo, NivelConstrucao nivel, EstadoConstrucao estado) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	private Cidadao artesao(Construcao of, Profissao prof, int pe) {
		Cidadao c = cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "A" + (++seq), Sexo.M, 30 * 12));
		c.setConstrucaoId(of.getId());
		c.setProfissaoTrabalho(prof);
		cidadaoRepository.saveAndFlush(c);
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), prof, 0));
		int bonus = peArtesao.peEfetivo(c, prof);
		CidadaoProfissao cp = cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(c.getId(), prof).orElseThrow();
		cp.setPontosBase(pe - bonus);
		cidadaoProfissaoRepository.saveAndFlush(cp);
		return c;
	}

	private Item item(ItemSubtipo s, int nivel) {
		return itemRepository.saveAndFlush(new Item(vila.getId(), s, Qualidade.BOA, nivel,
				List.of(new BonusItem(CodigoBonus.ATK, 3))));
	}

	private void estoque(Recurso r, int q) {
		estoque.creditar(vila, r, BigDecimal.valueOf(q));
	}

	private ResultActions consultar(String url) throws Exception {
		return mvc.perform(get(url).with(user(usuario.getEmail()).roles("USER")));
	}

	private ResultActions aprimorar(Long itemId, Long oficinaId, Long artesaoId) throws Exception {
		return mvc.perform(post("/api/jogo/inventario/" + itemId + "/aprimorar")
				.with(user(usuario.getEmail()).roles("USER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"oficinaId\":%d,\"artesaoId\":%d}".formatted(oficinaId, artesaoId)));
	}

	@Test
	void listagemPaginacaoFiltroEDetalhe() throws Exception {
		novaVila();
		Item a = item(ItemSubtipo.ESPADA, 1);
		item(ItemSubtipo.ESPADA, 2);
		item(ItemSubtipo.ARCO, 1);
		Item equipado = item(ItemSubtipo.ESPADA, 3);
		Cidadao c = cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "X", Sexo.M, 30 * 12));
		equipado.setCidadaoId(c.getId());
		equipado.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(equipado);
		a.setEmAprimoramento(true);
		itemRepository.saveAndFlush(a);

		consultar("/api/jogo/inventario").andExpect(status().isOk()).andExpect(jsonPath("$.total").value(3))
				.andExpect(jsonPath("$.itens", hasSize(3))).andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.pageSize").value(20)).andExpect(jsonPath("$.itens[0].emAprimoramento").value(true));
		consultar("/api/jogo/inventario?size=2&page=1").andExpect(jsonPath("$.itens", hasSize(1)))
				.andExpect(jsonPath("$.total").value(3)).andExpect(jsonPath("$.page").value(1));
		consultar("/api/jogo/inventario?categoria=ARMA").andExpect(jsonPath("$.total").value(3));
		consultar("/api/jogo/inventario?categoria=JOIA").andExpect(jsonPath("$.total").value(0));
		consultar("/api/jogo/inventario/" + a.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.item.id").value(a.getId())).andExpect(jsonPath("$.pedras", hasSize(0)));
		consultar("/api/jogo/inventario/999999").andExpect(status().isNotFound());
	}

	@Test
	void aprimoramentoFelizDebitaMetadeArredondadaParaCima() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		Item it = item(ItemSubtipo.ESPADA, 1);
		estoque(Recurso.FERRO, 100);
		estoque(Recurso.TABUA, 100);
		var base = new com.example.loginbase.jogo.item.CatalogoItens().de(ItemSubtipo.ESPADA).orElseThrow()
				.receitaBase();
		var custo = RegrasFabricacao.custoAprimoramento(base, 2);
		aprimorar(it.getId(), f.getId(), a.getId()).andExpect(status().isCreated())
				.andExpect(jsonPath("$.nivel").value(2)).andExpect(jsonPath("$.pfTotal").value(3))
				.andExpect(jsonPath("$.estado").value("EM_ANDAMENTO")).andExpect(jsonPath("$.itemId").value(it.getId()));
		custo.forEach((r, q) -> assertThat(estoque.quantidade(vila, r).intValue()).isEqualTo(100 - q));
		assertThat(itemRepository.findById(it.getId()).orElseThrow().isEmAprimoramento()).isTrue();
		Fabricacao fab = fabricacaoRepository.findByArtesaoId(a.getId()).orElseThrow();
		assertThat(fab.getPfAtual().signum()).isZero();
		// já em aprimoramento
		Cidadao b = artesao(f, Profissao.FERREIRO, 10);
		aprimorar(it.getId(), f.getId(), b.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Item já em aprimoramento"));
	}

	@Test
	void validacoes() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Construcao carp = oficina(TipoConstrucao.CARPINTARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA);
		Construcao inativa = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N3, EstadoConstrucao.EM_OBRA);
		Construcao f3 = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N3, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		Cidadao fraco = artesao(f3, Profissao.FERREIRO, 3);
		Cidadao outra = artesao(carp, Profissao.MADEIREIRO, 10);
		Cidadao ai = artesao(inativa, Profissao.FERREIRO, 20);
		Item l1 = item(ItemSubtipo.ESPADA, 1);

		aprimorar(999999L, f.getId(), a.getId()).andExpect(status().isNotFound());
		aprimorar(l1.getId(), 999999L, a.getId()).andExpect(status().isNotFound());
		aprimorar(l1.getId(), carp.getId(), outra.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Oficina incorreta. Espada é fabricada na Ferraria."));
		aprimorar(l1.getId(), inativa.getId(), ai.getId()).andExpect(status().isBadRequest());
		aprimorar(l1.getId(), f.getId(), outra.getId()).andExpect(status().isBadRequest());
		// artesão morto
		Cidadao morto = artesao(f, Profissao.FERREIRO, 10);
		morto.setVivo(false);
		cidadaoRepository.saveAndFlush(morto);
		aprimorar(l1.getId(), f.getId(), morto.getId()).andExpect(status().isBadRequest());
		// recursos insuficientes
		estoque(Recurso.FERRO, 0);
		aprimorar(l1.getId(), f.getId(), a.getId()).andExpect(status().isConflict());

		// nível máximo da oficina
		Item l3 = item(ItemSubtipo.ESPADA, 3);
		aprimorar(l3.getId(), f.getId(), a.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Nível máximo L3 para esta oficina"));
		// PE
		Item l5 = item(ItemSubtipo.ESPADA, 5);
		aprimorar(l5.getId(), f3.getId(), fraco.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("PE efetivo mínimo 10 necessário para L6"));
		// L10
		Item l10 = item(ItemSubtipo.ESPADA, 10);
		aprimorar(l10.getId(), f3.getId(), fraco.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Nível máximo L10 atingido"));
		// equipado
		Item eq = item(ItemSubtipo.ESPADA, 1);
		eq.setCidadaoId(a.getId());
		eq.setSlot(SlotEquipamento.ARMA);
		itemRepository.saveAndFlush(eq);
		aprimorar(eq.getId(), f.getId(), a.getId()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Desequipe o item antes de aprimorar"));
	}

	@Test
	void cicloCompletoRecalculaMagnitudeAoCruzarFaixa() throws Exception {
		novaVila();
		Construcao f = oficina(TipoConstrucao.FERRARIA, NivelConstrucao.N2, EstadoConstrucao.ATIVA);
		Cidadao a = artesao(f, Profissao.FERREIRO, 10);
		Item it = item(ItemSubtipo.ESPADA, 4);
		estoque(Recurso.FERRO, 100);
		estoque(Recurso.TABUA, 100);
		aprimorar(it.getId(), f.getId(), a.getId()).andExpect(status().isCreated())
				.andExpect(jsonPath("$.nivel").value(5)).andExpect(jsonPath("$.pfTotal").value(6));
		int t = 1;
		etapa.executar(vila, t);
		while (fabricacaoRepository.findByArtesaoId(a.getId()).isPresent() && t < 30) {
			etapa.executar(vila, ++t);
		}
		Item r = itemRepository.findById(it.getId()).orElseThrow();
		assertThat(r.getNivel()).isEqualTo(5);
		assertThat(r.isEmAprimoramento()).isFalse();
		assertThat(r.getBonus()).containsExactly(new BonusItem(CodigoBonus.ATK, 5));
		assertThat(r.getQualidade()).isEqualTo(Qualidade.BOA);
	}
}
