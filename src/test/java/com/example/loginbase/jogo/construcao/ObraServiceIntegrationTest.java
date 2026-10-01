package com.example.loginbase.jogo.construcao;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaObras;

@SpringBootTest
@Transactional
class ObraServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ConsultaTrabalhadores consulta;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired EtapaObras etapa;

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

	private Construcao obra(TipoConstrucao tipo, NivelConstrucao nivel, EstadoConstrucao estado, int po) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(po);
		c.setPoAtual(0);
		return construcaoRepository.saveAndFlush(c);
	}

	private Cidadao aloca(Construcao obra, Profissao p) {
		Cidadao c = cidadaoRepository.saveAndFlush(
				new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, 30 * 12));
		c.setConstrucaoId(obra.getId());
		c.setProfissaoTrabalho(p);
		return cidadaoRepository.saveAndFlush(c);
	}

	private double ganhoEsperado(Construcao obra) {
		return consulta.trabalhadores(obra, vila).stream()
				.mapToDouble(t -> t.eficiencia() * (t.profissao() == Profissao.CONSTRUTOR ? 1.0 : 0.5)).sum();
	}

	private Construcao recarrega(Construcao c) {
		return construcaoRepository.findById(c.getId()).orElseThrow();
	}

	private long eventos(TipoEventoTurno tipo) {
		return eventoRepository.findAll().stream()
				.filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == tipo).count();
	}

	@Test
	void doisConstrutoresConcluemCasaEmDoisTurnos() {
		novaVila();
		Construcao casa = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 4);
		Cidadao a = aloca(casa, Profissao.CONSTRUTOR);
		aloca(casa, Profissao.CONSTRUTOR);
		double ganho = ganhoEsperado(casa);
		assertThat(ganho).isGreaterThan(0);
		etapa.executar(vila, 1);
		assertThat(recarrega(casa).getPoAtual()).isEqualTo((int) Math.floor(ganho + 1e-9));
		for (int t = 2; t <= 6 && recarrega(casa).getEstado() != EstadoConstrucao.ATIVA; t++) {
			etapa.executar(vila, t);
		}
		Construcao fim = recarrega(casa);
		assertThat(fim.getEstado()).isEqualTo(EstadoConstrucao.ATIVA);
		assertThat(fim.getPoAtual()).isEqualTo(4);
		assertThat(eventos(TipoEventoTurno.OBRA_CONCLUIDA)).isEqualTo(1);
		Cidadao livre = cidadaoRepository.findById(a.getId()).orElseThrow();
		assertThat(livre.getConstrucaoId()).isNull();
		assertThat(livre.getProfissaoTrabalho()).isNull();
	}

	@Test
	void carregadorContaMeioPontoEFracaoAcumula() {
		novaVila();
		Construcao casa = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 40);
		aloca(casa, Profissao.CONSTRUTOR);
		aloca(casa, Profissao.CARREGADOR);
		double ganho = ganhoEsperado(casa);
		int turnos = 4;
		for (int t = 1; t <= turnos; t++) {
			etapa.executar(vila, t);
		}
		assertThat(recarrega(casa).getPoAtual()).isEqualTo((int) Math.floor(ganho * turnos + 1e-6));
	}

	@Test
	void multiplasObrasSaoProcessadas() {
		novaVila();
		Construcao a = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 40);
		Construcao b = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 40);
		aloca(a, Profissao.CONSTRUTOR);
		aloca(b, Profissao.CONSTRUTOR);
		for (int t = 1; t <= 3; t++) {
			etapa.executar(vila, t);
		}
		assertThat(recarrega(a).getPoAtual()).isGreaterThan(0);
		assertThat(recarrega(b).getPoAtual()).isGreaterThan(0);
	}

	@Test
	void trabalhadorForaDeConstrutorOuCarregadorNaoContaEPrediosAtivosSaoIgnorados() {
		novaVila();
		Construcao obra = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 40);
		aloca(obra, Profissao.MADEIREIRO);
		Construcao ativa = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.ATIVA, 4);
		ativa.setPoAtual(4);
		construcaoRepository.saveAndFlush(ativa);
		etapa.executar(vila, 1);
		assertThat(recarrega(obra).getPoAtual()).isZero();
		assertThat(recarrega(ativa).getPoAtual()).isEqualTo(4);
	}

	@Test
	void respeitaLimiteDeTrabalhadoresDoNivel() {
		novaVila();
		Construcao casa = obra(TipoConstrucao.CASA, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 100);
		for (int i = 0; i < 4; i++) {
			aloca(casa, Profissao.CONSTRUTOR);
		}
		List<ConsultaTrabalhadores.Trabalhador> ts = consulta.trabalhadores(casa, vila);
		double esperado = ts.stream().limit(2).mapToDouble(ConsultaTrabalhadores.Trabalhador::eficiencia).sum();
		etapa.executar(vila, 1);
		assertThat(recarrega(casa).getPoAtual()).isEqualTo((int) Math.floor(esperado + 1e-9));
	}

	@Test
	void upgradeConcluidoVoltaAtivaEmiteEventoELiberaConstrutores() {
		novaVila();
		Construcao casa = obra(TipoConstrucao.CASA, NivelConstrucao.N2, EstadoConstrucao.EM_UPGRADE, 2);
		Cidadao c = aloca(casa, Profissao.CONSTRUTOR);
		aloca(casa, Profissao.CONSTRUTOR);
		aloca(casa, Profissao.CONSTRUTOR);
		etapa.executar(vila, 1);
		etapa.executar(vila, 2);
		Construcao fim = recarrega(casa);
		assertThat(fim.getEstado()).isEqualTo(EstadoConstrucao.ATIVA);
		assertThat(fim.getNivel()).isEqualTo(NivelConstrucao.N2);
		assertThat(eventos(TipoEventoTurno.UPGRADE_CONCLUIDO)).isEqualTo(1);
		assertThat(eventos(TipoEventoTurno.OBRA_CONCLUIDA)).isZero();
		assertThat(cidadaoRepository.findById(c.getId()).orElseThrow().getConstrucaoId()).isNull();
	}

	@Test
	void carregadorDeArmazemPermaneceAlocadoAoConcluir() {
		novaVila();
		Construcao armazem = obra(TipoConstrucao.ARMAZEM, NivelConstrucao.N1, EstadoConstrucao.EM_OBRA, 1);
		Cidadao carregador = aloca(armazem, Profissao.CARREGADOR);
		Cidadao construtor = aloca(armazem, Profissao.CONSTRUTOR);
		for (int t = 1; t <= 6 && recarrega(armazem).getEstado() != EstadoConstrucao.ATIVA; t++) {
			etapa.executar(vila, t);
		}
		assertThat(recarrega(armazem).getEstado()).isEqualTo(EstadoConstrucao.ATIVA);
		assertThat(cidadaoRepository.findById(carregador.getId()).orElseThrow().getConstrucaoId()).isEqualTo(armazem.getId());
		assertThat(cidadaoRepository.findById(construtor.getId()).orElseThrow().getConstrucaoId()).isNull();
	}

}
