/**
 * Environment configuration for development
 */
export const environment = {
  production: false,

  // Backend API configuration
  apiUrl: 'http://localhost:8080/api/v1',

  // API endpoints
  endpoints: {
    auth: {
      login: '/auth/login',
      logout: '/auth/logout',
      validate: '/auth/validate',
      user: '/auth/user'
    },
    elections: {
      base: '/elections',
      active: '/elections',
      detail: '/elections/:id',
      vote: '/elections/:id/vote',
      close: '/elections/:id/close',
      results: '/elections/:id/results'
    }
  },

  // JWT configuration
  jwt: {
    tokenKey: 'votacion_jwt_token',
    tokenPrefix: 'Bearer',
    headerName: 'Authorization'
  },

  // Application settings
  app: {
    name: 'Sistema de Votación Blockchain',
    version: '1.0.0',
    defaultLanguage: 'es'
  },

  // Feature flags
  features: {
    enableBlockchainVerification: true,
    enableVoteModification: false,
    enableAuditTrail: true
  },

  // Timeouts and limits (in milliseconds)
  timeouts: {
    httpRequest: 30000,
    tokenRefresh: 3600000 // 1 hour
  },

  // Logging
  logging: {
    level: 'debug',
    enableConsole: true
  }
};
