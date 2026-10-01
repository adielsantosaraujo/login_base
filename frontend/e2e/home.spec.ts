import { expect, test } from '@playwright/test'

test('a página inicial carrega', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByText('Seja bem-vindo')).toBeVisible()
})
