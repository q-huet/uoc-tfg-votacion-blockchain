import {expect, test} from '@playwright/test';

test.describe('Health Check', () => {
  test.describe.configure({retries: 5});

  test.beforeEach(async ({page}) => {
    await page.goto('/health');
  });

  test('Should have status:UP', async ({page}) => {
    await expect(page.locator('body')).toContainText('{"status":"UP"}');
  });
});
