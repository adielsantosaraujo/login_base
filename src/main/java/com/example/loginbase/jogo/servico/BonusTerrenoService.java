package com.example.loginbase.jogo.servico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;

/** Bônus de terreno: do prédio pela âncora e de grupo pela média das âncoras. */
@Service
public class BonusTerrenoService {

	private static final BigDecimal CEM = BigDecimal.valueOf(100);

	private final ConstrucaoRepository construcaoRepository;
	private final RegiaoRepository regiaoRepository;
	private final LadrilhoRepository ladrilhoRepository;

	public BonusTerrenoService(ConstrucaoRepository construcaoRepository, RegiaoRepository regiaoRepository,
			LadrilhoRepository ladrilhoRepository) {
		this.construcaoRepository = construcaoRepository;
		this.regiaoRepository = regiaoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
	}

	@Transactional(readOnly = true)
	public int bonusDoPredio(Long vilaId, Construcao c) {
		Optional<TipoTerreno> terreno = ConstrucaoCatalogo.terreno(c.getTipo());
		if (terreno.isEmpty()) {
			return 0;
		}
		return regiaoRepository.findByVilaIdAndIndice(vilaId, c.getRegiaoIndice())
				.flatMap(r -> ladrilhoRepository.findByRegiaoIdAndXAndY(r.getId(), c.getX(), c.getY()))
				.filter(l -> l.getTerreno() == terreno.get())
				.map(Ladrilho::getBonusTotal)
				.orElse(0);
	}

	@Transactional(readOnly = true)
	public BigDecimal fatorDoPredio(Long vilaId, Construcao c) {
		return BigDecimal.ONE.add(BigDecimal.valueOf(bonusDoPredio(vilaId, c)).divide(CEM, 2, RoundingMode.HALF_UP));
	}

	@Transactional(readOnly = true)
	public BigDecimal media(Long vilaId, GrupoBonusVila g) {
		Map<Integer, List<Construcao>> porRegiao = new HashMap<>();
		for (Construcao c : construcaoRepository.findByVilaId(vilaId)) {
			if (g.tipos().contains(c.getTipo())
					&& (c.getEstado() == EstadoConstrucao.ATIVA || c.getEstado() == EstadoConstrucao.EM_UPGRADE)) {
				porRegiao.computeIfAbsent(c.getRegiaoIndice(), k -> new java.util.ArrayList<>()).add(c);
			}
		}
		int soma = 0;
		int qtd = 0;
		for (Map.Entry<Integer, List<Construcao>> e : porRegiao.entrySet()) {
			Optional<Regiao> regiao = regiaoRepository.findByVilaIdAndIndice(vilaId, e.getKey());
			if (regiao.isEmpty()) {
				continue;
			}
			Map<String, Ladrilho> mapa = new HashMap<>();
			for (Ladrilho l : ladrilhoRepository.findByRegiaoIdOrderByYAscXAsc(regiao.get().getId())) {
				mapa.put(l.getX() + "," + l.getY(), l);
			}
			for (Construcao c : e.getValue()) {
				Ladrilho ancora = mapa.get(c.getX() + "," + c.getY());
				if (ancora != null && ancora.getTerreno() == g.terreno()) {
					soma += ancora.getBonusTotal();
					qtd++;
				}
			}
		}
		if (qtd == 0) {
			return BigDecimal.ZERO.setScale(2);
		}
		return BigDecimal.valueOf(soma).divide(BigDecimal.valueOf(qtd), 2, RoundingMode.HALF_UP);
	}

	@Transactional(readOnly = true)
	public BigDecimal fator(Long vilaId, GrupoBonusVila g) {
		return BigDecimal.ONE.add(media(vilaId, g).divide(CEM, 4, RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP);
	}

}
