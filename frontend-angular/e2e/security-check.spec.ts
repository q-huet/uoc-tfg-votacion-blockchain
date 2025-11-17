import {expect, Page, test} from '@playwright/test';

test.describe('Check that only get requests to baseURL should return 200', () => {
  test('Should not allow delete', async ({page}) => {
    const response = await page.request.delete('/');
    expect(response.status()).toBe(403);
  });
  test('Should not allow put', async ({page}) => {
    const response = await page.request.put('/', {data: {foo: 'bar'}});
    expect(response.status()).toBe(403);
  });
  test('Should not allow post', async ({page}) => {
    const response = await page.request.post('/', {data: {foo: 'bar'}});
    expect(response.status()).toBe(403);
  });
  test('Should allow get', async ({page}) => {
    const response = await page.request.get('/');
    expect(response.status()).toBe(200);
  });
});

test.describe('Check that only get requests to /health should return 200', () => {
  test('Should not allow delete', async ({page}) => {
    const response = await page.request.delete('/health');
    expect(response.status()).toBe(403);
  });
  test('Should not allow put', async ({page}) => {
    const response = await page.request.put('/health', {
      data: {foo: 'bar'},
    });
    expect(response.status()).toBe(403);
  });
  test('Should not allow post', async ({page}) => {
    const response = await page.request.post('/health', {
      data: {foo: 'bar'},
    });
    expect(response.status()).toBe(403);
  });
  test('Should allow get', async ({page}) => {
    const response = await page.request.get('/health');
    expect(response.status()).toBe(200);
  });
});

test.describe('Check that the correct security headers are set', () => {
  test('Should have Cross-Origin-Opener-Policy set to same-origin on base url', async ({page}) => {
    const response = await page.goto('/');
    const hasCOOPHeader = (await response.headersArray()).some((header) => {
      return header.name === 'cross-origin-opener-policy' && header.value === 'same-origin';
    });
    expect(hasCOOPHeader).toBe(true);
  });
  test('Should have Strict-Transport-Security set to max-age=31536000; includeSubDomains', async ({
    page,
  }) => {
    const response = await page.goto('/');
    const hasHSTSHeader = (await response.headersArray()).some((header) => {
      return (
        header.name === 'strict-transport-security' &&
        header.value === 'max-age=31536000; includeSubDomains'
      );
    });
    expect(hasHSTSHeader).toBe(true);
  });
});

test.describe('Check that rewrites for Angular work properly', () => {
  async function testAngularRouteAndExpectResponseBehavior(page: Page, routeToTest: string) {
    const indexResponsePromise = page.waitForResponse('index.html');
    await page.goto('index.html');
    const indexResponse = await indexResponsePromise;
    const indexResponseText = await indexResponse.text();

    const testResponsePromise = page.waitForResponse(routeToTest);
    await page.goto(routeToTest);
    const testResponse = await testResponsePromise;
    const testResponseText = await testResponse.text();

    expect(testResponse.status()).toBe(200);
    expect(testResponseText).toBe(indexResponseText);
  }

  test('should return index.html for base route', async ({page}) => {
    await testAngularRouteAndExpectResponseBehavior(page, '/');
  });

  test('should return index.html for route not found in the Angular router and route to 404', async ({
    page,
    baseURL,
  }) => {
    const routeToTest = '/nonsensical-route-that-nobody-will-have';

    await testAngularRouteAndExpectResponseBehavior(page, routeToTest);

    expect(page.url()).not.toContain(routeToTest);
    expect(page.url()).toBe(baseURL + `/404`);
  });

  test('should return index.html for route found in the Angular router and keep the route in the url', async ({
    page,
    baseURL,
  }) => {
    const routeToTest = '/example-page';

    await testAngularRouteAndExpectResponseBehavior(page, routeToTest);

    expect(page.url()).toBe(baseURL + routeToTest);
  });
});
