import {Component, OnInit, OnDestroy} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {filter, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {NavComponent} from './nav/nav.component';
import {ToastModule} from 'primeng/toast';
import {FooterComponent} from './footer/footer.component';
import {MsalService, MsalBroadcastService} from '@azure/msal-angular';
import {InteractionStatus} from '@azure/msal-browser';

/** Class for the app component that is bootstrapped to run the application
 */
@Component({
  selector: 'body',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [NavComponent, ToastModule, RouterOutlet, FooterComponent],
})
export class AppComponent implements OnInit, OnDestroy {
  /** string used to hold the url for the skipToMain link */
  skipToMain: string;
  /** Subject for managing subscriptions */
  private readonly _destroying$ = new Subject<void>();

  /** constructor for setting up DI in this component */
  constructor(
    private readonly router: Router,
    private authService: MsalService,
    private msalBroadcastService: MsalBroadcastService,
  ) {}

  /** this class requires this method to implement the OnInit interface */
  ngOnInit(): void {
    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        takeUntil(this._destroying$),
      )
      .subscribe(() => {
        this.checkAndSetActiveAccount();
      });
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.setSkipLinkUrl(event.urlAfterRedirects);
      });
  }

  /** Required cleanup method for OnDestroy interface */
  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }

  /**
   * Checks if there is an active account set. If not, sets the first signed-in account as the active account.
   * Note: Basic usage demonstrated. Your app may require more complicated account selection logic.
   */
  checkAndSetActiveAccount(): void {
    const activeAccount = this.authService.instance.getActiveAccount();
    if (!activeAccount && this.authService.instance.getAllAccounts().length > 0) {
      const accounts = this.authService.instance.getAllAccounts();
      this.authService.instance.setActiveAccount(accounts[0]);
    }
  }
  /**
   * setSkipLinkUrl takes in a url string and processes whether
   * the skipToMain link should be updated to use the new value
   * @param currentUrl the new url to refer to
   */
  private setSkipLinkUrl(currentUrl: string): void {
    if (!currentUrl.endsWith('#app-content')) {
      this.skipToMain = `${currentUrl}#app-content`;
    }
  }
}
