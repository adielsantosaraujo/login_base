package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.batalha.Batalha;
import com.example.loginbase.jogo.batalha.BatalhaExpedicaoService;
import com.example.loginbase.jogo.batalha.Combatente;
import com.example.loginbase.jogo.batalha.FonteMasmorras;
import com.example.loginbase.jogo.batalha.LadoCombate;
import com.example.loginbase.jogo.batalha.LinhaCombate;
import com.example.loginbase.jogo.batalha.MasmorraAlvo;
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
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.item.catalogo.Alcance;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.pedra.PedraRepository;
import com.example.loginbase.jogo.quartel.PosicaoTropa;
import com.example.loginbase.jogo.quartel.Tropa;
import com.example.loginbase.jogo.quartel.TropaRepository;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@Transactional
class RecompensasMasmorraIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired TropaRepository tropaRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired PedraRepository pedraRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired BatalhaExpedicaoService servico;
	@MockitoBean FonteMasmorras fonte;

	private Vila vila;
	private Familia familia;
	private Construcao quartel;
	private int seq;

	private void cenario() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.QUARTEL);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(0);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(8);
		c.setPoAtual(8);
		quartel = construcaoRepository.saveAndFlush(c);
	}

	private Tropa tropa(int membros) {
		Tropa t = tropaRepository.saveAndFlush(new Tropa(vila.getId(), quartel.getId(), "T" + (++seq)));
		for (int i = 0; i < membros; i++) {
			Cidadao c = new Cidadao(vila.getId(), familia.getId(), "G" + (++seq), Sexo.M, 20 * 12);
			c.setVit(5);
			c.setForca(5);
			c.setVel(5);
			c.setTropaId(t.getId());
			c.setPosicaoTropa(i % 2 == 0 ? PosicaoTropa.FRENTE : PosicaoTropa.RETAGUARDA);
			c = cidadaoRepository.saveAndFlush(c);
			profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.GUERREIRO, 5));
			Item espada = new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.SIMPLES, 1, List.of());
			espada.setCidadaoId(c.getId());
			espada.setSlot(SlotEquipamento.ARMA);
			itemRepository.saveAndFlush(espada);
		}
		return t;
	}

	private Combatente inimigo(int pv, double atq) {
		return new Combatente(0, LadoCombate.INIMIGO, "Inimigo", LinhaCombate.FRENTE, pv, atq, 0, 1, 0, 0,
				Alcance.CORPO_A_CORPO_FRENTE, false);
	}

	private BigDecimal qtd(Recurso r) {
		return estoqueService.quantidade(vila, r);
	}

	@Test
	@SuppressWarnings("unchecked")
	void vitoriaEntregaTudoEGravaJson() {
		cenario();
		Tropa t = tropa(3);
		int nivel = 6;
		when(fonte.gerarInimigos(anyInt(), anyLong())).thenReturn(List.of(inimigo(5, 0)));
		BigDecimal ouroAntes = qtd(Recurso.OURO);
		Map<Recurso, BigDecimal> antes = estoqueService.listar(vila);

		Batalha b = servico.resolver(t, new MasmorraAlvo(90L, 1, nivel), 10);

		Map<String, Object> rec = b.getRecompensas();
		assertThat(rec).isNotNull();
		int ouro = (Integer) rec.get("ouro");
		assertThat(ouro).isBetween(240, 360);
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo(ouroAntes.add(BigDecimal.valueOf(ouro)));
		Map<String, Object> recursos = (Map<String, Object>) rec.get("recursos");
		int soma = 0;
		for (var e : recursos.entrySet()) {
			Recurso r = Recurso.valueOf(e.getKey());
			int v = (Integer) e.getValue();
			soma += v;
			BigDecimal base = antes.getOrDefault(r, BigDecimal.ZERO);
			assertThat(qtd(r)).isEqualByComparingTo(base.add(BigDecimal.valueOf(v)));
		}
		assertThat(soma).isEqualTo(360);

		Map<String, Object> item = (Map<String, Object>) rec.get("item");
		long semItem = itemRepository.findByVilaId(vila.getId()).stream().filter(i -> i.getCidadaoId() == null).count();
		assertThat(semItem).isEqualTo(item == null ? 0 : 1);
		if (item != null) {
			Item salvo = itemRepository.findById(((Number) item.get("itemId")).longValue()).orElseThrow();
			assertThat(salvo.getVilaId()).isEqualTo(vila.getId());
			assertThat(salvo.getNivel()).isEqualTo(nivel);
		}

		List<Map<String, Object>> pedras = (List<Map<String, Object>>) rec.get("pedras");
		assertThat(pedraRepository.findByVilaIdAndItemIdIsNullOrderByIdAsc(vila.getId())).hasSize(pedras.size());

		assertThat(rec.get("xpPorGuerreiro")).isEqualTo(6);
		List<Long> xp = (List<Long>) rec.get("guerreirosXp");
		assertThat(xp).hasSize(3);
		for (Long id : xp) {
			assertThat(cidadaoRepository.findById(id).orElseThrow().getXpGuerreiro()).isEqualByComparingTo("6");
		}
		assertThat(eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(vila.getId(), 10))
				.anyMatch(e -> e.getTipo() == TipoEventoTurno.RECOMPENSA_MASMORRA);
	}

	@Test
	void derrotaNaoEntregaNada() {
		cenario();
		Tropa t = tropa(4);
		when(fonte.gerarInimigos(anyInt(), anyLong())).thenReturn(List.of(inimigo(1_000_000, 100_000)));
		BigDecimal ouroAntes = qtd(Recurso.OURO);
		long itensAntes = itemRepository.findByVilaId(vila.getId()).size();

		Batalha b = servico.resolver(t, new MasmorraAlvo(91L, 1, 6), 10);

		assertThat(b.getRecompensas()).isNull();
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo(ouroAntes);
		assertThat(pedraRepository.findByVilaIdAndItemIdIsNullOrderByIdAsc(vila.getId())).isEmpty();
		assertThat(itemRepository.findByVilaId(vila.getId()).size()).isLessThanOrEqualTo((int) itensAntes);
		assertThat(eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(vila.getId(), 10))
				.noneMatch(e -> e.getTipo() == TipoEventoTurno.RECOMPENSA_MASMORRA);
		cidadaoRepository.findAll().stream().filter(c -> vila.getId().equals(c.getVilaId()))
				.forEach(c -> assertThat(c.getXpGuerreiro()).isEqualByComparingTo("0"));
	}
}
