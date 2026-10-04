/**
 * Referência — geração do mapa 4×4 (regras-regioes-v2.md, R8–R15).
 * JS puro, sem dependências. Portar para Java (ex.: GeradorMapaService) usando um
 * java.util.Random(semente) para que a prévia exibida seja reproduzível.
 */

const TIPOS = ['FLORESTA', 'PLANICIE', 'URBANA', 'LITORAL', 'MONTANHA'];

const BONUS_POR_TIPO = {
  FLORESTA: ['FLORESTA', 'BARREIRO', 'PLANTACOES'],
  PLANICIE: ['PLANTACOES', 'CRIACOES', 'FLORESTA'],
  URBANA:   ['INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'],
  LITORAL:  ['SALINAS', 'ENXOFRE', 'MILITAR'],
  MONTANHA: ['ROCHA', 'FERRO', 'CARVAO'],
};

// Faixas por posição sorteada (1º, 2º, 3º) — limites inclusivos.
const FAIXAS = [[35, 50], [16, 34], [5, 15]];

const TOTAL_REGIOES = 16, MIN_POR_TIPO = 2, MAX_POR_TIPO = 4, MAX_TIPOS_COM_4 = 2;

/** PRNG determinístico (mulberry32). Em Java, use java.util.Random(semente). */
function criarRng(semente) {
  let a = semente >>> 0;
  return () => {
    a |= 0; a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
const inteiro = (rng, min, max) => min + Math.floor(rng() * (max - min + 1));
function embaralhar(rng, arr) {
  for (let i = arr.length - 1; i > 0; i--) { const j = inteiro(rng, 0, i); [arr[i], arr[j]] = [arr[j], arr[i]]; }
  return arr;
}

/** Etapa 1a — quantidades por tipo (R8–R10). Amostragem por rejeição. */
function sortearQuantidades(rng) {
  for (;;) {
    const q = TIPOS.map(() => inteiro(rng, MIN_POR_TIPO, MAX_POR_TIPO));
    const soma = q.reduce((a, b) => a + b, 0);
    const com4 = q.filter((x) => x === MAX_POR_TIPO).length;
    if (soma === TOTAL_REGIOES && com4 <= MAX_TIPOS_COM_4) {
      return Object.fromEntries(TIPOS.map((t, i) => [t, q[i]]));
    }
  }
}

/** Gera as 16 regiões. Retorna [{ indice, tipo, bonus:[{bonus,posicao,valor}] }]. */
function gerarMapa(semente) {
  const rng = criarRng(semente);
  // Etapa 1 — tipos (R8–R11)
  const qtd = sortearQuantidades(rng);
  const layout = embaralhar(rng, TIPOS.flatMap((t) => Array(qtd[t]).fill(t)));
  // Etapa 2 — bônus (R12–R15): sorteia a ordem, depois o valor pela posição
  return layout.map((tipo, i) => {
    const ordem = embaralhar(rng, BONUS_POR_TIPO[tipo].slice());
    return {
      indice: i + 1,
      tipo,
      bonus: ordem.map((b, pos) => ({ bonus: b, posicao: pos + 1, valor: inteiro(rng, ...FAIXAS[pos]) })),
    };
  });
}

/* ---------- Vizinhança e validação da escolha (R2–R4) ---------- */
const linha = (indice) => Math.floor((indice - 1) / 4);
const coluna = (indice) => (indice - 1) % 4;
const vizinhas = (a, b) => Math.abs(linha(a) - linha(b)) + Math.abs(coluna(a) - coluna(b)) === 1;

/** Valida a seleção (índices 1..16 em ordem de clique). Retorna null se ok, ou o código do erro. */
function validarSelecao(mapa, indices) {
  if (indices.length !== 3 || new Set(indices).size !== 3 || indices.some((i) => i < 1 || i > 16)) return 'SELECAO_INVALIDA';
  for (let k = 1; k < indices.length; k++) {
    if (!indices.slice(0, k).some((j) => vizinhas(indices[k], j))) return 'REGIAO_NAO_ADJACENTE';
  }
  if (!indices.some((i) => mapa[i - 1].tipo === 'URBANA')) return 'SEM_REGIAO_URBANA';
  return null;
}

/** Soma dos bônus das regiões escolhidas (R17). */
function somarBonus(mapa, indices) {
  const total = {};
  indices.forEach((i) => mapa[i - 1].bonus.forEach(({ bonus, valor }) => { total[bonus] = (total[bonus] || 0) + valor; }));
  return total;
}

/* ---------- Auto-teste rápido: node geracao-mapa.js ---------- */
if (typeof require !== 'undefined' && require.main === module) {
  for (let s = 1; s <= 2000; s++) {
    const m = gerarMapa(s);
    const c = {}; m.forEach((r) => { c[r.tipo] = (c[r.tipo] || 0) + 1; });
    const q = Object.values(c);
    console.assert(m.length === 16, 'deve ter 16 regiões');
    console.assert(TIPOS.every((t) => c[t] >= 2 && c[t] <= 4), 'cada tipo 2..4', c);
    console.assert(q.filter((x) => x === 4).length <= 2, 'no máx. 2 tipos com 4', c);
    m.forEach((r) => r.bonus.forEach((b) => {
      const [mn, mx] = FAIXAS[b.posicao - 1];
      console.assert(b.valor >= mn && b.valor <= mx, 'faixa', b);
    }));
  }
  console.log('OK — exemplo semente 42:', JSON.stringify(gerarMapa(42).slice(0, 2)));
}

if (typeof module !== 'undefined') {
  module.exports = { TIPOS, BONUS_POR_TIPO, FAIXAS, gerarMapa, sortearQuantidades, vizinhas, validarSelecao, somarBonus };
}
