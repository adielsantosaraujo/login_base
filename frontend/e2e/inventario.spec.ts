import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e vila com itens no inventário e oficina ativa.
test.skip(true, 'Requer backend + login; executar manualmente')

test('inventário: lista itens e abre o modal de aprimoramento', async ({ page }) => {
  await page.goto('/jogo/inventario')
  await expect(page.getByTestId('itens')).toBeVisible()
  await page.getByRole('button', { name: 'Aprimorar' }).first().click()
  await expect(page.getByTestId('AprimoramentoModal')).toBeVisible()
  await page.getByTestId('select-oficina').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('select-artesao').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('confirmar-aprimoramento').click()
  await expect(page.getByTestId('sucesso')).toBeVisible()
})
