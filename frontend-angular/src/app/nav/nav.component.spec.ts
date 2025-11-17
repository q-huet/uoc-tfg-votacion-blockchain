import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NavComponent} from './nav.component';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {MockActivatedRoute} from '../../testing/router-test-doubles';

describe('NavComponent', () => {
  let fixture: ComponentFixture<NavComponent>;
  /* eslint-disable @typescript-eslint/no-unused-vars --
   * ESLint seems to think this is unused, but it is set on L64 and used on L78 */
  let componentInstance;
  /* eslint-enable @typescript-eslint/no-unused-vars */

  const expectedNavMenuItems = [
    {
      label: 'Example Page',
      routerLink: '/example-page',
    },
    {
      label: 'Example Dropdown',
      items: [
        {
          label: 'Nested1',
          items: [
            {
              label: 'Option1',
              icon: 'fas fa-check',
            },
          ],
        },
        {
          label: 'Nested2',
          items: [
            {
              label: 'Option2',
              icon: 'fas fa-plus',
            },
          ],
        },
      ],
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NavComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {provide: ActivatedRoute, useClass: MockActivatedRoute},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NavComponent);
    componentInstance = fixture.componentInstance;
  });
  describe('tests:', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(fixture).not.toBeNull();
    });

    it('should render menu items', () => {
      const expected = JSON.stringify(expectedNavMenuItems);
      const actual = JSON.stringify(fixture.componentInstance.navMenuItems);
      expect(actual).toEqual(expected);
    });
  });
});
