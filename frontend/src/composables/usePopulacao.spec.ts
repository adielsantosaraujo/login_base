import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/http'
import { PLANO_PADRAO, distribuirPopulacao, soma, type Papel } from '../domain/populacao'

const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))
const get = vi.fn()
const post = vi.fn()
vi.mock('../api/http', async (orig) => ({ ...(await orig<typeof import('../api/http')>()), get: (u: string) => get(u), post: (u: string, b: unknown) => post(u, b) }))
const marcar = vi.fn()
vi.mock('../router/guardaVila', () => ({ marcarPopulacaoConfirmada: () => marcar() }))

import { usePopulacao, CARACTERISTICAS, PROFISSOES, ROTULOS_PROFISSAO, bonusLider } from './usePopulacao'

function resposta() {
  const papeis: Papel[] = ['PAI', 'MAE', 'FILHO', 'FILHA']
  const base = [11, 12, 13, 14].map((fid, i) => ({
    familiaId: fid,
    sobrenome: `F${i}`,
    cidadaos: papeis.map((papel, j) => ({
      cidadaoId: fid * 10 + j, nome: `C${fid}${j}`, sexo: (j % 2 ? 'F' : 'M') as 'M' | 'F',
      idadeAnos: j < 2 ? 40 : 10, papel,
    })),
  }))
  const familias = distribuirPopulacao(base as never, PLANO_PADRAO)
  return {
    populacaoConfirmada: false,
    plano: { ...PLANO_PADRAO },
    minimos: { CONSTRUTOR: 2, CARREGADOR: 2 },
    limites: { caracteristicasTotal: 20, profissoesTotal: 10 },
    familiaLiderSugeridaId: 12,
    familiaLiderId: null,
    familias,
  }
}

async function pronto() {
  get.mockResolvedValue(resposta())
  const p = usePopulacao()
  await p.carregar()
  return p
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('usePopulacao', () => {
  it('reexporta o domínio', () => {
    expect(CARACTERISTICAS).toHaveLength(5)
    expect(PROFISSOES).toHaveLength(12)
    expect(ROTULOS_PROFISSAO.CACADOR).toBe('Caçador')
    expect(bonusLider(13)).toBe(6)
  })

  it('carrega, pré-seleciona a família sugerida e deriva estado', async () => {
    const p = await pronto()
    expect(get).toHaveBeenCalledWith('/api/jogo/vila/populacao')
    expect(p.familiaLiderId.value).toBe(12)
    expect(p.totalPlano.value).toBe(16)
    expect(p.contagemPrincipais.value.CONSTRUTOR).toBe(2)
    expect(p.valido.value).toBe(true)
    expect(p.pendentesPorFamilia.value).toHaveLength(4)
    expect(p.pendentesPorCidadao.value[110]).toEqual({ car: 0, prof: 0 })
  })

  it('erro ao carregar', async () => {
    get.mockRejectedValue(new Error('falhou'))
    const p = usePopulacao()
    await p.carregar()
    expect(p.erro.value).toBe('falhou')
    expect(p.carregando.value).toBe(false)
  })

  it('editar respeita só os totais', async () => {
    const p = await pronto()
    p.zerar()
    p.editar(110, 'VIT', 20)
    expect(p.cidadaos.value[0].caracteristicas.VIT).toBe(20)
    p.editar(110, 'FOR', 5)
    expect(p.cidadaos.value[0].caracteristicas.FOR).toBe(0)
    p.editar(110, 'CONSTRUTOR', 10)
    expect(p.cidadaos.value[0].profissoes.CONSTRUTOR).toBe(10)
    expect(p.pendentesPorCidadao.value[110]).toEqual({ car: 0, prof: 0 })
  })

  it('trocarPrincipal ajusta o plano', async () => {
    const p = await pronto()
    const c = p.cidadaos.value.find((x) => x.profissoes.CONSTRUTOR === 5)!
    p.trocarPrincipal(c.cidadaoId, 'GUERREIRO')
    expect(p.plano.value.CONSTRUTOR).toBe(1)
    expect(p.plano.value.GUERREIRO).toBe(3)
    expect(c.profissoes.GUERREIRO).toBe(5)
    expect(p.contagemPrincipais.value.CONSTRUTOR).toBe(1)
    expect(p.valido.value).toBe(false)
    expect(p.checklist.value.find((i) => i.chave === 'MINIMO_CONSTRUTOR')!.ok).toBe(false)
  })

  it('alterarPlano respeita mínimo e total 16', async () => {
    const p = await pronto()
    p.alterarPlano('CONSTRUTOR', 0)
    expect(p.plano.value.CONSTRUTOR).toBe(2)
    p.alterarPlano('COSTUREIRO', 3)
    expect(p.plano.value.COSTUREIRO).toBe(0)
    p.alterarPlano('CONSTRUTOR', 1 + 2)
    expect(p.totalPlano.value).toBe(16)
    p.alterarPlano('GUERREIRO', 1)
    p.alterarPlano('COSTUREIRO', 1)
    expect(p.totalPlano.value).toBe(16)
  })

  it('redistribuir só com plano 16 e é determinístico', async () => {
    const p = await pronto()
    p.zerar()
    p.redistribuir()
    expect(p.cidadaos.value[0].profissoes.COMERCIANTE).toBe(5)
    const antes = JSON.stringify(p.familias.value)
    p.alterarPlano('GUERREIRO', 1)
    p.zerar()
    p.redistribuir()
    expect(soma(p.cidadaos.value[0].profissoes)).toBe(0)
    p.alterarPlano('GUERREIRO', 2)
    p.redistribuir()
    expect(JSON.stringify(p.familias.value)).toBe(antes)
    expect(p.familias.value[0].familiaId).toBe(11)
  })

  it('zerar mantém plano e líder', async () => {
    const p = await pronto()
    p.zerar()
    expect(soma(p.cidadaos.value[0].caracteristicas)).toBe(0)
    expect(p.totalPlano.value).toBe(16)
    expect(p.familiaLiderId.value).toBe(12)
    expect(p.pendentesPorCidadao.value[110]).toEqual({ car: 20, prof: 10 })
  })

  it('escolherLider', async () => {
    const p = await pronto()
    p.escolherLider(14)
    expect(p.familiaLiderId.value).toBe(14)
  })

  it('confirmar envia o contrato e navega', async () => {
    post.mockResolvedValue({})
    const p = await pronto()
    expect(await p.confirmar()).toBe(true)
    const [url, corpo] = post.mock.calls[0]
    expect(url).toBe('/api/jogo/vila/populacao')
    expect(corpo.familiaLiderId).toBe(12)
    expect(corpo.familias).toHaveLength(4)
    const c = corpo.familias[0].cidadaos[0]
    expect(Object.keys(c.caracteristicas)).toEqual(['VIT', 'FOR', 'VEL', 'INT', 'CAR'])
    expect(Object.keys(c.profissoes)).toHaveLength(12)
    expect(marcar).toHaveBeenCalled()
    expect(push).toHaveBeenCalledWith('/jogo/mapa')
  })

  it('409 POPULACAO_JA_CONFIRMADA conta como sucesso', async () => {
    post.mockRejectedValue(new ApiError(409, 'já', 'POPULACAO_JA_CONFIRMADA'))
    const p = await pronto()
    expect(await p.confirmar()).toBe(true)
    expect(marcar).toHaveBeenCalled()
    expect(push).toHaveBeenCalledWith('/jogo/mapa')
  })

  it('outros erros e inválido não navegam', async () => {
    post.mockRejectedValue(new ApiError(400, 'ruim', 'LIMITE_CARACTERISTICAS'))
    const p = await pronto()
    expect(await p.confirmar()).toBe(false)
    expect(p.erro.value).toBe('ruim')
    expect(push).not.toHaveBeenCalled()
    p.escolherLider(999)
    post.mockClear()
    expect(await p.confirmar()).toBe(false)
    expect(post).not.toHaveBeenCalled()
  })
})
