package com.example.loginbase.jogo.batalha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
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
import com.example.loginbase.jogo.quartel.PosicaoTropa;
import com.example.loginbase.jogo.quartel.Tropa;
import com.example.loginbase.jogo.quartel.TropaRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EtapaTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

@SpringBootTest
@Transactional
class BatalhaExpedicaoIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired TropaRepository tropaRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired BatalhaRepository batalhaRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired BatalhaExpedicaoService servico;
	@Autowired List<EtapaTurno> etapas;
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

	private Combatente inimigo(int indice, int pv, double atq) {
		return new Combatente(indice, LadoCombate.INIMIGO, "Inimigo" + indice, LinhaCombate.FRENTE, pv, atq, 0, 1, 0,
				0, Alcance.CORPO_A_CORPO_FRENTE, false);
	}

	@Test
	void vitoriaGravaBatalhaEvento() {
		cenario();
		Tropa t = tropa(2);
		MasmorraAlvo alvo = new MasmorraAlvo(77L, 3, 2);
		when(fonte.gerarInimigos(eq(2), anyLong())).thenReturn(List.of(inimigo(0, 5, 0)));

		Batalha b = servico.resolver(t, alvo, 10);

		assertThat(b.getId()).isNotNull();
		assertThat(b.getResultado()).isEqualTo(ResultadoCombate.VITORIA);
		assertThat(b.getRecompensas()).isNull();
		assertThat(b.getMasmorraNivel()).isEqualTo(2);
		assertThat(b.getRegiaoIndice()).isEqualTo(3);
		assertThat(b.getTropaNome()).isEqualTo(t.getNome());
		assertThat(b.getSemente()).isEqualTo(BatalhaExpedicaoService.semente(vila.getId(), t.getId(), 10));
		Batalha lida = batalhaRepository.findById(b.getId()).orElseThrow();
		assertThat(lida.getLog().acoes()).isNotEmpty();
		assertThat(lida.getLog().participantes()).hasSize(3);
		assertThat(tropaRepository.findById(t.getId()).orElseThrow().getUltimaBatalhaId()).isEqualTo(b.getId());
		verify(fonte).registrarResultado(77L, true, 10);
		assertThat(eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(vila.getId(), 10))
				.anyMatch(e -> e.getTipo() == TipoEventoTurno.BATALHA);
	}

	@Test
	void derrotaMortosPerdemItensEFeridosContinuamNaTropa() {
		cenario();
		Tropa t = tropa(8);
		MasmorraAlvo alvo = new MasmorraAlvo(78L, 3, 5);
		when(fonte.gerarInimigos(eq(5), anyLong())).thenReturn(List.of(inimigo(0, 1_000_000, 100_000)));

		Batalha b = servico.resolver(t, alvo, 12);

		assertThat(b.getResultado()).isEqualTo(ResultadoCombate.DERROTA);
		verify(fonte).registrarResultado(78L, false, 12);
		List<Cidadao> todos = cidadaoRepository.findAll().stream().filter(c -> vila.getId().equals(c.getVilaId()))
				.toList();
		assertThat(todos).hasSize(8);
		for (Cidadao c : todos) {
			var itens = itemRepository.findByCidadaoId(c.getId());
			if (!c.isVivo()) {
				assertThat(itens).isEmpty();
				assertThat(c.getTropaId()).isNull();
			} else {
				assertThat(c.getEstado()).isEqualTo(EstadoCidadao.FERIDO);
				assertThat(c.getFeridoAteTurno()).isEqualTo(18);
				assertThat(c.getTropaId()).isEqualTo(t.getId());
				assertThat(itens).hasSize(1);
			}
		}
		// itens de mortos saíram do jogo (não voltaram ao inventário da vila)
		long mortos = todos.stream().filter(c -> !c.isVivo()).count();
		assertThat(itemRepository.findByVilaId(vila.getId())).hasSize((int) (8 - mortos));
	}

	@Test
	void recuperacaoNoPipelineDoTurno() {
		cenario();
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), "Ferido", Sexo.M, 20 * 12);
		c.setEstado(EstadoCidadao.FERIDO);
		c.setFeridoAteTurno(8);
		c = cidadaoRepository.saveAndFlush(c);
		EtapaTurno etapa = etapas.stream().filter(e -> e.ordem() == 11).findFirst().orElseThrow();

		etapa.executar(vila, 7);
		assertThat(cidadaoRepository.findById(c.getId()).orElseThrow().getEstado()).isEqualTo(EstadoCidadao.FERIDO);

		etapa.executar(vila, 8);
		Cidadao rec = cidadaoRepository.findById(c.getId()).orElseThrow();
		assertThat(rec.getEstado()).isEqualTo(EstadoCidadao.SAUDAVEL);
		assertThat(rec.getFeridoAteTurno()).isNull();
		assertThat(eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(vila.getId(), 8))
				.anyMatch(e -> e.getTipo() == TipoEventoTurno.RECUPERADO);
	}
}
