import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado, vila e prédio ATIVA com recursos.
test.skip(true, 'Requer backend + login; executar manualmente')

test('upgrade: melhorar um prédio ativo', async ({ page }) => {
  await page.goto('/jogo/regiao/6')
  await page.locator('[data-testid="ladrilho"][data-x="5"][data-y="5"]').click()
  await page.getByTestId('confirmar-upgrade').click()
  await expect(page.getByTestId('erro-upgrade')).toHaveCount(0)
})
