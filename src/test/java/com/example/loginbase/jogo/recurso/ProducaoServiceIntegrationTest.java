package com.example.loginbase.jogo.recurso;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.example.loginbase.jogo.construcao.ConstrucaoMarcacao;
import com.example.loginbase.jogo.construcao.ConstrucaoMarcacaoRepository;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Jazida;
import com.example.loginbase.jogo.modelo.LadrilhoJazida;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaProducao;

@SpringBootTest
@Transactional
class ProducaoServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired LadrilhoJazidaRepository ladrilhoJazidaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ConstrucaoMarcacaoRepository marcacaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired ProducaoService servico;
	@Autowired EtapaProducao etapa;
	@Autowired EventoTurnoRepository eventoRepository;

	Vila vila;
	Regiao regiao;
	Familia familia;

	@BeforeEach
	void preparar() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 7L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i == 1) {
				r.setPossuida(true);
				r.setTipo(TipoRegiao.COLETA);
			}
			r = regiaoRepository.save(r);
			if (i == 1) {
				regiao = r;
			}
		}
		regiaoRepository.flush();
		for (int x = 0; x < 10; x++) {
			ladrilhoJazidaRepository.save(new LadrilhoJazida(regiao.getId(), x, 0, Jazida.FLORESTA));
			ladrilhoJazidaRepository.save(new LadrilhoJazida(regiao.getId(), x, 1, Jazida.ROCHA));
		}
		ladrilhoJazidaRepository.flush();
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		estoqueService.inicializar(vila, java.util.Map.of());
	}

	private Construcao predio(TipoConstrucao tipo, NivelConstrucao nivel, EstadoConstrucao estado, String config) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(5);
		c.setY(5);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(4);
		c.setConfiguracao(config);
		return construcaoRepository.saveAndFlush(c);
	}

	private void trabalhador(Construcao c, Profissao p) {
		Cidadao ci = new Cidadao(vila.getId(), familia.getId(), "T", Sexo.M, 30 * 12);
		ci.setConstrucaoId(c.getId());
		ci.setProfissaoTrabalho(p);
		ci = cidadaoRepository.saveAndFlush(ci);
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(ci.getId(), p, 5));
	}

	private void marcar(Construcao c, int quantidade, int y) {
		for (int x = 0; x < quantidade; x++) {
			marcacaoRepository.save(new ConstrucaoMarcacao(vila.getId(), 1, x, y, c.getId()));
		}
		marcacaoRepository.flush();
	}

	private BigDecimal qtd(Recurso r) {
		return estoqueService.quantidade(vila, r);
	}

	private List<EventoTurno> eventos() {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == TipoEventoTurno.PRODUCAO).toList();
	}

	@Test
	void acampamentoDoisMadeireirosQuatroMarcados() {
		Construcao c = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		trabalhador(c, Profissao.MADEIREIRO);
		marcar(c, 4, 0);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("10");
	}

	@Test
	void fazendaPlantioN2UmAgricultor() {
		Construcao c = predio(TipoConstrucao.FAZENDA_PLANTIO, NivelConstrucao.N2, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.AGRICULTOR);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.GRAOS)).isEqualByComparingTo("7.2");
		assertThat(eventos().get(0).getDados()).containsEntry("categoria", "RURAL");
	}

	@Test
	void fazendaPlantioFibra() {
		Construcao c = predio(TipoConstrucao.FAZENDA_PLANTIO, NivelConstrucao.N1, EstadoConstrucao.ATIVA,
				"{\"cultura\":\"FIBRA\"}");
		trabalhador(c, Profissao.AGRICULTOR);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.FIBRA)).isEqualByComparingTo("4");
		assertThat(qtd(Recurso.GRAOS)).isEqualByComparingTo("0");
	}

	@Test
	void fazendaCriacaoPadraoGadoEOvelhas() {
		Construcao gado = predio(TipoConstrucao.FAZENDA_CRIACAO, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(gado, Profissao.FAZENDEIRO);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.CARNE)).isEqualByComparingTo("3");
		assertThat(qtd(Recurso.COURO)).isEqualByComparingTo("1");
		Construcao ovelhas = predio(TipoConstrucao.FAZENDA_CRIACAO, NivelConstrucao.N1, EstadoConstrucao.ATIVA,
				"{\"rebanho\":\"OVELHAS\"}");
		trabalhador(ovelhas, Profissao.FAZENDEIRO);
		servico.processarProducaoColataRural(vila, 2);
		assertThat(qtd(Recurso.LA)).isEqualByComparingTo("2");
		assertThat(qtd(Recurso.CARNE)).isEqualByComparingTo("7");
	}

	@Test
	void semLadrilhosMarcadosNaoProduz() {
		Construcao c = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
		assertThat(eventos()).isEmpty();
	}

	@Test
	void produtivosLimitadosPelosMarcados() {
		Construcao c = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		for (int i = 0; i < 4; i++) {
			trabalhador(c, Profissao.MADEIREIRO);
		}
		marcar(c, 3, 0); // floor(3/2) = 1
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("5");
	}

	@Test
	void marcacaoComJazidaErradaNaoConta() {
		Construcao c = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		marcar(c, 4, 1); // linha y=1 é Rocha
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
	}

	@Test
	void predioEmObraNaoProduz() {
		Construcao c = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		trabalhador(c, Profissao.MADEIREIRO);
		marcar(c, 4, 0);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
	}

	@Test
	void cabanaDeCacaProduzCarneECouro() {
		Construcao c = predio(TipoConstrucao.CABANA_CACA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.CACADOR);
		marcar(c, 2, 0);
		servico.processarProducaoColataRural(vila, 1);
		assertThat(qtd(Recurso.CARNE)).isEqualByComparingTo("2");
		assertThat(qtd(Recurso.COURO)).isEqualByComparingTo("1");
	}

	@Test
	void eventoProducaoViaEtapa() {
		Construcao c = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		marcar(c, 2, 0);
		etapa.executar(vila, 3);
		List<EventoTurno> ev = eventos();
		assertThat(ev).hasSize(1);
		assertThat(ev.get(0).getTurno()).isEqualTo(3);
		assertThat(ev.get(0).getDados()).containsEntry("categoria", "COLETA");
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("5");
	}

}
