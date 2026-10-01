import { expect, test } from '@playwright/test'

// Exige backend rodando e usuário com vila; execução manual.
test.skip('barra do turno exibe número e abre o relatório', async ({ page }) => {
  await page.goto('/jogo/mapa')
  await expect(page.getByTestId('turno-numero')).toBeVisible()
  await expect(page.getByTestId('turno-contagem')).toBeVisible()
  await page.getByTestId('abrir-relatorio').click()
  await expect(page.getByTestId('relatorio-turno')).toBeVisible()
})
