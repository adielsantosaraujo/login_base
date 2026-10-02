import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import ExpedicaoDialog from './ExpedicaoDialog.vue'

const membro = (id: number, estado = 'SAUDAVEL') => ({
  cidadaoId: id, nome: `M${id}`, posicao: 'FRENTE' as const, idadeAnos: 20, estado, xpGuerreiro: 0, peGuerreiro: 1,
})
const tropa = (membros = [membro(1)]) => ({
  id: 1, nome: 'T1', estado: 'AQUARTELADA' as const, quartelId: 4, masmorraId: null, regiaoDestino: null,
  turnosViagem: null, turnosRestantes: null, totalMembros: membros.length, membros,
})
const destinos = [
  { masmorraId: 10, regiao: 3, nivel: 2, turnosViagem: 2, comidaNecessaria: 8, comidaDisponivel: 20 },
  { masmorraId: 11, regiao: 7, nivel: 1, turnosViagem: 5, comidaNecessaria: 30, comidaDisponivel: 20 },
]
const montar = (props: Record<string, unknown>) =>
  mount(ExpedicaoDialog, { props: { tropa: tropa(), destinos, ...props } as never, global: { plugins: [PrimeVue] } })

describe('ExpedicaoDialog', () => {
  it('lista destinos e desabilita o de comida insuficiente', () => {
    const w = montar({})
    expect(w.find('[data-testid="destino-10"]').text()).toContain('Região 3')
    expect(w.find('[data-testid="destino-10"]').text()).toContain('8 necessária / 20 disponível')
    expect(w.find('[data-testid="sel-destino-11"]').attributes('disabled')).toBeDefined()
    expect(w.find('[data-testid="aviso-comida-11"]').exists()).toBe(true)
    expect(w.find('[data-testid="aviso-comida-10"]').exists()).toBe(false)
  })

  it('lista vazia mostra Nenhuma masmorra ativa', () => {
    const w = montar({ destinos: [] })
    expect(w.find('[data-testid="sem-masmorras"]').text()).toBe('Nenhuma masmorra ativa.')
  })

  it('confirma com a masmorra escolhida', async () => {
    const w = montar({})
    expect(w.find('[data-testid="confirmar-expedicao"]').attributes('disabled')).toBeDefined()
    await w.find('[data-testid="sel-destino-10"]').setValue(true)
    await w.find('[data-testid="confirmar-expedicao"]').trigger('click')
    expect(w.emitted('confirmar')![0]).toEqual([10])
  })

  it('avisa e bloqueia tropa com ferido ou sem membros', async () => {
    const f = montar({ tropa: tropa([membro(1), membro(2, 'FERIDO')]) })
    expect(f.find('[data-testid="aviso-ferido"]').exists()).toBe(true)
    await f.find('[data-testid="sel-destino-10"]').setValue(true)
    expect(f.find('[data-testid="confirmar-expedicao"]').attributes('disabled')).toBeDefined()
    const v = montar({ tropa: tropa([]) })
    expect(v.find('[data-testid="aviso-sem-membros"]').exists()).toBe(true)
  })

  it('exibe o erro da API com role alert e emite fechar', async () => {
    const w = montar({ erro: 'Comida insuficiente para a expedição' })
    expect(w.find('[role="alert"]').text()).toBe('Comida insuficiente para a expedição')
    await w.find('[data-testid="fechar-ExpedicaoDialog"]').trigger('click')
    expect(w.emitted('fechar')).toBeTruthy()
  })
})
