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
 * Testa o mapeamento JPA das entidades do jogo contra o schema real de
 * {@code V3__jogo.sql}/{@code V4__unidade_nome_e_lote_treino.sql}/{@code V5__niveis_estendidos.sql} (Postgres
 * do {@code docker-compose}, iniciado via {@code make up}): grava/lê
 * entidades, associa filhas à vila e verifica as unique constraints.
 * {@code replace = NONE} para usar o datasource real (não substitui por um
 * banco embarcado) e cada teste roda em transação com rollback automático.
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

	@Autowired
	private ContadorNomeRepository contadorNomeRepository;

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

	private Predio novoPredio(Vila vila, int nivel) {
		Predio predio = new Predio();
		predio.setVilaId(vila.getId());
		predio.setTipo(TipoPredio.SERRARIA);
		predio.setNivel(nivel);
		return predio;
	}

	private Canteiro novoCanteiro(Vila vila, int posicao) {
		Canteiro canteiro = new Canteiro();
		canteiro.setVilaId(vila.getId());
		canteiro.setPosicao(posicao);
		canteiro.setCultivo(Cultivo.TRIGO);
		canteiro.setPlantadoEm(Instant.now());
		return canteiro;
	}

	private Item novoItem(Vila vila, int nivel) {
		Item item = new Item();
		item.setVilaId(vila.getId());
		item.setModelo(ModeloItem.ESPADA);
		item.setNivel(nivel);
		item.setOrigem(OrigemItem.FORJA);
		item.setStatus(StatusItem.DISPONIVEL);
		return item;
	}

	@Test
	void deveAceitarPredioNoNivelCem() {
		Vila vila = criarVila(criarUsuario("predio-100@teste.local").getId());

		Predio salvo = predioRepository.saveAndFlush(novoPredio(vila, 100));

		assertThat(salvo.getNivel()).isEqualTo(100);
	}

	@Test
	void deveRejeitarPredioNoNivelCentoEUm() {
		Vila vila = criarVila(criarUsuario("predio-101@teste.local").getId());

		assertThatThrownBy(() -> predioRepository.saveAndFlush(novoPredio(vila, 101)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveAceitarCanteiroNaPosicaoVinteEQuatro() {
		Vila vila = criarVila(criarUsuario("canteiro-24@teste.local").getId());

		Canteiro salvo = canteiroRepository.saveAndFlush(novoCanteiro(vila, 24));

		assertThat(salvo.getPosicao()).isEqualTo(24);
	}

	@Test
	void deveRejeitarCanteiroNaPosicaoVinteECinco() {
		Vila vila = criarVila(criarUsuario("canteiro-25@teste.local").getId());

		assertThatThrownBy(() -> canteiroRepository.saveAndFlush(novoCanteiro(vila, 25)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveAceitarItemNoNivelVinteETres() {
		Vila vila = criarVila(criarUsuario("item-23@teste.local").getId());

		Item salvo = itemRepository.saveAndFlush(novoItem(vila, 23));

		assertThat(salvo.getNivel()).isEqualTo(23);
	}

	@Test
	void deveRejeitarItemNoNivelVinteEQuatro() {
		Vila vila = criarVila(criarUsuario("item-24@teste.local").getId());

		assertThatThrownBy(() -> itemRepository.saveAndFlush(novoItem(vila, 24)))
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
		unidade.setNome("Ana");
		unidade.setSobrenome("Silva");
		unidade.setOrdinalNome(1);

		Unidade salva = unidadeRepository.saveAndFlush(unidade);

		assertThat(unidadeRepository.findById(salva.getId())).isPresent();
	}

	@Test
	void deveGravarELerUnidadeComNomeESobrenome() {
		Usuario usuario = criarUsuario("nome-unidade@teste.local");
		Vila vila = criarVila(usuario.getId());

		Item arma = new Item();
		arma.setVilaId(vila.getId());
		arma.setModelo(ModeloItem.ESPADA);
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
		unidade.setTipo(com.example.loginbase.jogo.catalogo.TipoTropa.SOLDADO);
		unidade.setArmaItemId(arma.getId());
		unidade.setArmaduraItemId(armadura.getId());
		unidade.setStatus(StatusUnidade.DISPONIVEL);
		unidade.setNome("Ana");
		unidade.setSobrenome("Silva");
		unidade.setOrdinalNome(1);
		Unidade salva = unidadeRepository.saveAndFlush(unidade);

		Optional<Unidade> encontrada = unidadeRepository.findById(salva.getId());
		assertThat(encontrada).isPresent();
		assertThat(encontrada.get().getNome()).isEqualTo("Ana");
		assertThat(encontrada.get().getSobrenome()).isEqualTo("Silva");
		assertThat(encontrada.get().getOrdinalNome()).isEqualTo(1);
	}

	@Test
	void nomeExibicaoFormataComOuSemSufixoOrdinal() {
		Unidade primeira = new Unidade();
		primeira.setNome("Ana");
		primeira.setSobrenome("Silva");
		primeira.setOrdinalNome(1);
		assertThat(primeira.nomeExibicao()).isEqualTo("Ana Silva");

		Unidade segunda = new Unidade();
		segunda.setNome("Ana");
		segunda.setSobrenome("Silva");
		segunda.setOrdinalNome(2);
		assertThat(segunda.nomeExibicao()).isEqualTo("Ana Silva (2)");
	}

	@Test
	void deveGravarItemComOrdemIdERecuperarPorOrdemId() {
		Usuario usuario = criarUsuario("item-ordem@teste.local");
		Vila vila = criarVila(usuario.getId());

		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.TREINO);
		ordem.setAlvo(com.example.loginbase.jogo.catalogo.TipoTropa.SOLDADO.name());
		ordem.setQuantidade(1);
		ordem.setIniciadaEm(Instant.now());
		ordem.setConcluiEm(Instant.now().plusSeconds(60));
		ordem = ordemRepository.saveAndFlush(ordem);

		Item item = new Item();
		item.setVilaId(vila.getId());
		item.setModelo(ModeloItem.ESPADA);
		item.setNivel(1);
		item.setOrigem(OrigemItem.FORJA);
		item.setStatus(StatusItem.RESERVADO);
		item.setOrdemId(ordem.getId());
		item = itemRepository.saveAndFlush(item);

		Optional<Item> encontrado = itemRepository.findById(item.getId());
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getOrdemId()).isEqualTo(ordem.getId());
		assertThat(itemRepository.findByOrdemId(ordem.getId())).extracting(Item::getId)
				.containsExactly(item.getId());
		assertThat(itemRepository.findByOrdemIdOrderById(ordem.getId())).extracting(Item::getId)
				.containsExactly(item.getId());
	}

	@Test
	void deveGravarELerContadorNomePorVilaNomeESobrenome() {
		Usuario usuario = criarUsuario("contador-nome@teste.local");
		Vila vila = criarVila(usuario.getId());

		ContadorNome contador = new ContadorNome();
		contador.setVilaId(vila.getId());
		contador.setNome("Ana");
		contador.setSobrenome("Silva");
		contador.setUltimoOrdinal(1);
		contadorNomeRepository.saveAndFlush(contador);

		Optional<ContadorNome> encontrado = contadorNomeRepository.findByVilaIdAndNomeAndSobrenome(vila.getId(), "Ana",
				"Silva");
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getUltimoOrdinal()).isEqualTo(1);
	}

	@Test
	void deveRespeitarUniqueConstraintDeContadorNomePorVilaNomeESobrenome() {
		Usuario usuario = criarUsuario("unique-contador-nome@teste.local");
		Vila vila = criarVila(usuario.getId());

		ContadorNome primeiro = new ContadorNome();
		primeiro.setVilaId(vila.getId());
		primeiro.setNome("Ana");
		primeiro.setSobrenome("Silva");
		primeiro.setUltimoOrdinal(1);
		contadorNomeRepository.saveAndFlush(primeiro);

		ContadorNome duplicado = new ContadorNome();
		duplicado.setVilaId(vila.getId());
		duplicado.setNome("Ana");
		duplicado.setSobrenome("Silva");
		duplicado.setUltimoOrdinal(2);

		assertThatThrownBy(() -> contadorNomeRepository.saveAndFlush(duplicado))
				.isInstanceOf(DataIntegrityViolationException.class);
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
