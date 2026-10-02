import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import { describe, expect, it } from 'vitest'
import type { ReceitaDTO } from '../../composables/useOficina'
import FabricacaoModal from './FabricacaoModal.vue'

if (!window.matchMedia) {
  window.matchMedia = ((q: string) => ({ matches: false, media: q, addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {} })) as never
}

const oficina = {
  id: 1, tipo: 'FERRARIA', nivel: 'N1', estado: 'ATIVA', nivelMaximoItem: 3,
  artesaos: [
    { cidadaoId: 1, nome: 'Ana', peEfetivo: 5, eficiencia: 1, ocupado: false },
    { cidadaoId: 2, nome: 'Bia', peEfetivo: 0, eficiencia: 1, ocupado: false },
    { cidadaoId: 3, nome: 'Caio', peEfetivo: 9, eficiencia: 1, ocupado: true },
  ],
}
const receitas: ReceitaDTO[] = [
  { subtipo: 'ESPADA', nome: 'Espada', receitaBase: { FERRO: 5 }, custoPorNivel: { '1': { FERRO: 5 }, '2': { FERRO: 8 } },
    peMinimoPorNivel: { '1': 0, '2': 2, '3': 4 }, exigeAtributo: false },
  { subtipo: 'ANEL', nome: 'Anel', receitaBase: { OURO: 3 }, custoPorNivel: { '1': { OURO: 3 } },
    peMinimoPorNivel: { '1': 0 }, exigeAtributo: true },
]

function montar(estoque?: Record<string, number>) {
  return mount(FabricacaoModal, { props: { oficina, receitas, estoque }, global: { plugins: [PrimeVue] } })
}
const ORDEM = ['select-item', 'select-nivel', 'select-artesao', 'select-atributo']
const sel = (w: ReturnType<typeof montar>, id: string) => w.findAllComponents(Select)[ORDEM.indexOf(id)]

describe('FabricacaoModal', () => {
  it('oferece níveis de 1 ao máximo da oficina', async () => {
    const w = montar()
    const opts = (w.findAllComponents(Select)[1].props('options') as { value: number }[]).map((o) => o.value)
    expect(opts).toEqual([1, 2, 3])
  })

  it('desabilita artesão ocupado ou com PE abaixo do mínimo e mostra custo', async () => {
    const w = montar({ FERRO: 6 })
    sel(w, 'select-item').vm.$emit('update:modelValue', 'ESPADA')
    await flushPromises()
    sel(w, 'select-nivel').vm.$emit('update:modelValue', 2)
    await flushPromises()
    const opts = w.findAllComponents(Select)[2].props('options') as { value: number; disabled: boolean }[]
    expect(opts.find((o) => o.value === 1)?.disabled).toBe(false)
    expect(opts.find((o) => o.value === 2)?.disabled).toBe(true)
    expect(opts.find((o) => o.value === 3)?.disabled).toBe(true)
    expect(w.get('[data-testid="custo-FERRO"]').text()).toContain('8')
    expect(w.find('[data-testid="aviso-recursos"]').exists()).toBe(true)
  })

  it('exige atributo só quando a receita pede e emite fabricar', async () => {
    const w = montar()
    sel(w, 'select-item').vm.$emit('update:modelValue', 'ANEL')
    sel(w, 'select-nivel').vm.$emit('update:modelValue', 1)
    await flushPromises()
    sel(w, 'select-artesao').vm.$emit('update:modelValue', 1)
    await flushPromises()
    expect(w.find('[data-testid="select-atributo"]').exists()).toBe(true)
    expect(w.get('[data-testid="confirmar-fabricacao"]').attributes('disabled')).toBeDefined()
    sel(w, 'select-atributo').vm.$emit('update:modelValue', 'FOR')
    await flushPromises()
    await w.get('[data-testid="confirmar-fabricacao"]').trigger('click')
    expect(w.emitted('fabricar')![0][0]).toEqual({ subtipo: 'ANEL', nivel: 1, artesaoId: 1, atributoEscolhido: 'FOR' })
  })
})
