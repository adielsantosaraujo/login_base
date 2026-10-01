import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e vila criada.
test.skip(true, 'Requer backend + login; executar manualmente')

test('região: construir uma casa em ladrilho vazio', async ({ page }) => {
  await page.goto('/jogo/regiao/6')
  await page.locator('[data-testid="ladrilho"][data-x="5"][data-y="5"]').click()
  await page.getByTestId('item-CASA').click()
  await page.getByTestId('construir').click()
  await expect(page.getByTestId('mensagem-construcao')).toHaveText('Construção iniciada')
})
