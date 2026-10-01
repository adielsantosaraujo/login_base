import { expect, test } from '@playwright/test'

// Execução manual: exige backend no ar, usuário logado e vila com cidadãos.
test.skip(true, 'Requer backend + login; executar manualmente')

test('painel do cidadão: abrir, ver cabeçalho e abas', async ({ page }) => {
  await page.goto('/jogo/familias')
  await page.locator('[data-testid^="expandir-"]').first().click()
  await page.locator('[data-testid^="membro-"] a').first().click()
  await expect(page.getByTestId('cabecalho')).toBeVisible()
  await page.getByRole('tab', { name: 'Profissões' }).click()
  await expect(page.getByTestId('aba-profissoes')).toBeVisible()
  await page.getByRole('tab', { name: 'Equipamento' }).click()
  await expect(page.getByText('Equipamentos disponíveis na Fase 3')).toBeVisible()
})

test('painel do cidadão: distribuir pontos pendentes', async ({ page }) => {
  await page.goto('/jogo/cidadao/1')
  const campo = page.getByTestId('inc-VIT')
  await campo.fill('1')
  await page.getByTestId('confirmar-car').click()
  await expect(page.getByTestId('erro')).toHaveCount(0)
})
