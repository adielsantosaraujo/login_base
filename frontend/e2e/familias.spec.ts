import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado, vila com famílias e casa com núcleo livre.
test.skip(true, 'Requer backend + login; executar manualmente')

test('famílias: listar, expandir e abrir diálogo de casamento', async ({ page }) => {
  await page.goto('/jogo/familias')
  const primeira = page.locator('[data-testid^="familia-"]').first()
  await expect(primeira).toBeVisible()
  await page.locator('[data-testid^="expandir-"]').first().click()
  await expect(page.locator('[data-testid^="membros-"]').first()).toBeVisible()
  await page.locator('[data-testid^="casar-"]:enabled').first().click()
  await expect(page.getByTestId('casamento-dialog')).toBeVisible()
})

test('famílias: casar dois solteiros', async ({ page }) => {
  await page.goto('/jogo/familias')
  await page.locator('[data-testid^="expandir-"]').first().click()
  await page.locator('[data-testid^="casar-"]:enabled').first().click()
  await page.getByTestId('select-segundo').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('select-casa').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('select-sobrenome').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('confirmar-casamento').click()
  await expect(page.getByTestId('sucesso')).toBeVisible()
})
