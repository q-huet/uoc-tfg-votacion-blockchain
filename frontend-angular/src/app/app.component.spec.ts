import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AppComponent} from './app.component';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {By} from '@angular/platform-browser';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {of} from 'rxjs';
import {MessageService} from 'primeng/api';
import {MockMessageService} from '../testing/primeng-test-doubles';
import {
  MockActivatedRoute,
  MockRouter,
  MockRouterLinkDirective,
  MockRouterOutletDirective,
} from '../testing/router-test-doubles';
import {MsalService, MsalBroadcastService} from '@azure/msal-angular';
import {MsalServiceStub, MsalBroadcastServiceStub} from '../testing/msal-stubs';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>, appComponent, compiled;
  const routerMock = new MockRouter();
  const mockActivatedRoute = new MockActivatedRoute();
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppComponent],
      declarations: [
        // For routing we have to use some router stubs. These aren't given by angular,
        // but the /testing/router-test-doubles.ts file provides the necessary mocks
        MockRouterLinkDirective,
        MockRouterOutletDirective,
      ],
      providers: [
        {provide: Router, useValue: routerMock},
        {provide: ActivatedRoute, useValue: mockActivatedRoute},
        {provide: MessageService, useClass: MockMessageService},
        {provide: MsalService, useValue: MsalServiceStub},
        {provide: MsalBroadcastService, useValue: MsalBroadcastServiceStub},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    appComponent = fixture.debugElement.componentInstance;
    compiled = fixture.debugElement.nativeElement;
  });

  describe('tests:', () => {
    it('should create the app', () => {
      expect(appComponent).toBeTruthy();
    });

    describe('ngOnInit(): ', () => {
      it('should leverage the router events to dynamically construct the skip url', () => {
        spyOn(appComponent, 'setSkipLinkUrl');
        routerMock.events = of(new NavigationEnd(2, 'test', 'test-after-redirect'));
        appComponent.ngOnInit();
        expect(appComponent.setSkipLinkUrl).toHaveBeenCalledWith('test-after-redirect');
      });

      it('checkAndSetActiveAccount should set an account when none is active and accounts exist', () => {
        const msal = TestBed.inject(MsalService) as any;
        msal.instance.getActiveAccount.and.returnValue(null);
        msal.instance.getAllAccounts.and.returnValue([{homeAccountId: '1'}]);
        msal.instance.setActiveAccount.calls.reset();
        appComponent.checkAndSetActiveAccount();
        expect(msal.instance.setActiveAccount).toHaveBeenCalled();
      });

      it('checkAndSetActiveAccount should not set an account when no accounts exist', () => {
        const msal = TestBed.inject(MsalService) as any;
        msal.instance.getActiveAccount.and.returnValue(null);
        msal.instance.getAllAccounts.and.returnValue([]);
        msal.instance.setActiveAccount.calls.reset();
        appComponent.checkAndSetActiveAccount();
        expect(msal.instance.setActiveAccount).not.toHaveBeenCalled();
      });
    });

    describe('setSkipLinkUrl(): ', () => {
      it('should use the passed in url to appropriately set the skip url', () => {
        appComponent.skipToMain = 'someurl';
        appComponent.setSkipLinkUrl('test#app-content');
        expect(appComponent.skipToMain).toEqual('someurl');
        appComponent.setSkipLinkUrl('test');
        expect(appComponent.skipToMain).toEqual('test#app-content');
      });
    });

    describe('template tests: ', () => {
      it('should include a header, main content section and footer', () => {
        expect(compiled.querySelector('#app-header').tagName).toEqual('HEADER');
        expect(compiled.querySelector('#app-content').tagName).toEqual('MAIN');
        expect(compiled.querySelector('#footer').tagName).toEqual('FOOTER');
      });

      it('should have the toast component included and properly configured', () => {
        expect(fixture.debugElement.query(By.css('p-toast')).nativeElement.tagName).toEqual(
          'P-TOAST',
        );
      });

      it('should include the nav component in the header', () => {
        //equiv to fixture.debugElement.query(By.css('#app-header app-nav'))
        expect(compiled.querySelector('#app-header app-nav')).toBeDefined();
      });

      it('should include a skip to main link before other items in the header', () => {
        spyOn(appComponent, 'setSkipLinkUrl').and.callThrough();
        routerMock.events = of(new NavigationEnd(2, 'test', 'testurl-after-redirect'));

        appComponent.ngOnInit();

        fixture.detectChanges();
        expect(appComponent.setSkipLinkUrl).toHaveBeenCalledWith('testurl-after-redirect');
        expect(compiled.querySelector('#app-header').firstChild.tagName).toEqual('A');
        expect(compiled.querySelector('#app-header .skip-link').textContent).toEqual(
          'Skip to Content',
        );
        expect(
          compiled
            .querySelector('#app-header .skip-link')
            .href.endsWith('/testurl-after-redirect#app-content'),
        ).toEqual(true);
      });
    });
  });
});
