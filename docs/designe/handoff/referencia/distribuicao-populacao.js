/**
 * Referência — distribuição automática da população inicial (regras-populacao-v1.md, R13–R21).
 * JS puro e determinístico. Pode ser usado como está no frontend (Vue) ou portado para Java.
 */

const CARACTERISTICAS = ['vit', 'for', 'vel', 'int', 'car'];

// Ordem da tabela = critério de desempate da profissão principal (R7).
const PROFISSOES = {
  CONSTRUTOR:  ['int'],
  CARREGADOR:  ['for', 'vel'],
  AGRICULTOR:  ['int'],
  FAZENDEIRO:  ['int'],
  MINEIRO:     ['for'],
  MADEIREIRO:  ['for', 'vit'],
  FERREIRO:    ['int'],
  COZINHEIRO:  ['vel', 'car'],
  COSTUREIRO:  ['vel', 'car'],
  CACADOR:     ['vit', 'vel', 'car'],
  GUERREIRO:   ['for', 'vit', 'vel'],
  COMERCIANTE: ['car'],
};

const SECUNDARIA = {
  CONSTRUTOR: 'CARREGADOR', CARREGADOR: 'CONSTRUTOR', AGRICULTOR: 'FAZENDEIRO', FAZENDEIRO: 'AGRICULTOR',
  MINEIRO: 'MADEIREIRO', MADEIREIRO: 'MINEIRO', FERREIRO: 'MINEIRO', COZINHEIRO: 'COMERCIANTE',
  COSTUREIRO: 'COZINHEIRO', CACADOR: 'GUERREIRO', GUERREIRO: 'CACADOR', COMERCIANTE: 'COZINHEIRO',
};
const APOIO_CANDIDATAS = ['CARREGADOR', 'CONSTRUTOR', 'MADEIREIRO'];

// Ordem em que os papéis do plano são atribuídos (R13).
const ORDEM_PLANO = ['COMERCIANTE', 'CONSTRUTOR', 'CARREGADOR', 'MADEIREIRO', 'MINEIRO', 'AGRICULTOR',
  'FAZENDEIRO', 'COZINHEIRO', 'GUERREIRO', 'FERREIRO', 'COSTUREIRO', 'CACADOR'];

const PLANO_PADRAO = { COMERCIANTE: 1, CONSTRUTOR: 2, CARREGADOR: 2, MADEIREIRO: 2, MINEIRO: 2, AGRICULTOR: 2,
  FAZENDEIRO: 1, COZINHEIRO: 1, GUERREIRO: 2, FERREIRO: 1, COSTUREIRO: 0, CACADOR: 0 };

const MINIMOS = { CONSTRUTOR: 2, CARREGADOR: 2 };
const LIMITES = { caracteristicasTotal: 20, profissoesTotal: 10 }; // sem máximo por atributo (R5)
const PONTOS_PROF = [5, 3, 2]; // principal / secundária / apoio (R14)
const PESOS = { base: { vit: 0.5 }, principal: 3, secundaria: 1.5, apoio: 0.5 }; // R15
const POPULACAO = 16;

const zerado = (chaves) => Object.fromEntries(chaves.map((k) => [k, 0]));
const soma = (o) => Object.values(o).reduce((a, b) => a + b, 0);

/** Distribui os pontos de UMA pessoa para a profissão principal `prof` (R14–R15). Retorna { caracteristicas, profissoes }. */
function distribuirPessoa(prof) {
  const sec = SECUNDARIA[prof];
  const apoio = APOIO_CANDIDATAS.find((k) => k !== prof && k !== sec);
  const profissoes = zerado(Object.keys(PROFISSOES));
  profissoes[prof] = PONTOS_PROF[0];
  profissoes[sec] = PONTOS_PROF[1];
  profissoes[apoio] = PONTOS_PROF[2];

  const peso = { ...zerado(CARACTERISTICAS), ...PESOS.base };
  PROFISSOES[prof].forEach((c) => { peso[c] += PESOS.principal; });
  PROFISSOES[sec].forEach((c) => { peso[c] += PESOS.secundaria; });
  PROFISSOES[apoio].forEach((c) => { peso[c] += PESOS.apoio; });

  const caracteristicas = zerado(CARACTERISTICAS);
  for (let i = 0; i < LIMITES.caracteristicasTotal; i++) {
    let melhor = null, score = -1;
    for (const c of CARACTERISTICAS) {           // ordem fixa = desempate determinístico
      const s = peso[c] / (caracteristicas[c] + 1);
      if (s > score) { score = s; melhor = c; }
    }
    caracteristicas[melhor]++;
  }
  return { caracteristicas, profissoes };
}

/**
 * Distribui a população inteira conforme o plano (R13).
 * @param familias [{ sobrenome, cidadaos:[{ nome, sexo, idadeAnos, papel }] }] — 4 × 4, papel em ordem PAI, MAE, FILHO, FILHA
 * @param plano    { PROFISSAO: quantidade } com soma 16
 */
function distribuirPopulacao(familias, plano = PLANO_PADRAO) {
  if (soma(plano) !== POPULACAO) throw new Error('Plano deve somar ' + POPULACAO);
  const papeis = ORDEM_PLANO.flatMap((p) => Array(plano[p] || 0).fill(p));
  const resultado = familias.map((f) => ({ ...f, cidadaos: f.cidadaos.map((c) => ({ ...c })) }));
  let j = 0;
  for (let membro = 0; membro < 4; membro++) {          // rodízio: pais, mães, filhos, filhas
    for (let fam = 0; fam < resultado.length; fam++) {
      Object.assign(resultado[fam].cidadaos[membro], distribuirPessoa(papeis[j++] || 'CARREGADOR'));
    }
  }
  return resultado;
}

/* ---------- Derivados e validação ---------- */

/** Profissão principal = maior PE; empate → ordem de PROFISSOES (R7). null se tudo 0. */
function principal(cidadao) {
  let melhor = null, v = 0;
  for (const p of Object.keys(PROFISSOES)) if ((cidadao.profissoes[p] || 0) > v) { v = cidadao.profissoes[p]; melhor = p; }
  return melhor;
}

/** Troca a principal de um cidadão e ajusta o plano (R8). Retorna { cidadao, plano }. */
function trocarPrincipal(cidadao, novaProf, plano) {
  const antiga = principal(cidadao);
  const novoPlano = { ...plano };
  if (antiga && novoPlano[antiga] > 0) novoPlano[antiga]--;
  novoPlano[novaProf] = (novoPlano[novaProf] || 0) + 1;
  return { cidadao: { ...cidadao, ...distribuirPessoa(novaProf) }, plano: novoPlano };
}

/** Bônus R3 (cidadao.md): Σ floor(característica ÷ 5) das características ligadas à profissão. */
const bonusR3 = (cidadao, prof) => PROFISSOES[prof].reduce((a, c) => a + Math.floor((cidadao.caracteristicas[c] || 0) / 5), 0);

/** Líder = adulto mais velho; empate pela ordem do papel (R18). */
const ORDEM_PAPEL = ['PAI', 'MAE', 'FILHO', 'FILHA'];
function liderDaFamilia(familia) {
  return familia.cidadaos.filter((c) => c.idadeAnos >= 18)
    .sort((a, b) => b.idadeAnos - a.idadeAnos || ORDEM_PAPEL.indexOf(a.papel) - ORDEM_PAPEL.indexOf(b.papel))[0];
}
const bonusLider = (lider) => Math.min(10, Math.floor(lider.caracteristicas.car / 2)); // R19

/** Família sugerida = líder com maior CAR (R20). */
function familiaLiderSugerida(familias) {
  let idx = 0;
  familias.forEach((f, i) => { if (liderDaFamilia(f).caracteristicas.car > liderDaFamilia(familias[idx]).caracteristicas.car) idx = i; });
  return idx;
}

/** Validação completa (R3–R5, R21). Retorna lista de erros (vazia = ok). */
function validar(familias, familiaLiderIndex) {
  const erros = [];
  const todos = familias.flatMap((f) => f.cidadaos);
  if (todos.length !== POPULACAO) erros.push('POPULACAO_INCOMPLETA');
  todos.forEach((c) => {
    if (Object.values(c.caracteristicas).some((v) => v < 0) || soma(c.caracteristicas) > LIMITES.caracteristicasTotal)
      erros.push(`${c.nome}: Máximo ${LIMITES.caracteristicasTotal} pontos de característica`);
    if (Object.values(c.profissoes).some((v) => v < 0) || soma(c.profissoes) > LIMITES.profissoesTotal)
      erros.push(`${c.nome}: Máximo ${LIMITES.profissoesTotal} pontos de profissão`);
  });
  const contagem = {};
  todos.forEach((c) => { const p = principal(c); if (p) contagem[p] = (contagem[p] || 0) + 1; });
  Object.entries(MINIMOS).forEach(([p, min]) => { if ((contagem[p] || 0) < min) erros.push(`MINIMO_${p}`); });
  if (!(familiaLiderIndex >= 0 && familiaLiderIndex < familias.length)) erros.push('Escolha uma família líder');
  return erros;
}

/* ---------- Auto-teste rápido: node distribuicao-populacao.js ---------- */
if (typeof require !== 'undefined' && require.main === module) {
  const nomes = [['Oliveira', 'Marcos', 'Fernanda', 'André', 'Renata'], ['Lima', 'Felipe', 'Juliana', 'Bruno', 'Camila'],
    ['Almeida', 'Vitor', 'Patrícia', 'Diego', 'Larissa'], ['Pereira', 'Rafael', 'Sandra', 'Lucas', 'Beatriz']];
  const familias = nomes.map(([sobrenome, ...n]) => ({ sobrenome,
    cidadaos: n.map((nome, i) => ({ nome, sexo: i % 2 ? 'F' : 'M', idadeAnos: i < 2 ? 40 : 18, papel: ORDEM_PAPEL[i] })) }));
  const pop = distribuirPopulacao(familias);
  const lider = familiaLiderSugerida(pop);
  console.assert(validar(pop, lider).length === 0, validar(pop, lider));
  pop.forEach((f) => f.cidadaos.forEach((c) =>
    console.log(f.sobrenome.padEnd(9), c.nome.padEnd(9), (principal(c) || '-').padEnd(12), JSON.stringify(c.caracteristicas))));
  console.log('Família líder sugerida:', pop[lider].sobrenome, '+' + bonusLider(liderDaFamilia(pop[lider])) + '%');
}

if (typeof module !== 'undefined') {
  module.exports = { CARACTERISTICAS, PROFISSOES, SECUNDARIA, ORDEM_PLANO, PLANO_PADRAO, MINIMOS, LIMITES,
    distribuirPessoa, distribuirPopulacao, principal, bonusR3, trocarPrincipal, liderDaFamilia, bonusLider, familiaLiderSugerida, validar };
}
