import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {ExamplePageComponent} from './example-page.component';

describe('ExamplePageComponent', () => {
  let component: ExamplePageComponent;
  let fixture: ComponentFixture<ExamplePageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ExamplePageComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExamplePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should say that the app works', () => {
    //example using childNodes selection
    expect(fixture.debugElement.childNodes[0]['childNodes'][0].nativeNode.wholeText).toContain(
      'example-page works!',
    );
  });
});
