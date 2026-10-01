package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;

/**
 * Ocupação das casas (compartilhado por casamento, reprodução e imigração).
 *
 * <p>Núcleo ocupado = família com casa_id da casa e ao menos um membro vivo. Vaga ocupada = cidadão vivo dessas
 * famílias + 1 por gestação em curso (a vaga do bebê fica reservada). Capacidade pelo nível da casa
 * ({@link ConstrucaoCatalogo#nucleosCasa}/{@link ConstrucaoCatalogo#vagasCasa}: núcleos 1/2/4, vagas 4/10/24).
 * Só casas ATIVAS são consideradas.
 */
@Service
public class OcupacaoCasasService {

	/** Ocupação de uma casa. {@code familiaIds} são os núcleos ocupados. */
	public record OcupacaoCasa(Long casaId, String nivel, int regiaoIndice, int x, int y, int nucleosTotal,
			int nucleosOcupados, int nucleosLivres, int vagasTotal, int vagasOcupadas, int vagasLivres,
			List<Long> familiaIds) {
	}

	private final ConstrucaoRepository construcaoRepository;
	private final FamiliaRepository familiaRepository;
	private final CidadaoRepository cidadaoRepository;

	public OcupacaoCasasService(ConstrucaoRepository construcaoRepository, FamiliaRepository familiaRepository,
			CidadaoRepository cidadaoRepository) {
		this.construcaoRepository = construcaoRepository;
		this.familiaRepository = familiaRepository;
		this.cidadaoRepository = cidadaoRepository;
	}

	/** Todas as casas ATIVAS da vila, ordenadas por id. */
	@Transactional(readOnly = true)
	public List<OcupacaoCasa> casasAtivas(Long vilaId) {
		List<Construcao> casas = construcaoRepository.findByVilaIdAndEstado(vilaId, EstadoConstrucao.ATIVA).stream()
				.filter(c -> c.getTipo() == TipoConstrucao.CASA)
				.sorted((a, b) -> a.getId().compareTo(b.getId())).toList();
		List<OcupacaoCasa> resultado = new ArrayList<>();
		Dados dados = carregar(vilaId, Set.of());
		for (Construcao casa : casas) {
			resultado.add(calcular(casa, dados));
		}
		return resultado;
	}

	/** Ocupação de uma casa (a casa pode estar em qualquer estado; a capacidade vem do nível). */
	@Transactional(readOnly = true)
	public OcupacaoCasa ocupacao(Construcao casa) {
		return ocupacao(casa, Set.of());
	}

	/** Igual a {@link #ocupacao(Construcao)}, desconsiderando os cidadãos indicados (como se já tivessem saído). */
	@Transactional(readOnly = true)
	public OcupacaoCasa ocupacao(Construcao casa, Set<Long> cidadaosIgnorados) {
		return calcular(casa, carregar(casa.getVilaId(), cidadaosIgnorados));
	}

	private record Dados(Map<Long, List<Cidadao>> vivosPorFamilia, Map<Long, List<Long>> familiasPorCasa) {
	}

	private Dados carregar(Long vilaId, Set<Long> ignorados) {
		Map<Long, List<Cidadao>> vivos = new HashMap<>();
		for (Cidadao c : cidadaoRepository.findByVilaIdAndVivoTrue(vilaId)) {
			if (!ignorados.contains(c.getId())) {
				vivos.computeIfAbsent(c.getFamiliaId(), k -> new ArrayList<>()).add(c);
			}
		}
		Map<Long, List<Long>> porCasa = new HashMap<>();
		for (Familia f : familiaRepository.findByVilaId(vilaId)) {
			if (f.getCasaId() != null && vivos.containsKey(f.getId())) {
				porCasa.computeIfAbsent(f.getCasaId(), k -> new ArrayList<>()).add(f.getId());
			}
		}
		return new Dados(vivos, porCasa);
	}

	private OcupacaoCasa calcular(Construcao casa, Dados dados) {
		List<Long> familias = dados.familiasPorCasa().getOrDefault(casa.getId(), List.of());
		int vagasOcupadas = 0;
		Set<Long> vistos = new HashSet<>();
		for (Long familiaId : familias) {
			if (!vistos.add(familiaId)) {
				continue;
			}
			for (Cidadao c : dados.vivosPorFamilia().get(familiaId)) {
				vagasOcupadas++;
				if (c.getGestacaoTurnos() != null) {
					vagasOcupadas++;
				}
			}
		}
		int nucleosTotal = ConstrucaoCatalogo.nucleosCasa(casa.getNivel());
		int vagasTotal = ConstrucaoCatalogo.vagasCasa(casa.getNivel());
		return new OcupacaoCasa(casa.getId(), casa.getNivel().name(), casa.getRegiaoIndice(), casa.getX(),
				casa.getY(), nucleosTotal, familias.size(), Math.max(0, nucleosTotal - familias.size()), vagasTotal,
				vagasOcupadas, Math.max(0, vagasTotal - vagasOcupadas), List.copyOf(familias));
	}

}
