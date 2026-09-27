// Funções de chamada à API do jogo (`/api/jogo/**`), ver design.md seção 17.

import { request } from './http'
import type {
  AcaoCombateRequest,
  BatalhaDto,
  CatalogoDto,
  ForjarRequest,
  IniciarBatalhaRequest,
  ModeloItem,
  PlantarRequest,
  TreinarRequest,
  TipoPredio,
  TipoTropa,
  Cultivo,
  VilaDto,
} from './tipos'

export const jogoAPI = {
  async catalogo(): Promise<CatalogoDto> {
    return request('/api/jogo/catalogo')
  },

  async consultarVila(): Promise<VilaDto> {
    return request('/api/jogo/vila')
  },

  async melhorarPredio(tipo: TipoPredio): Promise<VilaDto> {
    return request(`/api/jogo/predios/${tipo}/melhorar`, { method: 'POST' })
  },

  async plantar(posicao: number, cultivo: Cultivo): Promise<VilaDto> {
    const corpo: PlantarRequest = { cultivo }
    return request(`/api/jogo/canteiros/${posicao}/plantar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(corpo),
    })
  },

  async forjar(modelo: ModeloItem, nivel: number, quantidade: number): Promise<VilaDto> {
    const corpo: ForjarRequest = { modelo, nivel, quantidade }
    return request('/api/jogo/forja/ordens', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(corpo),
    })
  },

  async treinar(tipo: TipoTropa, armaId: number, armaduraId: number): Promise<VilaDto> {
    const corpo: TreinarRequest = { tipo, armaId, armaduraId }
    return request('/api/jogo/quartel/ordens', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(corpo),
    })
  },

  async iniciarBatalha(nivel: number, unidadeIds: number[]): Promise<BatalhaDto> {
    const corpo: IniciarBatalhaRequest = { unidadeIds }
    return request(`/api/jogo/masmorras/${nivel}/batalhas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(corpo),
    })
  },

  async consultarBatalha(id: number): Promise<BatalhaDto> {
    return request(`/api/jogo/batalhas/${id}`)
  },

  async agir(id: number, acao: AcaoCombateRequest): Promise<BatalhaDto> {
    return request(`/api/jogo/batalhas/${id}/acoes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(acao),
    })
  },
}
