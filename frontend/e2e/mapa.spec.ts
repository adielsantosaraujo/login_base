import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e vila criada.
test.skip(true, 'Requer backend + login; executar manualmente')

test('mapa: abrir região e voltar', async ({ page }) => {
  await page.goto('/jogo/mapa')
  await expect(page.getByRole('heading', { name: 'Mapa da vila' })).toBeVisible()
  await expect(page.locator('.celula')).toHaveCount(16)
  await page.getByTestId('regiao-6').click()
  await expect(page.getByTestId('ladrilho')).toHaveCount(100)
  await page.getByTestId('voltar').click()
  await expect(page.locator('.celula')).toHaveCount(16)
})

test.skip('mapa: anexar região adjacente', async ({ page }) => {
  await page.goto('/jogo/mapa')
  await page.locator('.celula.anexavel').first().click()
  await expect(page.getByTestId('dialogo-anexacao')).toBeVisible()
  await page.getByTestId('confirmar-anexacao').click()
  await expect(page.getByTestId('mensagem-anexacao')).toBeVisible()
})
