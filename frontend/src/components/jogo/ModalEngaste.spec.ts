import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import { describe, expect, it } from 'vitest'
import ModalEngaste from './ModalEngaste.vue'

if (!window.matchMedia) {
  window.matchMedia = ((q: string) => ({ matches: false, media: q, addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {} })) as never
}

const pedra = { id: 3, qualidade: 'BOA', custoEngaste: 25, bonus: [{ codigo: 'ATK', magnitude: 'MEDIA', valor: 4 }] }
const comp = (id: number) => ({
  item: { id, nome: `Espada${id}`, nivel: 1, qualidade: 'EXCELENTE' }, slotsTotais: 3, slotsLivres: 2,
})

function montar(over: Record<string, unknown> = {}) {
  return mount(ModalEngaste, {
    props: { pedra, itensCompativeis: [comp(1), comp(2)], ouroDisponivel: 100, ...over } as never,
    global: { plugins: [PrimeVue] },
  })
}

describe('ModalEngaste', () => {
  it('renderiza seleção, custo e habilita Engastar após escolher item', async () => {
    const w = montar()
    expect(w.get('[data-testid="custo"]').text()).toContain('25')
    expect(w.get('[data-testid="ouro"]').text()).toContain('100')
    expect(w.get('[data-testid="bonus-pedra"]').text()).toContain('Ataque')
    const botao = () => w.get('[data-testid="confirmar-engaste"]')
    expect(botao().attributes('disabled')).toBeDefined()
    await w.findComponent(Select).vm.$emit('update:modelValue', 2)
    await flushPromises()
    expect(botao().attributes('disabled')).toBeUndefined()
    await botao().trigger('click')
    expect(w.emitted('engastar')?.[0]).toEqual([{ pedraId: 3, itemId: 2 }])
  })

  it('desabilita com ouro insuficiente', async () => {
    const w = montar({ ouroDisponivel: 15 })
    await w.findComponent(Select).vm.$emit('update:modelValue', 1)
    await flushPromises()
    expect(w.get('[data-testid="confirmar-engaste"]').attributes('disabled')).toBeDefined()
    expect(w.get('[data-testid="bloqueio"]').text()).toContain('faltam 10 Ouro')
  })

  it('avisa quando não há itens compatíveis e mostra erro', () => {
    const w = montar({ itensCompativeis: [], erro: 'Sem Ferraria ativa' })
    expect(w.get('[data-testid="bloqueio"]').text()).toContain('Sem itens com slots disponíveis')
    expect(w.get('[role="alert"]').text()).toBe('Sem Ferraria ativa')
    expect(w.get('[data-testid="confirmar-engaste"]').attributes('disabled')).toBeDefined()
  })

  it('emite fechar', async () => {
    const w = montar()
    await w.get('[data-testid="fechar-ModalEngaste"]').trigger('click')
    expect(w.emitted('fechar')).toBeTruthy()
  })
})
