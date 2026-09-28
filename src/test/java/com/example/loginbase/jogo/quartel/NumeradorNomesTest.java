package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.auditoria.AuditoriaConfig;
import com.example.loginbase.auditoria.UsuarioAuditorAware;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.dominio.ContadorNome;
import com.example.loginbase.jogo.dominio.ContadorNomeRepository;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;

/**
 * Testa {@link NumeradorNomes} contra o Postgres real do
 * {@code docker-compose} (mesma premissa de {@code RepositoriosJogoTest}):
 * numeração histórica de pares nome/sobrenome por vila (design.md D11) via
 * {@code jogo_contadores_nome}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, NumeradorNomes.class })
class NumeradorNomesTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private ContadorNomeRepository contadorNomeRepository;

	@Autowired
	private NumeradorNomes numeradorNomes;

	private Usuario criarUsuario(String email) {
		Usuario usuario = new Usuario();
		usuario.setNome("Jogador");
		usuario.setEmail(email);
		usuario.setSenha("senha-hash");
		return entityManager.persistFlushFind(usuario);
	}

	private Vila criarVila(Long usuarioId, String nome) {
		Vila vila = new Vila();
		vila.setUsuarioId(usuarioId);
		vila.setNome(nome);
		vila.setComida(1000);
		vila.setMadeira(1000);
		vila.setPedra(1000);
		vila.setFerro(1000);
		vila.setRecursosAtualizadosEm(Instant.now());
		return vilaRepository.saveAndFlush(vila);
	}

	private Item criarItem(Long vilaId, ModeloItem modelo) {
		Item item = new Item();
		item.setVilaId(vilaId);
		item.setModelo(modelo);
		item.setNivel(1);
		item.setOrigem(OrigemItem.FORJA);
		item.setStatus(StatusItem.EQUIPADO);
		return itemRepository.saveAndFlush(item);
	}

	private Unidade criarUnidade(Long vilaId, Item arma, Item armadura, String nome, String sobrenome,
			int ordinalNome) {
		Unidade unidade = new Unidade();
		unidade.setVilaId(vilaId);
		unidade.setTipo(TipoTropa.SOLDADO);
		unidade.setArmaItemId(arma.getId());
		unidade.setArmaduraItemId(armadura.getId());
		unidade.setStatus(StatusUnidade.DISPONIVEL);
		unidade.setNome(nome);
		unidade.setSobrenome(sobrenome);
		unidade.setOrdinalNome(ordinalNome);
		return unidadeRepository.saveAndFlush(unidade);
	}

	@Test
	void primeiraOcorrenciaDoParNaVilaRecebeOrdinal1() {
		Usuario usuario = criarUsuario("numerador-primeira@teste.local");
		Vila vila = criarVila(usuario.getId(), "Vila 1");

		int ordinal = numeradorNomes.proximoOrdinal(vila.getId(), "Ana", "Silva");

		assertThat(ordinal).isEqualTo(1);
		ContadorNome contador = contadorNomeRepository.findByVilaIdAndNomeAndSobrenome(vila.getId(), "Ana", "Silva")
				.orElseThrow();
		assertThat(contador.getUltimoOrdinal()).isEqualTo(1);
	}

	@Test
	void segundaOcorrenciaDoMesmoParNaVilaRecebeOrdinal2() {
		Usuario usuario = criarUsuario("numerador-segunda@teste.local");
		Vila vila = criarVila(usuario.getId(), "Vila 2");

		int primeiro = numeradorNomes.proximoOrdinal(vila.getId(), "Ana", "Silva");
		int segundo = numeradorNomes.proximoOrdinal(vila.getId(), "Ana", "Silva");

		assertThat(primeiro).isEqualTo(1);
		assertThat(segundo).isEqualTo(2);
		ContadorNome contador = contadorNomeRepository.findByVilaIdAndNomeAndSobrenome(vila.getId(), "Ana", "Silva")
				.orElseThrow();
		assertThat(contador.getUltimoOrdinal()).isEqualTo(2);
	}

	@Test
	void ordinalNuncaEReaproveitadoAposUnidadeDeOrdinal2SerExcluida() {
		Usuario usuario = criarUsuario("numerador-exclusao@teste.local");
		Vila vila = criarVila(usuario.getId(), "Vila 3");

		Item arma1 = criarItem(vila.getId(), ModeloItem.ESPADA);
		Item armadura1 = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO);
		Item arma2 = criarItem(vila.getId(), ModeloItem.ESPADA);
		Item armadura2 = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO);

		int ordinal1 = numeradorNomes.proximoOrdinal(vila.getId(), "Ana", "Silva");
		Unidade unidadeOrdinal1 = criarUnidade(vila.getId(), arma1, armadura1, "Ana", "Silva", ordinal1);

		int ordinal2 = numeradorNomes.proximoOrdinal(vila.getId(), "Ana", "Silva");
		Unidade unidadeOrdinal2 = criarUnidade(vila.getId(), arma2, armadura2, "Ana", "Silva", ordinal2);

		assertThat(ordinal1).isEqualTo(1);
		assertThat(ordinal2).isEqualTo(2);
		assertThat(unidadeOrdinal1.getId()).isNotNull();

		// A morte apaga a unidade, não o contador (D11): o ordinal 2 não volta a
		// ficar disponível.
		unidadeRepository.delete(unidadeOrdinal2);
		unidadeRepository.flush();

		int ordinal3 = numeradorNomes.proximoOrdinal(vila.getId(), "Ana", "Silva");
		assertThat(ordinal3).isEqualTo(3);
	}

	@Test
	void vilasDiferentesTemContadoresIndependentes() {
		Usuario usuario1 = criarUsuario("numerador-vila1@teste.local");
		Usuario usuario2 = criarUsuario("numerador-vila2@teste.local");
		Vila vila1 = criarVila(usuario1.getId(), "Vila A");
		Vila vila2 = criarVila(usuario2.getId(), "Vila B");

		int ordinalVila1Primeiro = numeradorNomes.proximoOrdinal(vila1.getId(), "Ana", "Silva");
		int ordinalVila1Segundo = numeradorNomes.proximoOrdinal(vila1.getId(), "Ana", "Silva");
		int ordinalVila2Primeiro = numeradorNomes.proximoOrdinal(vila2.getId(), "Ana", "Silva");

		assertThat(ordinalVila1Primeiro).isEqualTo(1);
		assertThat(ordinalVila1Segundo).isEqualTo(2);
		assertThat(ordinalVila2Primeiro).isEqualTo(1);
	}

}
