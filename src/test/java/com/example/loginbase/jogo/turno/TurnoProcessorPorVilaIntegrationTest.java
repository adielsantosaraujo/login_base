package com.example.loginbase.jogo.turno;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

/** Não transacional: cada vila confirma em REQUIRES_NEW; os dados criados são removidos ao final. */
@SpringBootTest
class TurnoProcessorPorVilaIntegrationTest {

	static final int TURNO = 900_000;
	static volatile Long vilaQueFalha;

	@TestConfiguration
	static class Config {
		@Bean
		EtapaTurno etapaDeTeste(RegistroEventoTurnoService registro, VilaRepository vilas) {
			return new EtapaTurno() {
				public int ordem() { return 99; }
				public String nome() { return "teste"; }
				public void executar(Vila vila, int turno) {
					registro.registrar(vila, turno, TipoEventoTurno.PRODUCAO, "ok", Map.of("k", 1));
					if (vila.getId().equals(vilaQueFalha)) {
						throw new IllegalStateException("falha simulada");
					}
				}
			};
		}
	}

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired TurnoProcessor processor;
	@Autowired JdbcTemplate jdbc;

	final List<Vila> criadas = new ArrayList<>();

	@AfterEach
	void limpar() {
		vilaQueFalha = null;
		for (Vila v : criadas) {
			jdbc.update("delete from evento_turno where vila_id = ?", v.getId());
			jdbc.update("delete from vila where id = ?", v.getId());
			jdbc.update("delete from usuarios where id = ?", v.getUsuarioId());
		}
		criadas.clear();
	}

	private Vila novaVila() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		Vila v = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 1L, 1));
		criadas.add(v);
		return v;
	}

	@Test
	void processaVilaRegistraEventoEMarcaTurnoSendoIdempotente() {
		Vila v = novaVila();

		processor.processarTurno(TURNO);
		processor.processarTurno(TURNO);

		assertThat(vilaRepository.findById(v.getId()).orElseThrow().getTurnoProcessado()).isEqualTo(TURNO);
		List<EventoTurno> eventos = eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(v.getId(), TURNO);
		assertThat(eventos).hasSize(1);
		assertThat(eventos.get(0).getDados()).containsEntry("k", 1);
	}

	@Test
	void falhaEmUmaVilaRegistraEventoEContinuaComAsDemais() {
		Vila ruim = novaVila();
		Vila boa = novaVila();
		vilaQueFalha = ruim.getId();

		processor.processarTurno(TURNO + 1);

		List<EventoTurno> eventosRuim = eventoRepository.findByVilaIdAndTurnoOrderByIdAsc(ruim.getId(), TURNO + 1);
		assertThat(eventosRuim).extracting(EventoTurno::getTipo).containsExactly(TipoEventoTurno.FALHA_PROCESSAMENTO);
		assertThat(vilaRepository.findById(ruim.getId()).orElseThrow().getTurnoProcessado()).isNull();
		assertThat(vilaRepository.findById(boa.getId()).orElseThrow().getTurnoProcessado()).isEqualTo(TURNO + 1);
		assertThat(eventoRepository.countByVilaIdAndTurno(boa.getId(), TURNO + 1)).isEqualTo(1);
	}

}
