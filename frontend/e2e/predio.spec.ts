import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado, vila com prédio ATIVA e cidadãos elegíveis.
test.skip(true, 'Requer backend + login; executar manualmente')

test('painel do prédio: alocar e desalocar trabalhador', async ({ page }) => {
  await page.goto('/jogo/regiao/6')
  await page.locator('[data-testid="ladrilho"][data-x="5"][data-y="5"]').click()
  await expect(page.getByTestId('vagas')).toContainText('0/')
  await page.getByTestId('select-cidadao').click()
  await page.getByRole('option').first().click()
  await page.getByTestId('alocar').click()
  await expect(page.getByTestId('erro-alocacao')).toHaveCount(0)
  await page.locator('[data-testid^="desalocar-"]').first().click()
})
