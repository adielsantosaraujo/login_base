import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import ExpedicaoStatus from './ExpedicaoStatus.vue'

const tropa = (estado: string, restantes: number | null) => ({
  id: 1, estado: estado as never, regiaoDestino: 5, turnosViagem: 4, turnosRestantes: restantes,
})
const montar = (t: ReturnType<typeof tropa>) => mount(ExpedicaoStatus, { props: { tropa: t }, global: { plugins: [PrimeVue] } })

describe('ExpedicaoStatus', () => {
  it('mostra ida, região e turnos restantes', () => {
    const w = montar(tropa('EM_VIAGEM_IDA', 3))
    expect(w.find('[data-testid="fase-1"]').text()).toBe('Ida')
    expect(w.find('[data-testid="regiao-1"]').text()).toBe('5')
    expect(w.find('[data-testid="turnos-1"]').text()).toContain('3/4')
    expect(w.find('[data-testid="progresso-1"]').attributes('aria-valuenow')).toBe('25')
  })

  it('mostra volta', () => {
    const w = montar(tropa('EM_VIAGEM_VOLTA', 1))
    expect(w.find('[data-testid="fase-1"]').text()).toBe('Volta')
    expect(w.find('[data-testid="progresso-1"]').attributes('aria-valuenow')).toBe('75')
  })

  it('não renderiza para tropa aquartelada', () => {
    expect(montar(tropa('AQUARTELADA', null)).find('[data-testid="expedicao-status-1"]').exists()).toBe(false)
  })
})
