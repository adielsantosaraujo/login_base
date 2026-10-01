import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e vila recém-criada.
test.skip(true, 'Requer backend + login; executar manualmente')

test('população: distribuir pontos, escolher líder e confirmar', async ({ page }) => {
  await page.goto('/jogo/populacao')
  await expect(page.getByRole('heading', { name: 'Distribuição da população' })).toBeVisible()
  const form = page.locator('[data-testid^="cidadao-"]').first()
  await form.getByTestId('car-VIT').fill('10')
  await form.getByTestId('car-CAR').fill('10')
  await expect(form.getByTestId('contador-car')).toHaveText('20 / 20')
  await form.getByTestId('prof-MINEIRO').fill('5')
  await expect(form.getByTestId('contador-prof')).toHaveText('5 / 10')
  await form.getByTestId('car-FOR').fill('11')
  await expect(form.getByTestId('erro-cidadao').first()).toBeVisible()
  await form.getByTestId('car-FOR').fill('0')
  await page.locator('[data-testid^="lider-"] input').first().check()
  await page.getByTestId('confirmar').click()
  await expect(page).toHaveURL(/\/jogo\/mapa/)
})
