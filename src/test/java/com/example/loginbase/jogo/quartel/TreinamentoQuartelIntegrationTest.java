package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
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
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.EtapaTurno;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.turno.etapas.EtapaQuartel;

@SpringBootTest
@Transactional
class TreinamentoQuartelIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired TropaRepository tropaRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired EtapaQuartel etapa;
	@Autowired List<EtapaTurno> etapas;

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

	private Cidadao cidadao(int anos) {
		return cidadaoRepository.saveAndFlush(new Cidadao(vila.getId(), familia.getId(), "C" + (++seq), Sexo.M, anos * 12));
	}

	private Construcao quartel(NivelConstrucao nivel, EstadoConstrucao estado, int instrutores) {
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.QUARTEL);
		c.setNivel(nivel);
		c.setRegiaoIndice(1);
		c.setX(seq++);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(8);
		c.setPoAtual(8);
		c = construcaoRepository.saveAndFlush(c);
		for (int i = 0; i < instrutores; i++) {
			Cidadao inst = cidadao(30);
			inst.setConstrucaoId(c.getId());
			inst.setProfissaoTrabalho(Profissao.GUERREIRO);
			cidadaoRepository.saveAndFlush(inst);
		}
		return c;
	}

	private Tropa tropa(Construcao q, EstadoTropa estado) {
		Tropa t = new Tropa(vila.getId(), q.getId(), "T" + (++seq));
		t.setEstado(estado);
		return tropaRepository.saveAndFlush(t);
	}

	private Cidadao membro(Tropa t, int pe, String xp) {
		Cidadao c = cidadao(20);
		c.setTropaId(t.getId());
		c.setPosicaoTropa(PosicaoTropa.FRENTE);
		c.setXpGuerreiro(new BigDecimal(xp));
		cidadaoRepository.saveAndFlush(c);
		profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.GUERREIRO, pe));
		return c;
	}

	private BigDecimal xp(Cidadao c) {
		return cidadaoRepository.findById(c.getId()).orElseThrow().getXpGuerreiro();
	}

	private int pe(Cidadao c) {
		return profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), Profissao.GUERREIRO).orElseThrow().getPontosBase();
	}

	private long eventosTreino(int turno) {
		return eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(vila.getId(), turno).stream()
				.filter(e -> e.getTipo() == TipoEventoTurno.TREINO_PE_GUERREIRO).count();
	}

	@Test
	void xpPorNivelDoQuartel() {
		novaVila();
		Cidadao a = membro(tropa(quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1), EstadoTropa.AQUARTELADA), 1, "0");
		Cidadao b = membro(tropa(quartel(NivelConstrucao.N2, EstadoConstrucao.ATIVA, 1), EstadoTropa.AQUARTELADA), 1, "0");
		Cidadao c = membro(tropa(quartel(NivelConstrucao.N3, EstadoConstrucao.ATIVA, 1), EstadoTropa.AQUARTELADA), 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(a)).isEqualByComparingTo("0.5");
		assertThat(xp(b)).isEqualByComparingTo("1.0");
		assertThat(xp(c)).isEqualByComparingTo("1.5");
	}

	@Test
	void duasTropasNoMesmoQuartelTreinam() {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N2, EstadoConstrucao.ATIVA, 1);
		Cidadao a = membro(tropa(q, EstadoTropa.AQUARTELADA), 1, "0");
		Cidadao b = membro(tropa(q, EstadoTropa.AQUARTELADA), 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(a)).isEqualByComparingTo("1.0");
		assertThat(xp(b)).isEqualByComparingTo("1.0");
	}

	@Test
	void conversaoDe10XpEmUmPeComEvento() {
		novaVila();
		Cidadao g = membro(tropa(quartel(NivelConstrucao.N2, EstadoConstrucao.ATIVA, 1), EstadoTropa.AQUARTELADA), 8, "7");
		etapa.executar(vila, 1);
		etapa.executar(vila, 2);
		assertThat(pe(g)).isEqualTo(8);
		assertThat(xp(g)).isEqualByComparingTo("9.0");
		assertThat(eventosTreino(2)).isZero();
		etapa.executar(vila, 3);
		assertThat(pe(g)).isEqualTo(9);
		assertThat(xp(g)).isEqualByComparingTo("0");
		assertThat(eventosTreino(3)).isEqualTo(1);
	}

	@Test
	void semInstrutorNaoTreina() {
		novaVila();
		Cidadao g = membro(tropa(quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 0), EstadoTropa.AQUARTELADA), 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(g)).isEqualByComparingTo("0");
	}

	@Test
	void instrutorFeridoNaoContaComoInstrutor() {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao inst = cidadaoRepository.findByConstrucaoIdAndVivoTrue(q.getId()).get(0);
		inst.setEstado(EstadoCidadao.FERIDO);
		cidadaoRepository.saveAndFlush(inst);
		Cidadao g = membro(tropa(q, EstadoTropa.AQUARTELADA), 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(g)).isEqualByComparingTo("0");
	}

	@Test
	void quartelEmObraNaoTreina() {
		novaVila();
		Cidadao g = membro(tropa(quartel(NivelConstrucao.N1, EstadoConstrucao.EM_UPGRADE, 1), EstadoTropa.AQUARTELADA), 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(g)).isEqualByComparingTo("0");
	}

	@Test
	void feridoNaoGanhaXpMasSaudavelDaMesmaTropaGanha() {
		novaVila();
		Tropa t = tropa(quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1), EstadoTropa.AQUARTELADA);
		Cidadao ferido = membro(t, 1, "0");
		ferido.setEstado(EstadoCidadao.FERIDO);
		cidadaoRepository.saveAndFlush(ferido);
		Cidadao ok = membro(t, 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(ferido)).isEqualByComparingTo("0");
		assertThat(xp(ok)).isEqualByComparingTo("0.5");
	}

	@Test
	void tropaEmViagemNaoTreina() {
		novaVila();
		Construcao q = quartel(NivelConstrucao.N1, EstadoConstrucao.ATIVA, 1);
		Cidadao ida = membro(tropa(q, EstadoTropa.EM_VIAGEM_IDA), 1, "0");
		Cidadao volta = membro(tropa(q, EstadoTropa.EM_VIAGEM_VOLTA), 1, "0");
		etapa.executar(vila, 1);
		assertThat(xp(ida)).isEqualByComparingTo("0");
		assertThat(xp(volta)).isEqualByComparingTo("0");
	}

	@Test
	void pipelineDoTurnoExecutaTreino() {
		novaVila();
		Cidadao g = membro(tropa(quartel(NivelConstrucao.N3, EstadoConstrucao.ATIVA, 1), EstadoTropa.AQUARTELADA), 1, "0");
		// processarVila usa REQUIRES_NEW (não enxerga dados não commitados): roda o pipeline real na ordem
		etapas.stream().sorted(Comparator.comparingInt(EtapaTurno::ordem)).forEach(e -> e.executar(vila, 1));
		assertThat(xp(g)).isEqualByComparingTo("1.5");
	}

}
