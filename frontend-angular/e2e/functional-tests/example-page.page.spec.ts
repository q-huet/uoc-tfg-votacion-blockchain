import {expect, test} from '@playwright/test';
import {ExamplePagePage} from './example-page.page';

let examplePage: ExamplePagePage;

test.beforeEach(async ({page}) => {
  examplePage = new ExamplePagePage(page);
  await examplePage.goto();
});

test('Example Page Logo Exists', async () => {
  await expect(examplePage.applicationLogo).toHaveAttribute('alt', 'Ford Oval');
});

test('Example Page Text Exists', async () => {
  await expect(examplePage.examplePageText).toContainText('example-page works!');
});
