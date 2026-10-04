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
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaProducao;

@SpringBootTest
@Transactional
class ProducaoFabricasIntegrationTest {

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
	@Autowired RegiaoBonusRepository regiaoBonusRepository;
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
				r.setTipo(TipoRegiao.FLORESTA);
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

	private BigDecimal qtd(Recurso r) {
		return estoqueService.quantidade(vila, r);
	}

	private void estoque(Recurso r, String q) {
		estoqueService.creditar(vila, r, new BigDecimal(q));
	}

	private List<EventoTurno> eventos() {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == TipoEventoTurno.PRODUCAO).toList();
	}

	@Test
	void serrariaComBonusIndustria() {
		regiaoBonusRepository.saveAndFlush(new RegiaoBonus(regiao.getId(), BonusRegiao.INDUSTRIA, 2, 30));
		Construcao c = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		estoque(Recurso.MADEIRA, "10");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.TABUA)).isEqualByComparingTo("3.9");
		assertThat(eventos().get(0).getDados()).containsEntry("bonusRegiao", 30);
	}

	@Test
	void serrariaN1() {
		Construcao c = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		estoque(Recurso.MADEIRA, "6");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.TABUA)).isEqualByComparingTo("3");
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
	}

	@Test
	void serrariaLimitadaPeloInsumoComFracao() {
		Construcao c = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		estoque(Recurso.MADEIRA, "3");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.TABUA)).isEqualByComparingTo("1.5");
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
	}

	@Test
	void olariaSemInsumo() {
		Construcao c = predio(TipoConstrucao.OLARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.CONSTRUTOR);
		estoque(Recurso.ARGILA, "1");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.TIJOLO)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.ARGILA)).isEqualByComparingTo("1");
		assertThat(eventos()).isEmpty();
	}

	@Test
	void fundicaoPadraoFerro() {
		Construcao c = predio(TipoConstrucao.FUNDICAO, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.FERREIRO);
		estoque(Recurso.MINERIO_DE_FERRO, "10");
		estoque(Recurso.CARVAO, "10");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.FERRO)).isEqualByComparingTo("2");
		assertThat(qtd(Recurso.MINERIO_DE_FERRO)).isEqualByComparingTo("6");
		assertThat(qtd(Recurso.CARVAO)).isEqualByComparingTo("8");
	}

	@Test
	void fundicaoN2Aco() {
		Construcao c = predio(TipoConstrucao.FUNDICAO, NivelConstrucao.N2, EstadoConstrucao.ATIVA,
				"{\"producao\":\"ACO\"}");
		trabalhador(c, Profissao.FERREIRO);
		estoque(Recurso.FERRO, "4");
		estoque(Recurso.CARVAO, "5");
		estoque(Recurso.ENXOFRE, "5");
		servico.processarProducaoFabricas(vila, 1);
		// eficiência 1,0 x 1,2 (N2) x 1 ciclo = 1,2 ciclos
		assertThat(qtd(Recurso.ACO)).isEqualByComparingTo("1.2");
		assertThat(qtd(Recurso.FERRO)).isEqualByComparingTo("1.6");
	}

	@Test
	void fundicaoN1IgnoraAco() {
		Construcao c = predio(TipoConstrucao.FUNDICAO, NivelConstrucao.N1, EstadoConstrucao.ATIVA,
				"{\"producao\":\"ACO\"}");
		trabalhador(c, Profissao.FERREIRO);
		estoque(Recurso.FERRO, "4");
		estoque(Recurso.CARVAO, "5");
		estoque(Recurso.ENXOFRE, "5");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.ACO)).isEqualByComparingTo("0");
	}

	@Test
	void cozinhaMultiplosCiclos() {
		Construcao c = predio(TipoConstrucao.COZINHA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.COZINHEIRO);
		estoque(Recurso.GRAOS, "10");
		estoque(Recurso.CARNE, "5");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("10");
		assertThat(qtd(Recurso.GRAOS)).isEqualByComparingTo("6");
		assertThat(qtd(Recurso.CARNE)).isEqualByComparingTo("3");
	}

	@Test
	void tecelagemFibraDepoisLa() {
		Construcao c = predio(TipoConstrucao.TECELAGEM, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.COSTUREIRO);
		estoque(Recurso.FIBRA, "2");
		estoque(Recurso.LA, "10");
		servico.processarProducaoFabricas(vila, 1);
		// 2 ciclos: 1 com Fibra, 1 com Lã
		assertThat(qtd(Recurso.TECIDO)).isEqualByComparingTo("2");
		assertThat(qtd(Recurso.FIBRA)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.LA)).isEqualByComparingTo("8");
	}

	@Test
	void curtume() {
		Construcao c = predio(TipoConstrucao.CURTUME, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.COSTUREIRO);
		estoque(Recurso.COURO, "10");
		estoque(Recurso.SAL, "1");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.COURO_CURTIDO)).isEqualByComparingTo("1");
		assertThat(qtd(Recurso.COURO)).isEqualByComparingTo("8");
	}

	@Test
	void ordemSerrariaAntesDeOlariaDisputandoMadeira() {
		// Olaria criada primeiro (id menor), mas a Serraria deve consumir antes
		Construcao olaria = predio(TipoConstrucao.OLARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		Construcao serraria = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(olaria, Profissao.CONSTRUTOR);
		trabalhador(serraria, Profissao.MADEIREIRO);
		estoque(Recurso.MADEIRA, "6");
		estoque(Recurso.ARGILA, "10");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.TABUA)).isEqualByComparingTo("3");
		assertThat(qtd(Recurso.MADEIRA)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.TIJOLO)).isEqualByComparingTo("0");
	}

	@Test
	void fabricaEmObraOuSemTrabalhadorNaoProduz() {
		Construcao obra = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, null);
		trabalhador(obra, Profissao.MADEIREIRO);
		predio(TipoConstrucao.COZINHA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		estoque(Recurso.MADEIRA, "6");
		estoque(Recurso.GRAOS, "10");
		estoque(Recurso.CARNE, "5");
		servico.processarProducaoFabricas(vila, 1);
		assertThat(qtd(Recurso.TABUA)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("0");
	}

	@Test
	void eventoProducaoFabricaViaEtapa() {
		Construcao c = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(c, Profissao.MADEIREIRO);
		estoque(Recurso.MADEIRA, "6");
		etapa.executar(vila, 4);
		List<EventoTurno> ev = eventos();
		assertThat(ev).hasSize(1);
		assertThat(ev.get(0).getTurno()).isEqualTo(4);
		assertThat(ev.get(0).getDados()).containsEntry("categoria", "FABRICA").containsEntry("ciclos", "3.00");
	}

	@Test
	void coletaAntesDaFabricaNoMesmoTurno() {
		Construcao camp = predio(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(camp, Profissao.MADEIREIRO);
		for (int x = 0; x < 2; x++) {
			marcacaoRepository.save(new ConstrucaoMarcacao(vila.getId(), 1, x, 0, camp.getId()));
		}
		marcacaoRepository.flush();
		Construcao serr = predio(TipoConstrucao.SERRARIA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, null);
		trabalhador(serr, Profissao.MADEIREIRO);
		etapa.executar(vila, 1);
		// 5 Madeira coletadas; 3 ciclos limitados a 2,5 pelo insumo
		assertThat(qtd(Recurso.TABUA)).isEqualByComparingTo("2.5");
	}

}
