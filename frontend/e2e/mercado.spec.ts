import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado, vila com Mercado ativo e Comerciante.
test.skip(true, 'Requer backend + login; executar manualmente')

test('mercado: vender um recurso', async ({ page }) => {
  await page.goto('/jogo/mercado')
  await expect(page.getByTestId('tabela-precos')).toBeVisible()
  await page.getByTestId('select-recurso').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('enviar-ordem').click()
  await expect(page.getByTestId('sucesso-ordem')).toBeVisible()
})
