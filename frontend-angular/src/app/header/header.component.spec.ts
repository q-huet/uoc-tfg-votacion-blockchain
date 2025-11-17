import {ComponentFixture, TestBed} from '@angular/core/testing';

import {HeaderComponent} from './header.component';
import {ActivatedRoute} from '@angular/router';
import {MockActivatedRoute} from '../../testing/router-test-doubles';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let compiled;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        {provide: ActivatedRoute, useClass: MockActivatedRoute},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    compiled = fixture.debugElement.nativeElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Template Tests', () => {
    it('should have app-name h1 set', () => {
      const appLogoDiv = compiled.querySelector('#app-icon img');
      expect(appLogoDiv.alt).toEqual('Ford Oval');
    });
  });
});
