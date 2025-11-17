import {routes} from './app.routes';

describe('routes', () => {
  it('should be defined', () => {
    expect(routes).toBeDefined();
  });

  it('should be an array', () => {
    expect(Array.isArray(routes)).toBe(true);
  });

  it('should have at least one route', () => {
    expect(routes.length).toBeGreaterThan(0);
  });

  it('should have a default redirect', () => {
    const defaultRoute = routes.find((r) => r.path === '');
    expect(defaultRoute).toBeDefined();
    expect(defaultRoute?.redirectTo).toBeDefined();
  });
});
