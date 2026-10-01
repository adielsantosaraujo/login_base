import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import { describe, expect, it } from 'vitest'
import { defineComponent } from 'vue'
import CasamentoDialog from './CasamentoDialog.vue'

const m = (id: number, nome: string, idadeAnos: number, estadoCivil = 'SOLTEIRO') => ({
  id, nome, sexo: 'M', idadeAnos, estadoCivil, conjugeId: null,
})
const familias = [
  { id: 1, vilaId: 1, sobrenome: 'Silva', casaId: 7, membros: [m(10, 'João', 30), m(11, 'Rui', 12), m(12, 'Paulo', 40, 'CASADO')] },
  { id: 2, vilaId: 1, sobrenome: 'Souza', casaId: 8, membros: [m(20, 'Ana', 25)] },
]
const casas = [
  { casaId: 7, nivel: 1, regiaoIndice: 6, x: 0, y: 0, nucleosTotal: 1, nucleosOcupados: 1, nucleosLivres: 0, vagasTotal: 4, vagasOcupadas: 2, vagasLivres: 2, familiaIds: [1] },
  { casaId: 9, nivel: 1, regiaoIndice: 6, x: 2, y: 2, nucleosTotal: 1, nucleosOcupados: 0, nucleosLivres: 1, vagasTotal: 4, vagasOcupadas: 0, vagasLivres: 4, familiaIds: [] },
]

const DialogStub = defineComponent({
  props: { visible: Boolean },
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>',
})

if (!window.matchMedia) {
  window.matchMedia = ((q: string) => ({ matches: false, media: q, addEventListener() {}, removeEventListener() {}, addListener() {}, removeListener() {} })) as never
}

function montar() {
  return mount(CasamentoDialog, {
    props: { visivel: true, primeiro: familias[0].membros[0], familias, casas } as never,
    global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub } },
  })
}

describe('CasamentoDialog', () => {
  it('lista só candidatos solteiros 18+ e casas com núcleo livre', () => {
    const w = montar()
    const [segundo, casa] = w.findAllComponents(Select)
    expect((segundo.props('options') as { id: number }[]).map((o) => o.id)).toEqual([20])
    expect((casa.props('options') as { id: number }[]).map((o) => o.id)).toEqual([9])
    w.unmount()
  })

  it('confirma desabilitado até preencher e emite a requisição', async () => {
    const w = montar()
    const botao = () => w.get('[data-testid="confirmar-casamento"]').element as HTMLButtonElement
    expect(botao().disabled).toBe(true)
    const [segundo, casa, sobrenome] = w.findAllComponents(Select)
    segundo.vm.$emit('update:modelValue', 20)
    casa.vm.$emit('update:modelValue', 9)
    await w.vm.$nextTick()
    expect(sobrenome.props('options')).toEqual(['Silva', 'Souza'])
    sobrenome.vm.$emit('update:modelValue', 'Souza')
    await w.vm.$nextTick()
    expect(botao().disabled).toBe(false)
    botao().click()
    expect(w.emitted('confirmar')?.[0][0]).toEqual({
      cidadao1Id: 10, cidadao2Id: 20, casaId: 9, sobrenomeEscolhido: 'Souza',
    })
    w.unmount()
  })

  it('mostra o erro do backend', () => {
    const w = mount(CasamentoDialog, {
      props: { visivel: true, primeiro: familias[0].membros[0], familias, casas, erro: 'Casa sem vagas' } as never,
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub } },
    })
    expect(w.find('[data-testid="erro-casamento"]').text()).toContain('Casa sem vagas')
    w.unmount()
  })
})
