import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn() }))

import { get, post } from '../api/http'
import {
  custoAprimoramento, motivoArtesaoAprimoramento, nivelMaximoDaOficina, oficinaDoSubtipo, peMinimoAprimoramento,
  podeAprimorar, slotsPedra, useInventario,
} from './useInventario'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)

const item = (o = {}) => ({
  id: 1, categoria: 'ARMA', subtipo: 'ESPADA', nome: 'Espada', nivel: 2, qualidade: 'BOA', bonus: [],
  atributoEscolhido: null, slot: null, cidadaoId: null, emAprimoramento: false, atributoPrincipal: null, ...o,
}) as never

describe('useInventario', () => {
  beforeEach(() => { getMock.mockReset(); postMock.mockReset() })

  it('helpers', () => {
    expect(oficinaDoSubtipo('ESPADA')).toBe('FERRARIA')
    expect(oficinaDoSubtipo('ARCO')).toBe('CARPINTARIA')
    expect(oficinaDoSubtipo('LUVAS')).toBe('ALFAIATARIA')
    expect(slotsPedra('SIMPLES')).toBe(0)
    expect(slotsPedra('BOA')).toBe(1)
    expect(slotsPedra('EXCELENTE')).toBe(3)
    expect(slotsPedra('DIVINA')).toBe(5)
    expect(peMinimoAprimoramento(2)).toBe(4)
    expect(nivelMaximoDaOficina('N2')).toBe(6)
    expect(podeAprimorar(item({ nivel: 10 }))).toBe(false)
    expect(podeAprimorar(item({ emAprimoramento: true }))).toBe(false)
    expect(podeAprimorar(item())).toBe(true)
    const r = { subtipo: 'ESPADA', receitaBase: {}, custoPorNivel: { '3': { FERRO: 9, TABUA: 3 } }, peMinimoPorNivel: {}, exigeAtributo: false }
    expect(custoAprimoramento(r, 2)).toEqual({ FERRO: 5, TABUA: 2 })
    const a = { cidadaoId: 1, nome: 'A', peEfetivo: 3, eficiencia: 1, ocupado: false }
    expect(motivoArtesaoAprimoramento(a, 2)).toContain('PE insuficiente')
    expect(motivoArtesaoAprimoramento({ ...a, ocupado: true }, 1)).toBe('ocupado')
    expect(motivoArtesaoAprimoramento({ ...a, peEfetivo: 4 }, 2)).toBeNull()
  })

  it('carrega e filtra por categoria', async () => {
    getMock.mockResolvedValue({ itens: [item()], total: 1, page: 0, pageSize: 10 })
    const inv = useInventario()
    await inv.filtrar('ARMA')
    expect(getMock).toHaveBeenCalledWith('/api/jogo/inventario?page=0&size=10&categoria=ARMA')
    expect(inv.total.value).toBe(1)
    await inv.irParaPagina(1)
    expect(getMock).toHaveBeenLastCalledWith('/api/jogo/inventario?page=1&size=10&categoria=ARMA')
  })

  it('registra erro de carga', async () => {
    getMock.mockRejectedValue(new Error('falhou'))
    const inv = useInventario()
    await inv.carregar()
    expect(inv.erro.value).toBe('falhou')
  })

  it('lista oficinas ativas do tipo com nível suficiente', async () => {
    getMock.mockImplementation(async (url: string) => {
      if (url === '/api/jogo/vila/mapa') {
        return { vila: { id: 1, nome: 'V' }, regioes: [{ indice: 1, possuida: true }, { indice: 2, possuida: false }] }
      }
      return {
        regiao: {}, ladrilhos: [
          { x: 0, y: 0, construcao: { id: 10, tipo: 'FERRARIA', nivel: 'N1', estado: 'ATIVA' } },
          { x: 1, y: 0, construcao: { id: 10, tipo: 'FERRARIA', nivel: 'N1', estado: 'ATIVA' } },
          { x: 2, y: 0, construcao: { id: 11, tipo: 'FERRARIA', nivel: 'N1', estado: 'EM_OBRA' } },
          { x: 3, y: 0, construcao: { id: 12, tipo: 'CARPINTARIA', nivel: 'N3', estado: 'ATIVA' } },
        ],
      }
    })
    const inv = useInventario()
    const r = await inv.carregarOficinasElegiveis(item({ nivel: 2 }))
    expect(r.map((c) => c.id)).toEqual([10])
    expect(getMock).not.toHaveBeenCalledWith('/api/jogo/regioes/2')
    expect(await inv.carregarOficinasElegiveis(item({ nivel: 3 }))).toEqual([])
  })

  it('aprimora e recarrega; devolve null com erro', async () => {
    getMock.mockResolvedValue({ itens: [], total: 0, page: 0, pageSize: 10 })
    postMock.mockResolvedValueOnce({ id: 9, nivel: 3 })
    const inv = useInventario()
    expect(await inv.aprimorar(1, 4, 2)).toEqual({ id: 9, nivel: 3 })
    expect(postMock).toHaveBeenCalledWith('/api/jogo/inventario/1/aprimorar', { oficinaId: 4, artesaoId: 2 })
    postMock.mockRejectedValueOnce(new Error('Ferro insuficiente'))
    expect(await inv.aprimorar(1, 4, 2)).toBeNull()
    expect(inv.erroAcao.value).toBe('Ferro insuficiente')
  })
})
