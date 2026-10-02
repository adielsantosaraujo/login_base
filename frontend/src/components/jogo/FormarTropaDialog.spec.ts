import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import type { GuerreiroDisponivelDTO } from '../../composables/useQuartel'
import FormarTropaDialog from './FormarTropaDialog.vue'

const guerreiros: GuerreiroDisponivelDTO[] = [
  { id: 1, nome: 'Ana', idadeAnos: 20, peGuerreiro: 2, arma: 'Espada', estado: 'SAUDAVEL', elegivel: true, motivo: null },
  { id: 2, nome: 'Bia', idadeAnos: 30, peGuerreiro: 0, arma: null, estado: 'SAUDAVEL', elegivel: false, motivo: 'PE Guerreiro insuficiente' },
  { id: 3, nome: 'Caio', idadeAnos: 25, peGuerreiro: 1, arma: 'Espada', estado: 'SAUDAVEL', elegivel: true, motivo: null },
]

function montar(props: Record<string, unknown> = {}) {
  return mount(FormarTropaDialog, { props: { guerreiros, ...props }, global: { plugins: [PrimeVue] } })
}

describe('FormarTropaDialog', () => {
  it('desabilita inelegíveis e mostra o motivo', () => {
    const w = montar()
    expect(w.find('[data-testid="sel-2"]').attributes('disabled')).toBeDefined()
    expect(w.find('[data-testid="sel-1"]').attributes('disabled')).toBeUndefined()
    expect(w.find('[data-testid="motivo-2"]').text()).toBe('PE Guerreiro insuficiente')
  })

  it('exige nome e emite membros com posição', async () => {
    const w = montar()
    expect(w.find('[data-testid="confirmar-tropa"]').attributes('disabled')).toBeDefined()
    await w.find('[data-testid="nome-tropa"]').setValue('Lobos')
    await w.find('[data-testid="sel-1"]').setValue(true)
    await w.find('[data-testid="sel-3"]').setValue(true)
    await w.find('[data-testid="posicao-3"]').setValue('RETAGUARDA')
    await w.find('[data-testid="confirmar-tropa"]').trigger('click')
    expect(w.emitted('confirmar')![0][0]).toEqual({
      nome: 'Lobos',
      membros: [{ cidadaoId: 1, posicao: 'FRENTE' }, { cidadaoId: 3, posicao: 'RETAGUARDA' }],
    })
  })

  it('bloqueia quando excede as vagas livres', async () => {
    const w = montar({ vagasLivres: 1 })
    await w.find('[data-testid="nome-tropa"]').setValue('Lobos')
    await w.find('[data-testid="sel-1"]').setValue(true)
    await w.find('[data-testid="sel-3"]').setValue(true)
    expect(w.find('[data-testid="bloqueio"]').text()).toContain('Capacidade')
    expect(w.find('[data-testid="confirmar-tropa"]').attributes('disabled')).toBeDefined()
  })

  it('modo adicionar não pede nome e exige seleção', async () => {
    const w = montar({ modo: 'adicionar' })
    expect(w.find('[data-testid="nome-tropa"]').exists()).toBe(false)
    expect(w.find('[data-testid="confirmar-tropa"]').attributes('disabled')).toBeDefined()
    await w.find('[data-testid="sel-1"]').setValue(true)
    await w.find('[data-testid="confirmar-tropa"]').trigger('click')
    expect(w.emitted('confirmar')![0][0]).toMatchObject({ membros: [{ cidadaoId: 1, posicao: 'FRENTE' }] })
  })

  it('exibe o erro com role=alert', () => {
    const w = montar({ erro: 'Já existe uma tropa com este nome' })
    const a = w.find('[role="alert"]')
    expect(a.text()).toBe('Já existe uma tropa com este nome')
  })
})
