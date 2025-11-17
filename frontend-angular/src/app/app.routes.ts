/**
 * Defines the application's main route configuration for Angular Router.
 *
 * This array of route objects maps URL paths to components, including default redirects and a 404 handler.
 *
 * @see {@link https://angular.dev/api/router/Routes Angular Routes}
 */

import {Routes} from '@angular/router';
import {ExamplePageComponent} from './example-page/example-page.component';
import {NotFoundComponent, LoginFailedComponent} from '@wame/ngx-frf-utilities';
import {MsalGuard} from '@azure/msal-angular';

/**
 * The main routes for the application.
 */
export const routes: Routes = [
  {
    path: 'example-page',
    component: ExamplePageComponent,
    title: 'Example Page',
    canActivate: [MsalGuard],
  },
  {
    path: '',
    redirectTo: '/example-page',
    pathMatch: 'full',
  },
  {
    path: 'login-failed',
    component: LoginFailedComponent,
  },
  {
    path: '404',
    component: NotFoundComponent,
  },
  {
    path: '**',
    redirectTo: '/404',
  },
];
