package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores.Trabalhador;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;

/** Armazéns ATIVOS da vila com seus Carregadores alocados e a eficiência média deles. */
@Component
@Primary
public class FonteCapacidadeArmazensReal implements FonteCapacidadeArmazens {

	private final ConstrucaoRepository construcaoRepository;
	private final ConsultaTrabalhadores consulta;

	public FonteCapacidadeArmazensReal(ConstrucaoRepository construcaoRepository, ConsultaTrabalhadores consulta) {
		this.construcaoRepository = construcaoRepository;
		this.consulta = consulta;
	}

	@Override
	public List<ArmazemAtivo> armazensAtivos(Vila vila) {
		return construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA).stream()
				.filter(c -> c.getTipo() == TipoConstrucao.ARMAZEM)
				.map(c -> {
					List<Trabalhador> carregadores = consulta.trabalhadores(c, vila).stream()
							.filter(t -> t.profissao() == Profissao.CARREGADOR).toList();
					BigDecimal media = carregadores.isEmpty() ? BigDecimal.ZERO
							: BigDecimal.valueOf(carregadores.stream().mapToDouble(Trabalhador::eficiencia).average()
									.orElse(0)).setScale(4, RoundingMode.HALF_UP);
					return new ArmazemAtivo(c.getNivel(), carregadores.size(), media);
				}).toList();
	}

}
