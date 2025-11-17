import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {MessageService} from 'primeng/api';
import {Provider, EnvironmentProviders, FactoryProvider, ClassProvider} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {
  MsalService,
  MsalGuard,
  MsalBroadcastService,
  MsalInterceptor,
  MSAL_INSTANCE,
  MSAL_GUARD_CONFIG,
  MSAL_INTERCEPTOR_CONFIG,
} from '@azure/msal-angular';
import {BrowserCacheLocation, LogLevel, InteractionType} from '@azure/msal-browser';
import {
  appConfig,
  MSALInstanceFactory,
  MSALInterceptorConfigFactory,
  MSALGuardConfigFactory,
  loggerCallback,
} from './app.config';
import {MsalServiceStub, MsalBroadcastServiceStub, MsalGuardStub} from '../testing/msal-stubs';
import {routes} from './app.routes';

/**
 * Helper to check if a provider is an EnvironmentProviders (from importProvidersFrom, provideRouter, etc.)
 */
function isEnvironmentProviders(
  provider: Provider | EnvironmentProviders,
): provider is EnvironmentProviders {
  return (
    provider &&
    typeof provider === 'object' &&
    ('ɵfromNgModule' in provider || 'ɵproviders' in provider)
  );
}

/**
 * Helper to find a provider by its 'provide' token
 */
function findProviderByToken(
  providers: (Provider | EnvironmentProviders)[],
  token: unknown,
): Provider | undefined {
  return providers.find(
    (provider) =>
      provider &&
      typeof provider === 'object' &&
      'provide' in provider &&
      provider.provide === token,
  ) as Provider | undefined;
}

describe('appConfig', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: appConfig.providers,
    });
  });

  it('should be defined with non-empty providers array', () => {
    expect(appConfig).toBeDefined();
    expect(Array.isArray(appConfig.providers)).toBe(true);
    expect(appConfig.providers.length).toBeGreaterThan(0);
  });

  it('should provide MessageService', () => {
    const messageService = TestBed.inject(MessageService);
    expect(messageService).toBeDefined();
  });

  it('should provide Router with routes', () => {
    const router = TestBed.inject(Router);
    expect(router).toBeDefined();
    expect(router.config).toEqual(routes);
  });

  it('should include EnvironmentProviders from importProvidersFrom and provideRouter', () => {
    const envProviders = appConfig.providers.filter(isEnvironmentProviders);
    expect(envProviders.length).toBeGreaterThan(0);
  });
  describe('MSAL Configuration (when entraId is enabled)', () => {
    it('should include MSAL HTTP interceptor', () => {
      const interceptorProvider = findProviderByToken(appConfig.providers, HTTP_INTERCEPTORS) as
        | ClassProvider
        | undefined;
      expect(interceptorProvider).toBeDefined();
      expect(interceptorProvider?.useClass).toBe(MsalInterceptor);
      expect(interceptorProvider?.multi).toBe(true);
    });

    it('should provide MSAL instance factory', () => {
      const instanceProvider = findProviderByToken(appConfig.providers, MSAL_INSTANCE) as
        | FactoryProvider
        | undefined;
      expect(instanceProvider).toBeDefined();
      expect(instanceProvider?.useFactory).toBe(MSALInstanceFactory);
    });

    it('should provide MSAL guard configuration factory', () => {
      const guardConfigProvider = findProviderByToken(appConfig.providers, MSAL_GUARD_CONFIG) as
        | FactoryProvider
        | undefined;
      expect(guardConfigProvider).toBeDefined();
      expect(guardConfigProvider?.useFactory).toBe(MSALGuardConfigFactory);
    });

    it('should provide MSAL interceptor configuration factory', () => {
      const interceptorConfigProvider = findProviderByToken(
        appConfig.providers,
        MSAL_INTERCEPTOR_CONFIG,
      ) as FactoryProvider | undefined;
      expect(interceptorConfigProvider).toBeDefined();
      expect(interceptorConfigProvider?.useFactory).toBe(MSALInterceptorConfigFactory);
    });

    it('should provide MSAL services', () => {
      expect(appConfig.providers).toContain(MsalService);
      expect(appConfig.providers).toContain(MsalGuard);
      expect(appConfig.providers).toContain(MsalBroadcastService);
    });
  });

  describe('MSAL Factory Functions', () => {
    beforeAll(() => {
      spyOn(console, 'log');
    });

    describe('MSALInstanceFactory', () => {
      it('should create a PublicClientApplication with correct configuration', () => {
        const instance = MSALInstanceFactory();
        const config = instance.getConfiguration();

        expect(instance).toBeDefined();
        expect(instance.getConfiguration).toBeDefined();
        expect(config.cache?.cacheLocation).toBe(BrowserCacheLocation.SessionStorage);
        expect(config.system?.allowPlatformBroker).toBe(false);
        expect(config.system?.loggerOptions?.logLevel).toBe(LogLevel.Info);
        expect(config.system?.loggerOptions?.piiLoggingEnabled).toBe(false);
        expect(config.system?.loggerOptions?.loggerCallback).toBe(loggerCallback);
      });
    });

    describe('MSALInterceptorConfigFactory', () => {
      it('should create interceptor configuration with protected resource map', () => {
        const config = MSALInterceptorConfigFactory();

        expect(config).toBeDefined();
        expect(config.interactionType).toBe(InteractionType.Redirect);
        expect(config.protectedResourceMap).toBeDefined();
        expect(config.protectedResourceMap instanceof Map).toBe(true);
        expect(config.protectedResourceMap.size).toBeGreaterThan(0);
      });
    });

    describe('MSALGuardConfigFactory', () => {
      it('should create guard configuration with auth request', () => {
        const config = MSALGuardConfigFactory();

        expect(config).toBeDefined();
        expect(config.interactionType).toBe(InteractionType.Redirect);
        expect(config.authRequest).toBeDefined();
        expect(Array.isArray((config.authRequest as {scopes: string[]}).scopes)).toBe(true);
        expect(config.loginFailedRoute).toBe('/login-failed');
      });
    });

    describe('loggerCallback', () => {
      it('should log messages to console', () => {
        const testMessage = 'Test log message';
        loggerCallback(LogLevel.Info, testMessage);
        expect(console.log).toHaveBeenCalledWith(testMessage);
      });
    });
  });

  describe('MSAL Integration Tests', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          ...appConfig.providers,
          {provide: MsalService, useValue: MsalServiceStub},
          {provide: MsalBroadcastService, useValue: MsalBroadcastServiceStub},
          {provide: MsalGuard, useValue: MsalGuardStub},
        ],
      });
    });

    it('should inject MSAL services correctly', () => {
      const msalService = TestBed.inject(MsalService);
      const msalBroadcastService = TestBed.inject(MsalBroadcastService);
      const msalGuard = TestBed.inject(MsalGuard);

      expect(msalService).toBeDefined();
      expect(msalBroadcastService).toBeDefined();
      expect(msalGuard).toBeDefined();
    });
  });
});
