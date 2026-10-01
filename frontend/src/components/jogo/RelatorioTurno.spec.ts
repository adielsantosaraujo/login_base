import { beforeAll, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import RelatorioTurno from './RelatorioTurno.vue'
import type { EventoTurno } from '../../composables/useTurno'

const eventos: EventoTurno[] = [
  { id: 1, tipo: 'NASCIMENTO', mensagem: 'Nasceu Ana', dados: null, timestamp: '2026-01-01T10:00:00Z' },
  { id: 2, tipo: 'MORTE', mensagem: 'Morreu Beto', dados: null, timestamp: '2026-01-01T10:00:01Z' },
  { id: 3, tipo: 'PRODUCAO', mensagem: 'Produziu trigo', dados: null, timestamp: '2026-01-01T10:00:02Z' },
]

const montar = (props: Record<string, unknown> = {}) =>
  mount(RelatorioTurno, {
    props: { turno: 5, eventos, ...props },
    global: { plugins: [PrimeVue] },
  })

describe('RelatorioTurno', () => {
  beforeAll(() => {
    window.matchMedia ??= ((q: string) => ({
      matches: false,
      media: q,
      addEventListener() {},
      removeEventListener() {},
      addListener() {},
      removeListener() {},
    })) as unknown as typeof window.matchMedia
  })

  it('lista todos os eventos', () => {
    const w = montar()
    expect(w.findAll('[data-testid="evento"]')).toHaveLength(3)
    expect(w.text()).toContain('Relatório do turno 5')
  })

  it('filtra por tipo', async () => {
    const w = montar()
    w.getComponent(Select).vm.$emit('update:modelValue', 'MORTE')
    await w.vm.$nextTick()
    const itens = w.findAll('[data-testid="evento"]')
    expect(itens).toHaveLength(1)
    expect(itens[0]!.text()).toContain('Morreu Beto')
  })

  it('mostra mensagem sem eventos e emite anteriores', async () => {
    const w = montar({ eventos: [] })
    expect(w.find('[data-testid="sem-eventos"]').exists()).toBe(true)
    await w.get('[data-testid="carregar-anteriores"]').trigger('click')
    expect(w.emitted('anteriores')).toHaveLength(1)
  })
})
