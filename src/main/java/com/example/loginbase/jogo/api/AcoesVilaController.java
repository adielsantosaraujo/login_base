package com.example.loginbase.jogo.api;

import java.time.Clock;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.construcao.ConstrucaoService;
import com.example.loginbase.jogo.economia.EstadoVila;
import com.example.loginbase.jogo.economia.VilaService;
import com.example.loginbase.jogo.fazenda.FazendaService;
import com.example.loginbase.jogo.forja.ForjaService;
import com.example.loginbase.jogo.quartel.EquipamentoService;
import com.example.loginbase.jogo.quartel.QuartelService;

/**
 * API REST das ações de vila (construção, plantio, forja, treino de tropas,
 * troca de equipamento): ver design.md, seção 17, e specs
 * game-buildings/game-farming/game-forge/game-army. Cada ação delega a
 * validação e execução da regra de jogo ao
 * serviço correspondente (que lança {@link com.example.loginbase.jogo.RegraJogoException}
 * em caso de violação, mapeada para 422/409 por {@link ErroApiHandler}) e
 * devolve o estado atualizado da vila ({@link VilaDto}), assim como
 * {@link VilaController#consultarVila}.
 *
 * <p>Requer autenticação (401 anônimo) e, por ser {@code POST}, token CSRF
 * válido (403 sem ele) — ver {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/jogo")
@Transactional
public class AcoesVilaController {

	private final ConstrucaoService construcaoService;
	private final FazendaService fazendaService;
	private final ForjaService forjaService;
	private final QuartelService quartelService;
	private final EquipamentoService equipamentoService;
	private final VilaService vilaService;
	private final JogoMapper jogoMapper;
	private final Clock clock;

	public AcoesVilaController(ConstrucaoService construcaoService, FazendaService fazendaService,
			ForjaService forjaService, QuartelService quartelService, EquipamentoService equipamentoService,
			VilaService vilaService, JogoMapper jogoMapper, Clock clock) {
		this.construcaoService = construcaoService;
		this.fazendaService = fazendaService;
		this.forjaService = forjaService;
		this.quartelService = quartelService;
		this.equipamentoService = equipamentoService;
		this.vilaService = vilaService;
		this.jogoMapper = jogoMapper;
		this.clock = clock;
	}

	@PostMapping("/predios/{tipo}/melhorar")
	public ResponseEntity<VilaDto> melhorar(@PathVariable TipoPredio tipo, Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		construcaoService.melhorar(usuarioId, tipo);
		return ResponseEntity.ok(consultarVilaAtualizada(usuarioId));
	}

	@PostMapping("/canteiros/{posicao}/plantar")
	public ResponseEntity<VilaDto> plantar(@PathVariable int posicao, @Valid @RequestBody PlantarRequest req,
			Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		fazendaService.plantar(usuarioId, posicao, req.cultivo());
		return ResponseEntity.ok(consultarVilaAtualizada(usuarioId));
	}

	@PostMapping("/forja/ordens")
	public ResponseEntity<VilaDto> forjar(@Valid @RequestBody ForjarRequest req, Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		forjaService.forjar(usuarioId, req.modelo(), req.nivel(), req.quantidade());
		return ResponseEntity.ok(consultarVilaAtualizada(usuarioId));
	}

	@PostMapping("/quartel/ordens")
	public ResponseEntity<VilaDto> treinar(@Valid @RequestBody TreinarRequest req, Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		quartelService.treinar(usuarioId, req.tipo(), req.armaNivel(), req.armaduraModelo(), req.armaduraNivel(),
				req.quantidade());
		return ResponseEntity.ok(consultarVilaAtualizada(usuarioId));
	}

	@PostMapping("/unidades/{id}/equipamento")
	public ResponseEntity<VilaDto> trocarEquipamento(@PathVariable long id,
			@Valid @RequestBody TrocarEquipamentoRequest req, Authentication auth) {
		long usuarioId = UsuarioAtual.id(auth);
		equipamentoService.trocar(usuarioId, id, req.slot(), req.itemId());
		return ResponseEntity.ok(consultarVilaAtualizada(usuarioId));
	}

	/** Estado da vila após a ação, no mesmo formato de {@code GET /api/jogo/vila}. */
	private VilaDto consultarVilaAtualizada(long usuarioId) {
		EstadoVila estado = vilaService.consultar(usuarioId);
		return jogoMapper.toVilaDto(estado, clock);
	}

}
