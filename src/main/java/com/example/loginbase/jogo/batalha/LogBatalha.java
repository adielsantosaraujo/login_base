package com.example.loginbase.jogo.batalha;

import java.util.List;

/**
 * Conteúdo da coluna {@code batalha.log} (jsonb): {@code {"participantes":[{id,lado,nome,pvMax,pv}],
 * "acoes":[{rodada,atacanteId,atacanteLado,alvoId,alvoLado,dano,critico,pvAntes,pvDepois,abatido}]}}.
 * Em {@code participantes}, {@code pv} é o PV final.
 */
public record LogBatalha(List<ResultadoBatalha.PvFinal> participantes, List<AcaoBatalha> acoes) {
}
