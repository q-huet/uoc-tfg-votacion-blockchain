/**This file contains a constant object that has properties that are necessary
 * for the build found in the `prod` configuration in `angular.json`.
 */

export const environment = {
  production: true,
  baseUrl: '',
  msalConfig: {
    clientId: '[PROD Client ID]',
    authority: 'https://login.microsoftonline.com/c990bb7a-51f4-439b-bd36-9c07fb1041c0/',
    redirectUri: '[SPA Redirect]',
  },
  apiConfig: {
    apiName: {
      scopes: ['api://[Provider Client ID]/[Scope Name]'],
      uri: '[Deployed API URL]',
    },
  },
};
