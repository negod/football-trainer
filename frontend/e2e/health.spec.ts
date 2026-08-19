import { test, expect } from '@playwright/test';

const backendUrl = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080';

test('backend health endpoint reports up', async ({ request }) => {
  const response = await request.get(`${backendUrl}/api/health`);
  expect(response.ok()).toBeTruthy();
  expect(await response.json()).toMatchObject({ status: 'UP' });
});

test('app shell renders', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
});
