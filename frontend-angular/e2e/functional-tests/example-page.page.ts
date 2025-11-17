import {Locator, Page} from '@playwright/test';

export class ExamplePagePage {
  readonly page: Page;
  readonly applicationLogo: Locator;
  readonly examplePageText: Locator;

  constructor(page: Page) {
    this.page = page;
    this.applicationLogo = page.locator('img[src="assets/image/Ford-Oval.svg"]');
    this.examplePageText = page.locator('app-example-page p');
  }

  /* Visiting Homepage to implicitly test fallback routing to example-page */
  async goto() {
    await this.page.goto('');
  }
}
