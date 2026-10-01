package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.loginbase.jogo.modelo.Vila;

class FamiliaServiceTest {

	private final List<Cidadao> salvos = new ArrayList<>();
	private final AtomicLong ids = new AtomicLong(100);
	private final FamiliaRepository familiaRepo = Mockito.mock(FamiliaRepository.class);
	private final CidadaoRepository cidadaoRepo = Mockito.mock(CidadaoRepository.class);
	private final FamiliaService service = new FamiliaService(familiaRepo, cidadaoRepo);
	private final Vila vila = Mockito.mock(Vila.class);

	FamiliaServiceTest() {
		Mockito.when(vila.getId()).thenReturn(1L);
		Mockito.when(familiaRepo.save(Mockito.any(Familia.class))).thenAnswer(i -> {
			Familia f = i.getArgument(0);
			if (f.getId() == null) {
				f.setId(ids.incrementAndGet());
			}
			return f;
		});
		Mockito.when(cidadaoRepo.saveAll(Mockito.anyIterable())).thenAnswer(i -> {
			Iterable<Cidadao> it = i.getArgument(0);
			for (Cidadao c : it) {
				if (c.getId() == null) {
					c.setId(ids.incrementAndGet());
				}
				if (!salvos.contains(c)) {
					salvos.add(c);
				}
			}
			return it;
		});
	}

	private List<Familia> gerar(long semente) {
		return service.gerarFamiliasIniciais(vila, List.of(11L, 12L, 13L, 14L), semente);
	}

	@Test
	void geraQuatroFamiliasComQuatroMembros() {
		List<Familia> familias = gerar(7);
		assertThat(familias).hasSize(4);
		assertThat(salvos).hasSize(16);
		assertThat(familias).extracting(Familia::getCasaId).containsExactly(11L, 12L, 13L, 14L);
		for (Familia f : familias) {
			List<Cidadao> membros = salvos.stream().filter(c -> c.getFamiliaId().equals(f.getId())).toList();
			assertThat(membros).hasSize(4);
			assertThat(membros).extracting(Cidadao::getNome).doesNotHaveDuplicates().allMatch(n -> !n.isBlank());
			assertThat(membros).extracting(Cidadao::getIdadeMeses).containsExactlyInAnyOrder(480, 480, 216, 216);
		}
		assertThat(new HashSet<>(familias.stream().map(Familia::getSobrenome).toList())).hasSize(4);
	}

	@Test
	void casaisReciprocosECaracteristicasZeradasComPendentes() {
		gerar(7);
		for (Cidadao c : salvos) {
			assertThat(c.getVit() + c.getForca() + c.getVel() + c.getInteligencia() + c.getCar()).isZero();
			assertThat(c.getPontosCarPendentes()).isEqualTo(20);
			assertThat(c.getPontosProfPendentes()).isEqualTo(10);
			assertThat(c.isVivo()).isTrue();
			if (c.getIdadeMeses() == 480) {
				Cidadao conjuge = salvos.stream().filter(o -> o.getId().equals(c.getConjugeId())).findFirst().orElseThrow();
				assertThat(conjuge.getConjugeId()).isEqualTo(c.getId());
				assertThat(conjuge.getSexo()).isNotEqualTo(c.getSexo());
			} else {
				assertThat(c.getConjugeId()).isNull();
				assertThat(c.getPaiId()).isNotNull();
				assertThat(c.getMaeId()).isNotNull();
			}
		}
	}

	@Test
	void mesmaSementeGeraOsMesmosNomes() {
		gerar(42);
		List<String> primeira = salvos.stream().map(Cidadao::getNome).toList();
		salvos.clear();
		gerar(42);
		assertThat(salvos.stream().map(Cidadao::getNome).toList()).isEqualTo(primeira);
	}

}
