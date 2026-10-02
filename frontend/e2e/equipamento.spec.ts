import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado, cidadão 1 e item compatível no inventário.
test.skip(true, 'Requer backend + login; executar manualmente')

test('equipamento: equipar e remover arma', async ({ page }) => {
  await page.goto('/jogo/cidadao/1')
  await page.getByRole('tab', { name: 'Equipamento' }).click()
  await page.getByTestId('equipar-ARMA').click()
  await page.getByTestId('seletor-inventario').getByRole('button', { name: 'Selecionar' }).first().click()
  await expect(page.getByTestId('slot-ARMA').getByTestId('item-nome')).toBeVisible()
  await page.getByTestId('remover-ARMA').click()
  await expect(page.getByTestId('slot-ARMA')).toContainText('Vazio')
})
