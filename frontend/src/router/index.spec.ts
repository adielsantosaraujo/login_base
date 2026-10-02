import { describe, expect, it } from 'vitest'
import router from './index'

describe('router', () => {
  it.each(['/', '/index'])('%s resolve para a home', (caminho) => {
    expect(router.resolve(caminho).name).toBe('home')
  })
})
