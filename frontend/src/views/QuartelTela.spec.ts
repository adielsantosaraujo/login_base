import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/http', () => ({ get: vi.fn(), post: vi.fn(), del: vi.fn() }))
vi.mock('vue-router', () => ({ useRoute: () => ({ params: { id: '4' } }) }))

import { del, get, post } from '../api/http'
import QuartelTela from './QuartelTela.vue'

const getMock = vi.mocked(get)
const postMock = vi.mocked(post)
const delMock = vi.mocked(del)

const membro = (id: number, estado = 'SAUDAVEL') => ({
  cidadaoId: id, nome: `M${id}`, posicao: 'FRENTE', idadeAnos: 20, estado, xpGuerreiro: 2.5, peGuerreiro: 1,
})
const tropa = (id: number, estado: string, membros = [membro(1)]) => ({
  id, nome: `T${id}`, estado, quartelId: 4, masmorraId: null as number | null, regiaoDestino: null as number | null,
  turnosViagem: null as number | null, turnosRestantes: null as number | null,
  totalMembros: membros.length, membros,
})
let tropas: ReturnType<typeof tropa>[]
const disp = [
  { id: 7, nome: 'Ana', idadeAnos: 20, peGuerreiro: 1, arma: 'Espada', estado: 'SAUDAVEL', elegivel: true, motivo: null },
  { id: 8, nome: 'Bia', idadeAnos: 20, peGuerreiro: 0, arma: null, estado: 'SAUDAVEL', elegivel: false, motivo: 'Cidadão não tem arma equipada' },
]

async function montar() {
  const w = mount(QuartelTela, { global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>', props: ['to'] } } } })
  await flushPromises()
  return w
}

describe('QuartelTela', () => {
  beforeEach(() => {
    getMock.mockReset(); postMock.mockReset(); delMock.mockReset()
    tropas = [tropa(1, 'AQUARTELADA', [membro(1), membro(2, 'FERIDO')]), tropa(2, 'EM_VIAGEM_IDA')]
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/guerreiros-disponiveis')) return disp
      return { id: 4, nivel: 'N2', estado: 'ATIVA', instrutores: 1, vagasInstrutor: 2, capacidade: 8, membrosAtuais: 3, maxTropas: 3, tropas }
    })
  })

  it('mostra cabeçalho, tropas e destaca ferido', async () => {
    const w = await montar()
    expect(w.find('[data-testid="instrutores"]').text()).toContain('1/2')
    expect(w.find('[data-testid="membros"]').text()).toContain('3/8')
    expect(w.find('[data-testid="tropas-max"]').text()).toContain('2/3')
    expect(w.find('[data-testid="estado-membro-2"]').text()).toContain('Ferido')
    expect(w.find('[data-testid="membro-1-2"]').classes()).toContain('ferido')
    expect(w.find('[data-testid="membro-1-1"]').text()).toContain('2,50')
  })

  it('só oferece edição para tropa aquartelada', async () => {
    const w = await montar()
    expect(w.find('[data-testid="adicionar-1"]').exists()).toBe(true)
    expect(w.find('[data-testid="desfazer-1"]').exists()).toBe(true)
    expect(w.find('[data-testid="adicionar-2"]').exists()).toBe(false)
    expect(w.find('[data-testid="desfazer-2"]').exists()).toBe(false)
    expect(w.find('[data-testid="remover-2-1"]').exists()).toBe(false)
  })

  it('forma tropa pelo dialog', async () => {
    postMock.mockResolvedValue(tropa(3, 'AQUARTELADA'))
    const w = await montar()
    await w.find('[data-testid="formar-tropa"]').trigger('click')
    await w.find('[data-testid="nome-tropa"]').setValue('Nova')
    await w.find('[data-testid="sel-7"]').setValue(true)
    await w.find('[data-testid="confirmar-tropa"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/quarteis/4/tropas', { nome: 'Nova', membros: [{ cidadaoId: 7, posicao: 'FRENTE' }] })
    expect(w.find('[data-testid="FormarTropaDialog"]').exists()).toBe(false)
  })

  it('exibe erro da API no dialog com role=alert e mantém aberto', async () => {
    postMock.mockRejectedValue(new Error('Já existe uma tropa com este nome'))
    const w = await montar()
    await w.find('[data-testid="formar-tropa"]').trigger('click')
    await w.find('[data-testid="nome-tropa"]').setValue('T1')
    await w.find('[data-testid="confirmar-tropa"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="erro-formar-tropa"]').attributes('role')).toBe('alert')
    expect(w.find('[data-testid="erro-formar-tropa"]').text()).toBe('Já existe uma tropa com este nome')
  })

  it('adiciona e remove membro', async () => {
    postMock.mockResolvedValue(tropa(1, 'AQUARTELADA'))
    delMock.mockResolvedValue(tropa(1, 'AQUARTELADA'))
    const w = await montar()
    await w.find('[data-testid="adicionar-1"]').trigger('click')
    await w.find('[data-testid="sel-7"]').setValue(true)
    await w.find('[data-testid="confirmar-tropa"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/tropas/1/membros', { cidadaoId: 7, posicao: 'FRENTE' })
    await w.find('[data-testid="remover-1-2"]').trigger('click')
    await flushPromises()
    expect(delMock).toHaveBeenCalledWith('/api/jogo/tropas/1/membros/2')
  })

  it('desfazer tropa pede confirmação', async () => {
    delMock.mockResolvedValue(undefined)
    const w = await montar()
    await w.find('[data-testid="desfazer-1"]').trigger('click')
    expect(delMock).not.toHaveBeenCalled()
    await w.find('[data-testid="confirmar-desfazer-nao"]').trigger('click')
    expect(w.find('[data-testid="confirmar-desfazer"]').exists()).toBe(false)
    await w.find('[data-testid="desfazer-1"]').trigger('click')
    await w.find('[data-testid="confirmar-desfazer-sim"]').trigger('click')
    await flushPromises()
    expect(delMock).toHaveBeenCalledWith('/api/jogo/tropas/1')
  })

  it('mostra erro de carregamento com role=alert', async () => {
    getMock.mockRejectedValue(new Error('Quartel não encontrado'))
    const w = await montar()
    expect(w.find('[data-testid="erro-quartel"]').attributes('role')).toBe('alert')
    expect(w.find('[data-testid="erro-quartel"]').text()).toBe('Quartel não encontrado')
  })

  it('mostra status de viagem e botão de expedição só para aquartelada', async () => {
    tropas = [tropa(1, 'AQUARTELADA'), { ...tropa(2, 'EM_VIAGEM_IDA'), regiaoDestino: 5, turnosViagem: 4, turnosRestantes: 2 }]
    const w = await montar()
    expect(w.find('[data-testid="expedicao-1"]').exists()).toBe(true)
    expect(w.find('[data-testid="expedicao-2"]').exists()).toBe(false)
    expect(w.find('[data-testid="turnos-2"]').text()).toContain('2/4')
  })

  it('abre o dialog de expedição, lista vazia e erro da API', async () => {
    tropas = [tropa(1, 'AQUARTELADA')]
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/destinos')) return []
      if (url.endsWith('/guerreiros-disponiveis')) return disp
      return { id: 4, nivel: 'N2', estado: 'ATIVA', instrutores: 1, vagasInstrutor: 2, capacidade: 8, membrosAtuais: 1, maxTropas: 3, tropas }
    })
    const w = await montar()
    await w.find('[data-testid="expedicao-1"]').trigger('click')
    await flushPromises()
    expect(getMock).toHaveBeenCalledWith('/api/jogo/tropas/1/destinos')
    expect(w.find('[data-testid="sem-masmorras"]').exists()).toBe(true)
    await w.find('[data-testid="fechar-ExpedicaoDialog"]').trigger('click')
    expect(w.find('[data-testid="ExpedicaoDialog"]').exists()).toBe(false)
  })

  it('envia expedição e recarrega o quartel', async () => {
    tropas = [tropa(1, 'AQUARTELADA')]
    const destinos = [{ masmorraId: 10, regiao: 3, nivel: 1, turnosViagem: 2, comidaNecessaria: 4, comidaDisponivel: 20 }]
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/destinos')) return destinos
      if (url.endsWith('/guerreiros-disponiveis')) return disp
      return { id: 4, nivel: 'N2', estado: 'ATIVA', instrutores: 1, vagasInstrutor: 2, capacidade: 8, membrosAtuais: 1, maxTropas: 3, tropas }
    })
    postMock.mockResolvedValue(tropa(1, 'EM_VIAGEM_IDA'))
    const w = await montar()
    await w.find('[data-testid="expedicao-1"]').trigger('click')
    await flushPromises()
    await w.find('[data-testid="sel-destino-10"]').setValue(true)
    await w.find('[data-testid="confirmar-expedicao"]').trigger('click')
    await flushPromises()
    expect(postMock).toHaveBeenCalledWith('/api/jogo/tropas/1/expedicao', { masmorraId: 10 })
    expect(w.find('[data-testid="ExpedicaoDialog"]').exists()).toBe(false)
  })

  it('mantém o dialog aberto e mostra o erro quando a API recusa', async () => {
    tropas = [tropa(1, 'AQUARTELADA')]
    const destinos = [{ masmorraId: 10, regiao: 3, nivel: 1, turnosViagem: 2, comidaNecessaria: 4, comidaDisponivel: 20 }]
    getMock.mockImplementation(async (url: string) => {
      if (url.endsWith('/destinos')) return destinos
      if (url.endsWith('/guerreiros-disponiveis')) return disp
      return { id: 4, nivel: 'N2', estado: 'ATIVA', instrutores: 1, vagasInstrutor: 2, capacidade: 8, membrosAtuais: 1, maxTropas: 3, tropas }
    })
    postMock.mockRejectedValue(new Error('Comida insuficiente para a expedição'))
    const w = await montar()
    await w.find('[data-testid="expedicao-1"]').trigger('click')
    await flushPromises()
    await w.find('[data-testid="sel-destino-10"]').setValue(true)
    await w.find('[data-testid="confirmar-expedicao"]').trigger('click')
    await flushPromises()
    expect(w.find('[data-testid="erro-expedicao"]').text()).toBe('Comida insuficiente para a expedição')
  })
})
