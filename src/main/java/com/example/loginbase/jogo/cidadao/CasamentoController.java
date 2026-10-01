package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.VilaAtual;

@RestController
@RequestMapping("/api/jogo")
public class CasamentoController {

	public record CasamentoRequest(Long cidadao1Id, Long cidadao2Id, Long casaId, String sobrenomeEscolhido) {
	}

	public record MembroDTO(Long id, String nome, Sexo sexo, int idadeAnos, String estadoCivil, Long conjugeId) {
	}

	public record NucleoDTO(Long id, Long vilaId, String sobrenome, Long casaId, List<MembroDTO> membros) {
	}

	private final VilaAtual vilaAtual;
	private final CasamentoService casamentoService;
	private final OcupacaoCasasService ocupacaoCasas;
	private final FamiliaRepository familiaRepository;
	private final CidadaoRepository cidadaoRepository;

	public CasamentoController(VilaAtual vilaAtual, CasamentoService casamentoService,
			OcupacaoCasasService ocupacaoCasas, FamiliaRepository familiaRepository,
			CidadaoRepository cidadaoRepository) {
		this.vilaAtual = vilaAtual;
		this.casamentoService = casamentoService;
		this.ocupacaoCasas = ocupacaoCasas;
		this.familiaRepository = familiaRepository;
		this.cidadaoRepository = cidadaoRepository;
	}

	@PostMapping("/casamento")
	public NucleoDTO casar(@RequestBody CasamentoRequest req) {
		Vila vila = vilaAtual.obter();
		Familia nova = casamentoService.casar(vila.getId(), req.cidadao1Id(), req.cidadao2Id(), req.casaId(),
				req.sobrenomeEscolhido());
		return nucleo(nova, cidadaoRepository.findByFamiliaIdAndVivoTrue(nova.getId()));
	}

	/** Famílias da vila que têm membros vivos. */
	@GetMapping("/familias")
	public List<NucleoDTO> familias() {
		Vila vila = vilaAtual.obter();
		Map<Long, List<Cidadao>> porFamilia = cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()).stream()
				.collect(Collectors.groupingBy(Cidadao::getFamiliaId));
		List<NucleoDTO> resultado = new ArrayList<>();
		for (Familia f : familiaRepository.findByVilaId(vila.getId())) {
			List<Cidadao> membros = porFamilia.get(f.getId());
			if (membros != null) {
				resultado.add(nucleo(f, membros));
			}
		}
		resultado.sort(Comparator.comparing(NucleoDTO::id));
		return resultado;
	}

	@GetMapping("/casas")
	public List<OcupacaoCasasService.OcupacaoCasa> casas() {
		return ocupacaoCasas.casasAtivas(vilaAtual.obter().getId());
	}

	private static NucleoDTO nucleo(Familia f, List<Cidadao> membros) {
		List<MembroDTO> dtos = membros.stream().sorted(Comparator.comparing(Cidadao::getId))
				.map(c -> new MembroDTO(c.getId(), c.getNome(), c.getSexo(), c.getIdadeAnos(),
						c.getConjugeId() != null ? "CASADO" : "SOLTEIRO", c.getConjugeId()))
				.toList();
		return new NucleoDTO(f.getId(), f.getVilaId(), f.getSobrenome(), f.getCasaId(), dtos);
	}

}
