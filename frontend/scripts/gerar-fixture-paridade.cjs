/* Gera a fixture de paridade da distribuição a partir do JS de referência (determinístico). */
const fs = require('fs');
const path = require('path');
const ref = require('../../docs/designe/handoff/referencia/distribuicao-populacao.js');

const maiusculas = (o) => Object.fromEntries(Object.entries(o).map(([k, v]) => [k.toUpperCase(), v]));
const ORDEM_PAPEL = ['PAI', 'MAE', 'FILHO', 'FILHA'];
const SEXOS = ['M', 'F', 'M', 'F'];
const IDADES = [40, 40, 18, 18];
const nomes = [
  ['Oliveira', 'Marcos', 'Fernanda', 'André', 'Renata'],
  ['Lima', 'Felipe', 'Juliana', 'Bruno', 'Camila'],
  ['Almeida', 'Vitor', 'Patrícia', 'Diego', 'Larissa'],
  ['Pereira', 'Rafael', 'Sandra', 'Lucas', 'Beatriz'],
];
const familias = nomes.map(([sobrenome, ...n]) => ({
  sobrenome,
  cidadaos: n.map((nome, i) => ({ nome, sexo: SEXOS[i], idadeAnos: IDADES[i], papel: ORDEM_PAPEL[i] })),
}));

const converter = (c) => ({
  caracteristicas: maiusculas(c.caracteristicas),
  profissoes: { ...c.profissoes },
  principal: ref.principal(c),
});

const pessoas = {};
Object.keys(ref.PROFISSOES).forEach((p) => {
  const r = ref.distribuirPessoa(p);
  pessoas[p] = { caracteristicas: maiusculas(r.caracteristicas), profissoes: { ...r.profissoes } };
});

const planos = [
  ['plano-padrao', ref.PLANO_PADRAO],
  ['plano-cacador-costureiro', { COMERCIANTE: 1, CONSTRUTOR: 2, CARREGADOR: 2, MADEIREIRO: 2, MINEIRO: 2, AGRICULTOR: 2, FAZENDEIRO: 1, COZINHEIRO: 1, GUERREIRO: 1, FERREIRO: 0, COSTUREIRO: 1, CACADOR: 1 }],
  ['plano-construtores-carregadores', { COMERCIANTE: 1, CONSTRUTOR: 4, CARREGADOR: 4, MADEIREIRO: 1, MINEIRO: 1, AGRICULTOR: 1, FAZENDEIRO: 1, COZINHEIRO: 1, GUERREIRO: 1, FERREIRO: 1, COSTUREIRO: 0, CACADOR: 0 }],
];

const casos = planos.map(([nome, plano]) => {
  const pop = ref.distribuirPopulacao(familias, plano);
  return {
    nome,
    plano: { ...plano },
    resultado: pop.map((f) => f.cidadaos.map(converter)),
    familiaLiderSugerida: ref.familiaLiderSugerida(pop),
  };
});

const fixture = { geradoDe: 'docs/designe/handoff/referencia/distribuicao-populacao.js', pessoas, familias, casos };
const destino = path.join(__dirname, '..', 'src', 'domain', '__fixtures__', 'distribuicao-populacao.json');
fs.mkdirSync(path.dirname(destino), { recursive: true });
fs.writeFileSync(destino, JSON.stringify(fixture, null, 2) + '\n');
