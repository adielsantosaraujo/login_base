import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e um prédio de coleta ATIVO.
test.skip(true, 'Requer backend + login; executar manualmente')

test('marcação: marcar e desmarcar ladrilho adjacente à jazida', async ({ page }) => {
  await page.goto('/jogo/regiao/6')
  await page.getByTestId('menu-marcacao').click()
  await page.locator('[data-testid="PainelMarcacao"] [data-testid="ladrilho"][data-x="3"][data-y="0"]').click()
  await expect(page.getByTestId('marcacao-contador')).toContainText('1/4')
})
