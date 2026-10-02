import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e vila com Ferraria ativa com artesão alocado.
test.skip(true, 'Requer backend + login; executar manualmente')

test('oficina: nova fabricação entra na fila', async ({ page }) => {
  await page.goto('/jogo/oficina/1')
  await page.getByTestId('nova-fabricacao').click()
  await page.getByTestId('select-item').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('select-nivel').click()
  await page.getByRole('option', { name: 'L1' }).click()
  await page.getByTestId('select-artesao').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('confirmar-fabricacao').click()
  await expect(page.getByTestId('fila')).toBeVisible()
})
