/*
  Originally provided by Angular as "router-stubs.ts".
  This file has been renamed to "router-test-doubles.ts" and contains test doubles for Angular Router classes and directives,
  adapted for use in unit testing scenarios.
  Copyright 2017 Google Inc. All Rights Reserved.
  Use of this source code is governed by an MIT-style license that
  can be found in the LICENSE file at https://angular.dev/license
 */
import {
  Component,
  Directive,
  ElementRef,
  HostListener,
  Injectable,
  Input,
  OnInit,
} from '@angular/core';
import {Event, NavigationEnd, NavigationExtras, UrlTree} from '@angular/router';
// Only implements params and part of snapshot.params
import {BehaviorSubject, Observable, of} from 'rxjs';

export {ActivatedRoute, Router, RouterLink, RouterOutlet} from '@angular/router';

@Directive({
  selector: '[routerLink]',
})
export class MockRouterLinkDirective implements OnInit {
  //disable as this needs to match the surface api
  @Input('routerLink') linkParams: any;
  @Input() state?: any;

  //navigatedTo: any = null;
  constructor(private el: ElementRef) {}

  ngOnInit() {
    this.el.nativeElement.setAttribute('href', this.linkParams);
  }
}

@Component({
  selector: 'router-outlet',
  template: '',
})
//disable as this needs to match the surface api
// eslint-disable-next-line
export class MockRouterOutletDirective {
  @HostListener('activate') onClick() {}
}

@Injectable()
export class MockRouter {
  events: Observable<Event>;
  url: string;

  constructor() {
    this.events = of(new NavigationEnd(1, '/testurl-before-redirect', '/testurl-after-redirect'));
  }

  navigate(commands: any[], extras?: NavigationExtras) {}

  navigateByUrl(url: string | UrlTree, extras?: NavigationExtras) {}

  getCurrentNavigation() {
    return null;
  }
  createUrlTree(commands: any[], navigationExtras?: NavigationExtras): UrlTree {
    return {} as UrlTree; // Return a dummy UrlTree object
  }

  serializeUrl(url: UrlTree): string {
    return ''; // Return a dummy URL string
  }
}

@Injectable()
export class MockActivatedRoute {
  // ActivatedRoute.params is Observable
  private paramsSubject = new BehaviorSubject(this.testParams);
  params = this.paramsSubject.asObservable();

  private queryParamsSubject = new BehaviorSubject(this.testQueryParams);
  queryParams = this.queryParamsSubject.asObservable();

  // ActivatedRoute.data is Observable
  private dataSubject = new BehaviorSubject(this.testData);
  data = this.dataSubject.asObservable();

  // url mapping
  private _url: [{path: string}];
  get url() {
    return this._url;
  }

  set url(url: [{path: string}]) {
    this._url = url;
  }

  // fragment mapping
  private _fragment: string;
  get fragment() {
    return this._fragment;
  }

  set fragment(fragment: string) {
    this._fragment = fragment;
  }

  // Test parameters
  private _testParams: object;
  get testParams() {
    return this._testParams;
  }

  set testParams(params: object) {
    this._testParams = params;
    this.paramsSubject.next(params);
  }

  // Test query parameters
  private _testQueryParams: object;
  get testQueryParams() {
    return this._testQueryParams;
  }

  set testQueryParams(queryParams: object) {
    this._testQueryParams = queryParams;
    this.queryParamsSubject.next(queryParams);
  }

  // Test data
  private _testData: object;
  get testData() {
    return this._testData;
  }

  set testData(data: object) {
    this._testData = data;
    this.dataSubject.next(data);
  }

  get snapshot() {
    return {
      url: this.url,
      fragment: this.fragment,
      params: this.testParams,
      queryParams: this.testQueryParams,
      data: this.testData,
    };
  }
}

export interface UrlSegmentStub {
  path: string;
}

@Injectable()
export class MockActivatedRouteSnapshot {
  // url mapping
  private _url: UrlSegmentStub[] = [];
  get url() {
    return this._url;
  }

  set url(url: UrlSegmentStub[]) {
    this._url = url;
  }

  // Test parameters
  private _params: object = {};
  get params() {
    return this._params;
  }

  set params(params: object) {
    this._params = params;
  }

  // Test data
  private _data: object = {};
  get data() {
    return this._data;
  }

  set data(data: object) {
    this._data = data;
  }
}
