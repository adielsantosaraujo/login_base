package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaFabricacao;

@SpringBootTest
@Transactional
class FabricacaoTurnoIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired FabricacaoRepository fabricacaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired ConsultaTrabalhadores consulta;
	@Autowired ItemBonusGerador gerador;
	@Autowired EtapaFabricacao etapa;

	private Vila vila;
	private Familia familia;
	private int seq;

	private void novaVila() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
	}

	private Construcao oficina(EstadoConstrucao estado) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.FERRARIA);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(1);
		c.setPoAtual(1);
		return construcaoRepository.saveAndFlush(c);
	}

	private Cidadao artesao(Construcao o) {
		Cidadao c = cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "A" + (++seq), Sexo.M, 30 * 12));
		c.setConstrucaoId(o.getId());
		c.setProfissaoTrabalho(Profissao.FERREIRO);
		return cidadaoRepository.saveAndFlush(c);
	}

	private Fabricacao fab(Construcao o, Cidadao a, int nivel, int pfTotal) {
		return fabricacaoRepository.saveAndFlush(
				new Fabricacao(vila.getId(), o.getId(), a.getId(), ItemSubtipo.ESPADA, nivel, pfTotal, 1));
	}

	private Fabricacao recarrega(Fabricacao f) {
		return fabricacaoRepository.findById(f.getId()).orElseThrow();
	}

	private long eventos(TipoEventoTurno tipo) {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == tipo).count();
	}

	private int rodaAteConcluir(Fabricacao f, int turnoInicial) {
		int t = turnoInicial;
		etapa.executar(vila, t);
		while (fabricacaoRepository.findById(f.getId()).isPresent() && t < turnoInicial + 10) {
			etapa.executar(vila, ++t);
		}
		return t;
	}

	private double eficiencia(Construcao o) {
		return consulta.trabalhadores(o, vila).stream().mapToDouble(t -> t.eficiencia()).sum();
	}

	@Test
	void avancaPorEficiencia() {
		novaVila();
		Construcao o = oficina(EstadoConstrucao.ATIVA);
		Cidadao a = artesao(o);
		Fabricacao f = fab(o, a, 1, 1000);
		double ef = eficiencia(o);
		assertThat(ef).isGreaterThan(0);
		etapa.executar(vila, 1);
		etapa.executar(vila, 2);
		assertThat(recarrega(f).getPfAtual().doubleValue()).isEqualTo(2 * ef, org.assertj.core.data.Offset.offset(1e-3));
	}

	@Test
	void conclusaoCriaItemDeterministico() {
		novaVila();
		Construcao o = oficina(EstadoConstrucao.ATIVA);
		Cidadao a = artesao(o);
		Fabricacao f = fab(o, a, 1, 1);
		f.setAtributoEscolhido(CodigoBonus.FOR);
		fabricacaoRepository.saveAndFlush(f);
		Long id = f.getId();
		int turno = rodaAteConcluir(f, 7);
		assertThat(fabricacaoRepository.findById(id)).isEmpty();
		List<Item> itens = itemRepository.findByVilaId(vila.getId());
		assertThat(itens).hasSize(1);
		Item it = itens.get(0);
		assertThat(it.getCidadaoId()).isNull();
		assertThat(it.getSlot()).isNull();
		assertThat(it.getSubtipo()).isEqualTo(ItemSubtipo.ESPADA);
		assertThat(it.getAtributoEscolhido()).isEqualTo(CodigoBonus.FOR);
		assertThat(it.getBonus()).hasSize(it.getQualidade().getQtdBonus());
		long semente = id * 1_000_003L + turno;
		assertThat(it.getBonus()).isEqualTo(
				gerador.gerarBonusIntrinsecos(it.getQualidade(), ItemCategoria.ARMA, 1, semente * 31 + 7));
		assertThat(eventos(TipoEventoTurno.FABRICACAO_CONCLUIDA)).isEqualTo(1);
	}

	@Test
	void pausaPorArtesaoMortoEEventoSoNaTransicao() {
		novaVila();
		Construcao o = oficina(EstadoConstrucao.ATIVA);
		Cidadao a = artesao(o);
		Fabricacao f = fab(o, a, 1, 1000);
		a.setVivo(false);
		cidadaoRepository.saveAndFlush(a);
		etapa.executar(vila, 1);
		etapa.executar(vila, 2);
		Fabricacao r = recarrega(f);
		assertThat(r.getEstado()).isEqualTo(EstadoFabricacao.PAUSADA);
		assertThat(r.getPfAtual()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(eventos(TipoEventoTurno.FABRICACAO_PAUSADA)).isEqualTo(1);
	}

	@Test
	void pausaPorArtesaoDesalocadoERetomadaAposReatribuicao() {
		novaVila();
		Construcao o = oficina(EstadoConstrucao.ATIVA);
		Cidadao a = artesao(o);
		Fabricacao f = fab(o, a, 1, 1000);
		a.setConstrucaoId(null);
		a.setProfissaoTrabalho(null);
		cidadaoRepository.saveAndFlush(a);
		etapa.executar(vila, 1);
		assertThat(recarrega(f).getEstado()).isEqualTo(EstadoFabricacao.PAUSADA);
		Cidadao b = artesao(o);
		Fabricacao x = recarrega(f);
		x.setArtesaoId(b.getId());
		fabricacaoRepository.saveAndFlush(x);
		etapa.executar(vila, 2);
		Fabricacao r = recarrega(f);
		assertThat(r.getEstado()).isEqualTo(EstadoFabricacao.EM_ANDAMENTO);
		assertThat(r.getPfAtual().doubleValue()).isGreaterThan(0);
	}

	@Test
	void pausaPorOficinaNaoAtiva() {
		novaVila();
		Construcao o = oficina(EstadoConstrucao.EM_UPGRADE);
		Cidadao a = artesao(o);
		Fabricacao f = fab(o, a, 1, 1000);
		etapa.executar(vila, 1);
		etapa.executar(vila, 2);
		assertThat(recarrega(f).getEstado()).isEqualTo(EstadoFabricacao.PAUSADA);
		assertThat(recarrega(f).getPfAtual()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(eventos(TipoEventoTurno.FABRICACAO_PAUSADA)).isEqualTo(1);
	}

	@Test
	void aprimoramentoSobeNivelERecalculaMagnitudes() {
		novaVila();
		Construcao o = oficina(EstadoConstrucao.ATIVA);
		Cidadao a = artesao(o);
		Item item = new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.BOA, 1,
				List.of(new BonusItem(CodigoBonus.ATK, 3), new BonusItem(CodigoBonus.FOR, 1)));
		item.setEmAprimoramento(true);
		item = itemRepository.saveAndFlush(item);
		Fabricacao f = fab(o, a, 10, 1);
		f.setItemId(item.getId());
		fabricacaoRepository.saveAndFlush(f);
		rodaAteConcluir(f, 3);
		Item r = itemRepository.findById(item.getId()).orElseThrow();
		assertThat(r.getNivel()).isEqualTo(10);
		assertThat(r.isEmAprimoramento()).isFalse();
		assertThat(r.getQualidade()).isEqualTo(Qualidade.BOA);
		assertThat(r.getBonus()).isEqualTo(gerador.recalcularMagnitudes(
				List.of(new BonusItem(CodigoBonus.ATK, 3), new BonusItem(CodigoBonus.FOR, 1)), 10));
		assertThat(fabricacaoRepository.findById(f.getId())).isEmpty();
		assertThat(eventos(TipoEventoTurno.APRIMORAMENTO_CONCLUIDO)).isEqualTo(1);
	}
}
