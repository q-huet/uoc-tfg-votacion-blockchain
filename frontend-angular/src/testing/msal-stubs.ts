import {Observable, of} from 'rxjs';
import {InteractionStatus} from '@azure/msal-browser';

/**
 * MSAL Service stub for testing
 */
export const MsalServiceStub = {
  instance: {
    getActiveAccount: jasmine.createSpy('getActiveAccount').and.returnValue(null),
    getAllAccounts: jasmine.createSpy('getAllAccounts').and.returnValue([]),
    setActiveAccount: jasmine.createSpy('setActiveAccount'),
  },
};

/**
 * MSAL Broadcast Service stub for testing
 */
export const MsalBroadcastServiceStub = {
  inProgress$: of(InteractionStatus.None),
  msalSubject$: of(null),
  msalInstance$: of(null),
};

/**
 * MSAL Guard stub for testing
 */
export const MsalGuardStub = {
  canActivate: jasmine.createSpy('canActivate').and.returnValue(of(true)),
};
