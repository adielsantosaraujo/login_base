package com.example.loginbase.jogo.recurso;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

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
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaOuroPassivo;

@SpringBootTest
@Transactional
class OuroServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository cidadaoProfissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired OuroService servico;
	@Autowired EtapaOuroPassivo etapa;
	@Autowired EventoTurnoRepository eventoRepository;

	private Vila vila;
	private Familia familia;
	private int seq;

	private void preparar(String refeicao) {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		estoqueService.inicializar(vila, Map.of(Recurso.REFEICAO, new BigDecimal(refeicao)));
	}

	private Cidadao cidadao(int anos) {
		return cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, anos * 12));
	}

	private Construcao estalagem(NivelConstrucao nivel) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.ESTALAGEM);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(4);
		c.setPoAtual(4);
		return construcaoRepository.saveAndFlush(c);
	}

	private void cozinheiro(Construcao e, int pe) {
		Cidadao c = cidadao(30);
		cidadaoProfissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.COZINHEIRO, pe));
		c.setConstrucaoId(e.getId());
		c.setProfissaoTrabalho(Profissao.COZINHEIRO);
		cidadaoRepository.saveAndFlush(c);
	}

	private BigDecimal qtd(Recurso r) {
		return estoqueService.quantidade(vila, r);
	}

	private long eventos(TipoEventoTurno t) {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == t).count();
	}

	@Test
	void impostoBasico() {
		preparar("0");
		for (int i = 0; i < 10; i++) cidadao(30);
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("5");
	}

	@Test
	void impostoSemAdultos() {
		preparar("0");
		cidadao(5);
		cidadao(17);
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("0");
	}

	@Test
	void estalagemN1ServeCincoRefeicoes() {
		preparar("5");
		cozinheiro(estalagem(NivelConstrucao.N1), 5); // eficiência 1,0
		long adultos = 1;
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo(String.valueOf(20 + adultos * 0.5));
	}

	@Test
	void estalagemSemRefeicao() {
		preparar("0");
		cozinheiro(estalagem(NivelConstrucao.N1), 5);
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("0.5"); // só imposto
	}

	@Test
	void estalagemN2Multiplicador() {
		preparar("30");
		Construcao e = estalagem(NivelConstrucao.N2);
		cozinheiro(e, 5);
		cozinheiro(e, 5); // eficiência 1,0 x 1,2 cada -> 5 x 2,4 = 12
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("18");
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("49"); // 48 + imposto 1
	}

	@Test
	void estalagemRefeicoesInsuficientes() {
		preparar("3");
		Construcao e = estalagem(NivelConstrucao.N1);
		cozinheiro(e, 5);
		cozinheiro(e, 5); // capacidade 10, só 3
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("0");
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("13"); // 12 + imposto 1
	}

	@Test
	void estalagemArredondaCapacidadeParaBaixo() {
		preparar("30");
		Construcao e = estalagem(NivelConstrucao.N2);
		cozinheiro(e, 6);
		cozinheiro(e, 6); // 2 x 1,1 x 1,2 = 2,64 -> capacidade 13,2 -> 13
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("17");
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("53"); // 52 + imposto 1
	}

	@Test
	void estalagemSemTrabalhadores() {
		preparar("30");
		estalagem(NivelConstrucao.N1);
		cidadao(30);
		servico.processarOuroPassivo(vila, 1);
		assertThat(qtd(Recurso.REFEICAO)).isEqualByComparingTo("30");
		assertThat(qtd(Recurso.OURO)).isEqualByComparingTo("0.5");
		assertThat(eventos(TipoEventoTurno.IMPOSTO_COBRADO)).isEqualTo(1);
	}

	@Test
	void eventosRegistradosViaEtapa() {
		preparar("5");
		cozinheiro(estalagem(NivelConstrucao.N1), 5);
		assertThat(etapa.ordem()).isEqualTo(2);
		etapa.executar(vila, 1);
		assertThat(eventos(TipoEventoTurno.IMPOSTO_COBRADO)).isEqualTo(1);
		assertThat(eventos(TipoEventoTurno.ESTALAGEM_RECEITA)).isEqualTo(1);
	}

	@Test
	void semEstalagemNaoGeraEventoDeReceita() {
		preparar("5");
		cidadao(30);
		servico.processarOuroPassivo(vila, 1);
		assertThat(eventos(TipoEventoTurno.ESTALAGEM_RECEITA)).isZero();
	}

}
