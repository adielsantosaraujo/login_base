package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.auditoria.AuditoriaConfig;
import com.example.loginbase.auditoria.UsuarioAuditorAware;
import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.SlotEquipamento;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.AplicadorOrdens;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.suporte.JogoTestConfig;

/**
 * Testa {@link EquipamentoService}: troca válida de arma/armadura (item antigo
 * volta a {@code DISPONIVEL}, novo item fica {@code EQUIPADO}, unidade passa
 * a apontar para o novo item) e as rejeições da Decisão D12 (unidade
 * inexistente/de outra vila, unidade {@code EM_MASMORRA}, slot futuro, item
 * inexistente/de outra vila/indisponível/incompatível), sempre sem nenhuma
 * mudança no banco — mesma premissa de {@code QuartelServiceTest} (Postgres
 * real do {@code docker-compose}, {@code @Transactional(propagation = NOT_SUPPORTED)}
 * porque {@code VilaService} usa uma transação {@code REQUIRES_NEW} genuína
 * para criar a vila).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ AuditoriaConfig.class, UsuarioAuditorAware.class, JogoTestConfig.class, JogoProperties.class,
		VilaService.class, AplicadorOrdens.class, EquipamentoService.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EquipamentoServiceTest {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VilaService vilaService;

	@Autowired
	private EquipamentoService equipamentoService;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	private Usuario criarUsuario(String prefixoEmail, String nome) {
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setEmail(prefixoEmail + "+" + java.util.UUID.randomUUID() + "@teste.local");
		usuario.setSenha("senha-hash");
		return usuarioRepository.saveAndFlush(usuario);
	}

	private Item criarItem(Long vilaId, ModeloItem modelo, int nivel, StatusItem status) {
		Item item = new Item();
		item.setVilaId(vilaId);
		item.setModelo(modelo);
		item.setNivel(nivel);
		item.setOrigem(OrigemItem.FORJA);
		item.setStatus(status);
		return itemRepository.saveAndFlush(item);
	}

	private Unidade criarUnidade(Long vilaId, TipoTropa tipo, Long armaItemId, Long armaduraItemId,
			StatusUnidade status) {
		Unidade unidade = new Unidade();
		unidade.setVilaId(vilaId);
		unidade.setTipo(tipo);
		unidade.setArmaItemId(armaItemId);
		unidade.setArmaduraItemId(armaduraItemId);
		unidade.setStatus(status);
		// nome/sobrenome únicos por chamada (derivados do id da arma, sempre novo
		// nesses testes) para não colidir com a unique constraint da vila.
		unidade.setNome("Soldado" + armaItemId);
		unidade.setSobrenome("Teste");
		unidade.setOrdinalNome(1);
		return unidadeRepository.saveAndFlush(unidade);
	}

	private RegraJogoException assertRegra(Runnable acao) {
		return assertThrows(RegraJogoException.class, acao::run);
	}

	@Test
	void trocaValidaDeArmaLiberaAntigaEEquipaNova() {
		Usuario usuario = criarUsuario("troca-arma", "Ana");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item armaAntiga = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, armaAntiga.getId(), armadura.getId(),
				StatusUnidade.DISPONIVEL);
		Item armaNova = criarItem(vila.getId(), ModeloItem.ESPADA, 2, StatusItem.DISPONIVEL);

		equipamentoService.trocar(usuario.getId(), unidade.getId(), SlotEquipamento.ARMA, armaNova.getId());

		Unidade unidadeDepois = unidadeRepository.findById(unidade.getId()).orElseThrow();
		assertThat(unidadeDepois.getArmaItemId()).isEqualTo(armaNova.getId());
		assertThat(unidadeDepois.getArmaduraItemId()).isEqualTo(armadura.getId());
		assertThat(itemRepository.findById(armaNova.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.EQUIPADO);
		assertThat(itemRepository.findById(armaAntiga.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void trocaValidaDeArmaduraEntreModelosLiberaAntigaEEquipaNova() {
		Usuario usuario = criarUsuario("troca-armadura", "Bruna");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armaduraAntiga = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armaduraAntiga.getId(),
				StatusUnidade.DISPONIVEL);
		Item armaduraNova = criarItem(vila.getId(), ModeloItem.ARMADURA_FERRO, 1, StatusItem.DISPONIVEL);

		equipamentoService.trocar(usuario.getId(), unidade.getId(), SlotEquipamento.ARMADURA, armaduraNova.getId());

		Unidade unidadeDepois = unidadeRepository.findById(unidade.getId()).orElseThrow();
		assertThat(unidadeDepois.getArmaduraItemId()).isEqualTo(armaduraNova.getId());
		assertThat(itemRepository.findById(armaduraNova.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.EQUIPADO);
		assertThat(itemRepository.findById(armaduraAntiga.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void armaDeModeloErradoRejeitaComItemIndisponivelENadaMuda() {
		Usuario usuario = criarUsuario("modelo-errado", "Carla");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId(),
				StatusUnidade.DISPONIVEL);
		// SOLDADO exige ESPADA, não ARCO.
		Item arco = criarItem(vila.getId(), ModeloItem.ARCO, 1, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(
				() -> equipamentoService.trocar(usuario.getId(), unidade.getId(), SlotEquipamento.ARMA, arco.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
		assertThat(unidadeRepository.findById(unidade.getId()).orElseThrow().getArmaItemId()).isEqualTo(arma.getId());
		assertThat(itemRepository.findById(arco.getId()).orElseThrow().getStatus()).isEqualTo(StatusItem.DISPONIVEL);
		assertThat(itemRepository.findById(arma.getId()).orElseThrow().getStatus()).isEqualTo(StatusItem.EQUIPADO);
	}

	@Test
	void itemReservadoOuEquipadoRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("item-nao-disponivel", "Debora");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId(),
				StatusUnidade.DISPONIVEL);
		Item armaReservada = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.RESERVADO);
		Item armaEquipadaEmOutra = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);

		RegraJogoException exReservada = assertRegra(() -> equipamentoService.trocar(usuario.getId(), unidade.getId(),
				SlotEquipamento.ARMA, armaReservada.getId()));
		RegraJogoException exEquipada = assertRegra(() -> equipamentoService.trocar(usuario.getId(), unidade.getId(),
				SlotEquipamento.ARMA, armaEquipadaEmOutra.getId()));

		assertThat(exReservada.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
		assertThat(exEquipada.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void unidadeEmMasmorraRejeitaTrocaENadaMuda() {
		Usuario usuario = criarUsuario("em-masmorra", "Elisa");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId(),
				StatusUnidade.EM_MASMORRA);
		Item armaNova = criarItem(vila.getId(), ModeloItem.ESPADA, 2, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(() -> equipamentoService.trocar(usuario.getId(), unidade.getId(),
				SlotEquipamento.ARMA, armaNova.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.UNIDADE_EM_MASMORRA);
		assertThat(unidadeRepository.findById(unidade.getId()).orElseThrow().getArmaItemId()).isEqualTo(arma.getId());
		assertThat(itemRepository.findById(armaNova.getId()).orElseThrow().getStatus())
				.isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void slotFuturoRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("slot-futuro", "Fabia");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId(),
				StatusUnidade.DISPONIVEL);
		Item qualquerItem = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(() -> equipamentoService.trocar(usuario.getId(), unidade.getId(),
				SlotEquipamento.CABECA, qualquerItem.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

	@Test
	void unidadeInexistenteOuDeOutraVilaRejeitaComNaoEncontrado() {
		Usuario dono = criarUsuario("dono-unidade", "Gilda");
		Vila vilaDono = vilaService.obterParaAtualizacao(dono.getId());
		Item arma = criarItem(vilaDono.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vilaDono.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidadeDoDono = criarUnidade(vilaDono.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId(),
				StatusUnidade.DISPONIVEL);

		Usuario outro = criarUsuario("outro-usuario", "Helio");
		vilaService.obterParaAtualizacao(outro.getId());

		assertThrows(RecursoNaoEncontradoException.class,
				() -> equipamentoService.trocar(dono.getId(), 999_999_999L, SlotEquipamento.ARMA, arma.getId()));
		assertThrows(RecursoNaoEncontradoException.class, () -> equipamentoService.trocar(outro.getId(),
				unidadeDoDono.getId(), SlotEquipamento.ARMA, arma.getId()));
	}

	@Test
	void itemDeOutraVilaRejeitaComItemIndisponivel() {
		Usuario usuario = criarUsuario("item-outra-vila", "Irene");
		Vila vila = vilaService.obterParaAtualizacao(usuario.getId());
		Item arma = criarItem(vila.getId(), ModeloItem.ESPADA, 1, StatusItem.EQUIPADO);
		Item armadura = criarItem(vila.getId(), ModeloItem.ARMADURA_COURO, 1, StatusItem.EQUIPADO);
		Unidade unidade = criarUnidade(vila.getId(), TipoTropa.SOLDADO, arma.getId(), armadura.getId(),
				StatusUnidade.DISPONIVEL);

		Usuario outroUsuario = criarUsuario("item-outra-vila-dono", "Julio");
		Vila outraVila = vilaService.obterParaAtualizacao(outroUsuario.getId());
		Item armaDeOutraVila = criarItem(outraVila.getId(), ModeloItem.ESPADA, 1, StatusItem.DISPONIVEL);

		RegraJogoException ex = assertRegra(() -> equipamentoService.trocar(usuario.getId(), unidade.getId(),
				SlotEquipamento.ARMA, armaDeOutraVila.getId()));

		assertThat(ex.getCodigo()).isEqualTo(CodigoErro.ITEM_INDISPONIVEL);
	}

}
