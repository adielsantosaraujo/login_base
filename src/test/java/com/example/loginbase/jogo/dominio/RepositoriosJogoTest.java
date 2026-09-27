package com.example.loginbase.jogo.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.auditoria.AuditoriaConfig;
import com.example.loginbase.auditoria.UsuarioAuditorAware;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;

/**
 * Testa o mapeamento JPA das 8 entidades do jogo contra o schema real de
 * {@code V3__jogo.sql} (Postgres do {@code docker-compose}, iniciado via
 * {@code make up}): grava/lê entidades, associa filhas à vila e verifica as
 * unique constraints. {@code replace = NONE} para usar o datasource real
 * (não substitui por um banco embarcado) e cada teste roda em transação com
 * rollback automático.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class })
class RepositoriosJogoTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private VilaRepository vilaRepository;

	@Autowired
	private PredioRepository predioRepository;

	@Autowired
	private CanteiroRepository canteiroRepository;

	@Autowired
	private EstoqueSementeRepository estoqueSementeRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private OrdemRepository ordemRepository;

	@Autowired
	private BatalhaRepository batalhaRepository;

	private Usuario criarUsuario(String email) {
		Usuario usuario = new Usuario();
		usuario.setNome("Jogador");
		usuario.setEmail(email);
		usuario.setSenha("senha-hash");
		return entityManager.persistFlushFind(usuario);
	}

	private Vila criarVila(Long usuarioId) {
		Vila vila = new Vila();
		vila.setUsuarioId(usuarioId);
		vila.setNome("Vila de Teste");
		vila.setComida(1000);
		vila.setMadeira(1000);
		vila.setPedra(1000);
		vila.setFerro(1000);
		vila.setRecursosAtualizadosEm(Instant.now());
		return vilaRepository.saveAndFlush(vila);
	}

	@Test
	void deveGravarELerVilaComAuditoriaPreenchida() {
		Usuario usuario = criarUsuario("dono-vila@teste.local");

		Vila vila = criarVila(usuario.getId());

		Optional<Vila> encontrada = vilaRepository.findById(vila.getId());
		assertThat(encontrada).isPresent();
		assertThat(encontrada.get().getUsuarioId()).isEqualTo(usuario.getId());
		assertThat(encontrada.get().getMasmorraNivelLiberado()).isEqualTo(1);
		assertThat(encontrada.get().getCriadoEm()).isNotNull();
		assertThat(encontrada.get().getCriadoPor()).isNotBlank();
		assertThat(encontrada.get().getAlteradoEm()).isNotNull();
		assertThat(encontrada.get().getAlteradoPor()).isNotBlank();
	}

	@Test
	void deveBuscarVilaPorUsuarioComLockPessimista() {
		Usuario usuario = criarUsuario("lock@teste.local");
		Vila vila = criarVila(usuario.getId());

		Optional<Vila> resultado = vilaRepository.findByUsuarioIdParaAtualizacao(usuario.getId());

		assertThat(resultado).isPresent();
		assertThat(resultado.get().getId()).isEqualTo(vila.getId());
	}

	@Test
	void deveAssociarPrediosCanteirosEItensAVila() {
		Usuario usuario = criarUsuario("associa@teste.local");
		Vila vila = criarVila(usuario.getId());

		Predio predio = new Predio();
		predio.setVilaId(vila.getId());
		predio.setTipo(TipoPredio.FAZENDA);
		predio.setNivel(1);
		predioRepository.saveAndFlush(predio);

		Canteiro canteiro = new Canteiro();
		canteiro.setVilaId(vila.getId());
		canteiro.setPosicao(1);
		canteiro.setCultivo(Cultivo.TRIGO);
		canteiro.setPlantadoEm(Instant.now());
		canteiroRepository.saveAndFlush(canteiro);

		EstoqueSemente semente = new EstoqueSemente();
		semente.setVilaId(vila.getId());
		semente.setCultivo(Cultivo.MILHO);
		semente.setQuantidade(5);
		estoqueSementeRepository.saveAndFlush(semente);

		Item arma = new Item();
		arma.setVilaId(vila.getId());
		arma.setModelo(ModeloItem.ESPADA);
		arma.setNivel(1);
		arma.setOrigem(OrigemItem.FORJA);
		arma.setStatus(StatusItem.DISPONIVEL);
		itemRepository.saveAndFlush(arma);

		assertThat(predioRepository.findByVilaId(vila.getId())).extracting(Predio::getTipo)
				.containsExactly(TipoPredio.FAZENDA);
		assertThat(canteiroRepository.findByVilaId(vila.getId())).extracting(Canteiro::getPosicao)
				.containsExactly(1);
		assertThat(estoqueSementeRepository.findByVilaId(vila.getId())).extracting(EstoqueSemente::getCultivo)
				.containsExactly(Cultivo.MILHO);
		assertThat(itemRepository.findByVilaId(vila.getId())).extracting(Item::getModelo)
				.containsExactly(ModeloItem.ESPADA);
	}

	@Test
	void deveRespeitarUniqueConstraintDePredioPorVilaETipo() {
		Usuario usuario = criarUsuario("unique-predio@teste.local");
		Vila vila = criarVila(usuario.getId());

		Predio primeiro = new Predio();
		primeiro.setVilaId(vila.getId());
		primeiro.setTipo(TipoPredio.SERRARIA);
		primeiro.setNivel(1);
		predioRepository.saveAndFlush(primeiro);

		Predio duplicado = new Predio();
		duplicado.setVilaId(vila.getId());
		duplicado.setTipo(TipoPredio.SERRARIA);
		duplicado.setNivel(2);

		assertThatThrownBy(() -> predioRepository.saveAndFlush(duplicado))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveRespeitarUniqueConstraintDeOrdemPorVilaECategoria() {
		Usuario usuario = criarUsuario("unique-ordem@teste.local");
		Vila vila = criarVila(usuario.getId());

		Ordem primeira = new Ordem();
		primeira.setVilaId(vila.getId());
		primeira.setCategoria(CategoriaOrdem.CONSTRUCAO);
		primeira.setAlvo(TipoPredio.FAZENDA.name());
		primeira.setNivel(2);
		primeira.setQuantidade(1);
		primeira.setIniciadaEm(Instant.now());
		primeira.setConcluiEm(Instant.now().plusSeconds(60));
		ordemRepository.saveAndFlush(primeira);

		Ordem segunda = new Ordem();
		segunda.setVilaId(vila.getId());
		segunda.setCategoria(CategoriaOrdem.CONSTRUCAO);
		segunda.setAlvo(TipoPredio.ARMAZEM.name());
		segunda.setNivel(1);
		segunda.setQuantidade(1);
		segunda.setIniciadaEm(Instant.now());
		segunda.setConcluiEm(Instant.now().plusSeconds(60));

		assertThatThrownBy(() -> ordemRepository.saveAndFlush(segunda))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveGravarUnidadeComItensDeArmaEArmadura() {
		Usuario usuario = criarUsuario("unidade@teste.local");
		Vila vila = criarVila(usuario.getId());

		Item arma = new Item();
		arma.setVilaId(vila.getId());
		arma.setModelo(ModeloItem.LANCA);
		arma.setNivel(1);
		arma.setOrigem(OrigemItem.FORJA);
		arma.setStatus(StatusItem.EQUIPADO);
		arma = itemRepository.saveAndFlush(arma);

		Item armadura = new Item();
		armadura.setVilaId(vila.getId());
		armadura.setModelo(ModeloItem.ARMADURA_COURO);
		armadura.setNivel(1);
		armadura.setOrigem(OrigemItem.FORJA);
		armadura.setStatus(StatusItem.EQUIPADO);
		armadura = itemRepository.saveAndFlush(armadura);

		Unidade unidade = new Unidade();
		unidade.setVilaId(vila.getId());
		unidade.setTipo(com.example.loginbase.jogo.catalogo.TipoTropa.LANCEIRO);
		unidade.setArmaItemId(arma.getId());
		unidade.setArmaduraItemId(armadura.getId());
		unidade.setStatus(StatusUnidade.DISPONIVEL);

		Unidade salva = unidadeRepository.saveAndFlush(unidade);

		assertThat(unidadeRepository.findById(salva.getId())).isPresent();
	}

	@Test
	void deveGravarBatalhaComEstadoLogELootComoJson() {
		Usuario usuario = criarUsuario("batalha@teste.local");
		Vila vila = criarVila(usuario.getId());

		Batalha batalha = new Batalha();
		batalha.setVilaId(vila.getId());
		batalha.setMasmorraNivel(1);
		batalha.setStatus(StatusBatalha.EM_ANDAMENTO);
		batalha.setTurno(0);
		batalha.setEstado("{\"turno\":0}");
		batalha.setLog("[]");
		batalha.setIniciadaEm(Instant.now());

		Batalha salva = batalhaRepository.saveAndFlush(batalha);

		assertThat(salva.getVersion()).isZero();
		assertThat(batalhaRepository.findByVilaId(vila.getId())).extracting(Batalha::getStatus)
				.containsExactly(StatusBatalha.EM_ANDAMENTO);
	}

	@Test
	void deveRespeitarIndiceUnicoDeBatalhaEmAndamentoPorVila() {
		Usuario usuario = criarUsuario("unique-batalha@teste.local");
		Vila vila = criarVila(usuario.getId());

		Batalha primeira = new Batalha();
		primeira.setVilaId(vila.getId());
		primeira.setMasmorraNivel(1);
		primeira.setStatus(StatusBatalha.EM_ANDAMENTO);
		primeira.setTurno(0);
		primeira.setEstado("{}");
		primeira.setLog("[]");
		primeira.setIniciadaEm(Instant.now());
		batalhaRepository.saveAndFlush(primeira);

		Batalha segunda = new Batalha();
		segunda.setVilaId(vila.getId());
		segunda.setMasmorraNivel(1);
		segunda.setStatus(StatusBatalha.EM_ANDAMENTO);
		segunda.setTurno(0);
		segunda.setEstado("{}");
		segunda.setLog("[]");
		segunda.setIniciadaEm(Instant.now());

		assertThatThrownBy(() -> batalhaRepository.saveAndFlush(segunda))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

}
