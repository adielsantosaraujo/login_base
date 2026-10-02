import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import { describe, expect, it } from 'vitest'
import AprimoramentoModal from './AprimoramentoModal.vue'
import DetalhesItemModal from './DetalhesItemModal.vue'

if (!window.matchMedia) {
  window.matchMedia = ((q: string) => ({ matches: false, media: q, addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {} })) as never
}

const item = (o = {}) => ({
  id: 1, categoria: 'ARMA', subtipo: 'ESPADA', nome: 'Espada', nivel: 2, qualidade: 'BOA', bonus: [{ codigo: 'FOR', valor: 2 }],
  atributoEscolhido: null, slot: null, cidadaoId: null, emAprimoramento: false,
  atributoPrincipal: { tipo: 'ATAQUE', valor: 5 }, ...o,
}) as never

const oficina = {
  id: 10, tipo: 'FERRARIA', nivel: 'N2', estado: 'ATIVA', nivelMaximoItem: 6,
  artesaos: [
    { cidadaoId: 1, nome: 'Ana', peEfetivo: 4, eficiencia: 1, ocupado: false },
    { cidadaoId: 2, nome: 'Bia', peEfetivo: 3, eficiencia: 1, ocupado: false },
    { cidadaoId: 3, nome: 'Caio', peEfetivo: 9, eficiencia: 1, ocupado: true },
  ],
}
const receitas = [{ subtipo: 'ESPADA', receitaBase: { FERRO: 3 }, custoPorNivel: { '3': { FERRO: 9 } }, peMinimoPorNivel: {}, exigeAtributo: false }]

function montar(over: Record<string, unknown> = {}) {
  return mount(AprimoramentoModal, {
    props: { item: item(), oficinas: [{ id: 10, tipo: 'FERRARIA', nivel: 'N2', tamanho: 1, estado: 'ATIVA' }], oficina, receitas, ...over },
    global: { plugins: [PrimeVue] },
  })
}

describe('AprimoramentoModal', () => {
  it('desabilita artesão ocupado ou com PE < 2(L+1)-2 e mostra custo 50%', () => {
    const w = montar()
    const opts = w.findAllComponents(Select)[1].props('options') as { value: number; disabled: boolean }[]
    expect(opts.map((o) => [o.value, o.disabled])).toEqual([[1, false], [2, true], [3, true]])
    expect(w.get('[data-testid="pe-minimo"]').text()).toContain('4')
    expect(w.get('[data-testid="custo-FERRO"]').text()).toContain('5')
  })

  it('só habilita Aprimorar com oficina e artesão válidos e emite a requisição', async () => {
    const w = montar()
    const botao = () => w.get('[data-testid="confirmar-aprimoramento"]')
    expect(botao().attributes('disabled')).toBeDefined()
    await w.findAllComponents(Select)[0].vm.$emit('update:modelValue', 10)
    await w.findAllComponents(Select)[1].vm.$emit('update:modelValue', 1)
    await flushPromises()
    expect(w.emitted('selecionar-oficina')?.[0]).toEqual([10])
    expect(botao().attributes('disabled')).toBeUndefined()
    await botao().trigger('click')
    expect(w.emitted('aprimorar')?.[0]).toEqual([{ oficinaId: 10, artesaoId: 1 }])
  })

  it('bloqueia quando faltam recursos', async () => {
    const w = montar({ estoque: { FERRO: 1 } })
    await w.findAllComponents(Select)[0].vm.$emit('update:modelValue', 10)
    await w.findAllComponents(Select)[1].vm.$emit('update:modelValue', 1)
    await flushPromises()
    expect(w.get('[data-testid="motivo"]').text()).toContain('Recursos insuficientes')
  })

  it('L10 não pode ser aprimorado', () => {
    const w = montar({ item: item({ nivel: 10 }) })
    expect(w.find('[data-testid="nivel-maximo"]').exists()).toBe(true)
    expect(w.get('[data-testid="confirmar-aprimoramento"]').attributes('disabled')).toBeDefined()
  })
})

describe('DetalhesItemModal', () => {
  it('mostra atributo principal, bônus e slots de pedra por qualidade', () => {
    const w = mount(DetalhesItemModal, { props: { item: item({ qualidade: 'EXCELENTE' }) }, global: { plugins: [PrimeVue] } })
    expect(w.get('[data-testid="atributo-principal"]').text()).toContain('5')
    expect(w.get('[data-testid="bonus"]').text()).toContain('Força')
    expect(w.findAll('[data-testid="slot-vazio"]')).toHaveLength(3)
    const s = mount(DetalhesItemModal, { props: { item: item({ qualidade: 'SIMPLES' }) }, global: { plugins: [PrimeVue] } })
    expect(s.find('[data-testid="sem-slots"]').exists()).toBe(true)
  })
})
