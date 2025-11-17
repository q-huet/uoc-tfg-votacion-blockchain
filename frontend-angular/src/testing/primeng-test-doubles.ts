//need to mock the primeng stuff we use in this part of the app
import {
  AfterContentInit,
  booleanAttribute,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ContentChild,
  ContentChildren,
  Directive,
  forwardRef,
  Injectable,
  Input,
  OnInit,
  QueryList,
  TemplateRef,
  ViewEncapsulation,
} from '@angular/core';
import {Confirmation, FilterOperator, Footer, PrimeTemplate, SelectItem} from 'primeng/api';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {Nullable} from 'primeng/ts-helpers';
import {Subject} from 'rxjs';

@Component({
  selector: 'p-dropdown',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MockDropDownComponent),
      multi: true,
    },
  ],
})
export class MockDropDownComponent implements ControlValueAccessor {
  _options: any[];
  @Input() get options(): any[] {
    return this._options;
  }

  set options(val: any[]) {
    this._options = val;
  }

  @Input() style: any;

  value: any;
  private _disabled: boolean;

  @Input() get disabled(): boolean {
    return this._disabled;
  }

  set disabled(_disabled: boolean) {
    this._disabled = _disabled;
    this.cd.detectChanges();
  }

  onModelChange = () => {};
  onModelTouched = () => {};

  constructor(private cd: ChangeDetectorRef) {}

  writeValue(value: any): void {
    this.value = value;
    this.cd.markForCheck();
  }

  registerOnChange(fn: () => void): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onModelTouched = fn;
  }

  setDisabledState(val: boolean): void {
    this.disabled = val;
  }
}
@Component({
  selector: 'p-multiSelect',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MockMultiSelectComponent),
      multi: true,
    },
  ],
})
export class MockMultiSelectComponent implements ControlValueAccessor {
  @Input() style: any;

  @Input() display = 'comma';

  @Input() optionLabel: string;

  @Input() optionValue: string;

  @Input() get options(): any[] {
    return this._options;
  }
  set options(val: any[]) {
    this._options = val;
  }
  _options: any[];

  value: any;

  /* eslint-disable-next-line @typescript-eslint/no-unsafe-function-type --
   * disable as this needs to match the primeng implementation
   * https://github.com/primefaces/primeng/blob/17.18.15/src/app/components/multiselect/multiselect.ts#L996
   **/
  public onModelChange: Function = () => {};

  /* eslint-disable-next-line @typescript-eslint/no-unsafe-function-type --
   * disable as this needs to match the primeng implementation
   * https://github.com/primefaces/primeng/blob/17.18.15/src/app/components/multiselect/multiselect.ts#L998
   **/
  public onModelTouched: Function = () => {};

  constructor(private cd: ChangeDetectorRef) {}

  writeValue(value: any): void {
    this.value = value;

    this.cd.markForCheck();
  }

  /* eslint-disable-next-line @typescript-eslint/no-unsafe-function-type --
   * disable as this needs to match the primeng implementation
   * https://github.com/primefaces/primeng/blob/17.18.15/src/app/components/multiselect/multiselect.ts#L2048
   **/
  registerOnChange(fn: Function): void {
    this.onModelChange = fn;
  }

  /* eslint-disable-next-line @typescript-eslint/no-unsafe-function-type --
   * disable as this needs to match the primeng implementation
   * https://github.com/primefaces/primeng/blob/17.18.15/src/app/components/multiselect/multiselect.ts#L2052
   **/
  registerOnTouched(fn: Function): void {
    this.onModelTouched = fn;
  }
}

@Component({
  selector: 'p-growl',
  template: '',
})
export class MockGrowlComponent {
  @Input() value;
}

@Component({
  selector: 'p-confirmDialog',
  template: '',
})
export class MockConfirmDialogComponent {
  @Input() style;
}

@Component({
  selector: 'p-dialog',
  template: '',
})
export class MockDialogComponent {
  @Input() visible;
  @Input() header;
  @Input() closeAriaLabel;
}

@Component({
  selector: 'p-footer',
  template: '',
})
export class MockFooterComponent {}

@Component({
  selector: 'p-header',
  template: '',
})
export class MockHeaderComponent {}

@Component({
  selector: 'p-panel',
  template: `
    <div
      [ngClass]="{'p-panel p-component': true}"
      [ngStyle]="style"
      [class]="styleClass">
      <div class="p-panel-header" *ngIf="showHeader">
        <span class="p-panel-title" *ngIf="header">{{ header }}</span>
        <ng-content select="p-header"></ng-content>
        <ng-container *ngTemplateOutlet="headerTemplate"></ng-container>
        <div class="p-panel-content">
          <ng-content></ng-content>
          <ng-container
            *ngTemplateOutlet="contentTemplate"></ng-container>
        </div>
        <div
          class="p-panel-footer"
          *ngIf="footerFacet || footerTemplate">
          <ng-content select="p-footer"></ng-content>
          <ng-container
            *ngTemplateOutlet="footerTemplate"></ng-container>
        </div>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class MockPanelComponent implements AfterContentInit {
  @Input() header: string | undefined;
  /* eslint-disable-next-line @typescript-eslint/consistent-indexed-object-style --
   * disable as this needs to match the primeng implementation
   * https://github.com/primefaces/primeng/blob/17.18.15/src/app/components/panel/panel.ts#L134
   **/
  @Input() style: {[klass: string]: any} | null | undefined;
  @Input() styleClass: string | undefined;
  /* eslint-disable-next-line @typescript-eslint/no-inferrable-types --
   * disable as this needs to match the primeng implementation
   * https://github.com/primefaces/primeng/blob/17.18.15/src/app/components/panel/panel.ts#L162
   **/
  @Input({transform: booleanAttribute}) showHeader: boolean = true;

  headerTemplate: Nullable<TemplateRef<any>>;

  contentTemplate: Nullable<TemplateRef<any>>;

  footerTemplate: Nullable<TemplateRef<any>>;

  @ContentChild(Footer) footerFacet: Nullable<TemplateRef<any>>;

  @ContentChildren(PrimeTemplate) templates: Nullable<QueryList<PrimeTemplate>>;

  ngAfterContentInit() {
    (this.templates as QueryList<PrimeTemplate>).forEach((item) => {
      switch (item.getType()) {
        case 'header':
          this.headerTemplate = item.template;
          break;

        case 'content':
          this.contentTemplate = item.template;
          break;

        case 'footer':
          this.footerTemplate = item.template;
          break;

        default:
          this.contentTemplate = item.template;
          break;
      }
    });
  }
}

//mock the table so we can test its templates. This mock is set up to do the following:
// - minimally compile the p-table component
// - create an html table with the given thead and tbody templates
// - pass the given columns through to the thead and tbody
// - pass the first item of the dataset through to the tbody for display
@Component({
  selector: 'p-table',
  template: `
    <div *ngIf="captionTemplate" class="p-datatable-header">
      <ng-container *ngTemplateOutlet="captionTemplate"></ng-container>
    </div>
    <p-paginator
      [rows]="rows"
      [first]="first"
      [totalRecords]="totalRecords"
      *ngIf="
        paginator &&
        (paginatorPosition === 'top' || paginatorPosition === 'both')
      "
      [templateLeft]="paginatorLeftTemplate"
      [templateRight]="paginatorRightTemplate"></p-paginator>
    <table>
      <thead class="p-datatable-thead">
        <ng-container
          *ngTemplateOutlet="
            headerTemplate;
            context: {$implicit: columns}
          "></ng-container>
      </thead>
      <tbody class="p-datatable-tbody">
        <ng-container
          *ngTemplateOutlet="
            bodyTemplate;
            context: {$implicit: rowData, columns: columns}
          "></ng-container>
      </tbody>
    </table>
    <p-paginator
      [rows]="rows"
      [first]="first"
      [totalRecords]="totalRecords"
      *ngIf="
        paginator &&
        (paginatorPosition === 'bottom' || paginatorPosition === 'both')
      "
      [templateLeft]="paginatorLeftTemplate"
      [templateRight]="paginatorRightTemplate"></p-paginator>
  `,
})
export class MockTableComponent implements AfterContentInit {
  @ContentChildren(PrimeTemplate) templates: QueryList<PrimeTemplate>;
  @Input() value;
  @Input() responsive;
  @Input() autoLayout;
  @Input() rows;
  @Input() pageLinks;
  @Input() paginator;
  @Input() selection;
  @Input() customSort;
  @Input() columns;
  @Input() paginatorPosition;
  @Input() rowsPerPageOptions;
  @Input() lazy;
  @Input() loading;
  @Input() loadingIcon;
  @Input() showLoader;
  @Input() resetPageOnSort;
  @Input() selectionMode;
  rowData;
  first = 0;
  page = 0;

  captionTemplate: TemplateRef<any>;
  headerTemplate: TemplateRef<any>;
  bodyTemplate: TemplateRef<any>;
  paginatorLeftTemplate: TemplateRef<any>;
  paginatorRightTemplate: TemplateRef<any>;
  totalRecords: number;

  ngAfterContentInit() {
    //explicitly pick off the first row and give it to the body template
    this.rowData = this.value[0];

    this.templates.forEach((item) => {
      switch (item.getType()) {
        case 'caption':
          this.captionTemplate = item.template;
          break;

        case 'header':
          this.headerTemplate = item.template;
          break;

        case 'body':
          this.bodyTemplate = item.template;
          break;

        case 'paginatorleft':
          this.paginatorLeftTemplate = item.template;
          break;

        case 'paginatorright':
          this.paginatorRightTemplate = item.template;
          break;
      }
    });
  }

  onPageChange(event) {
    this.first = event.first;
  }

  changePageToNext() {
    const newFirst = this.rows * (this.page + 1);
    this.onPageChange({first: newFirst});
  }
}

@Component({
  selector: 'p-paginator',
  template: `
    <div>
      <div class="p-paginator-left-content" *ngIf="templateLeft">
        <ng-container
          *ngTemplateOutlet="
            templateLeft;
            context: {$implicit: paginatorState}
          "></ng-container>
      </div>
      <div class="p-paginator-right-content" *ngIf="templateRight">
        <ng-container
          *ngTemplateOutlet="
            templateRight;
            context: {$implicit: paginatorState}
          "></ng-container>
      </div>
    </div>
  `,
})
export class MockPaginatorComponent implements OnInit {
  @Input() rows;
  @Input() first;
  @Input() totalRecords;
  @Input() pageLinks;
  @Input() templateLeft;
  @Input() templateRight;

  paginatorState: any;

  ngOnInit(): void {
    this.paginatorState = {
      page: this.getPage(),
      pageCount: this.getPageCount(),
      rows: this.rows,
      first: this.first,
      totalRecords: this.totalRecords,
    };
  }

  getPage(): number {
    return Math.floor(this.first / this.rows);
  }

  getPageCount() {
    return Math.ceil(this.totalRecords / this.rows) || 1;
  }
}

@Component({
  selector: 'p-tableHeaderCheckbox',
  template: '',
})
export class MockTableHeaderCheckboxComponent {}

@Component({
  selector: 'p-tableCheckbox',
  template: '',
})
export class MockTableCheckboxComponent {
  @Input() value;
}

@Component({
  selector: 'p-button',
  template: '',
})
export class MockButtonComponent {
  @Input() disabled;
  @Input() icon;
  @Input() label;
  @Input() type;
}

@Directive({
  selector: '[pButton]',
})
export class MockButtonAttributeDirective {
  @Input() label: string;
  @Input() icon: string;
}

@Component({
  selector: 'p-dataTable',
  template: '',
})
export class MockDataTableComponent {
  @Input() first;
  @Input() value;
  @Input() responsive;
  @Input() rows;
  @Input() pageLinks;
  @Input() paginator;

  paginate(event) {}
}

@Component({
  selector: 'p-columnFilter',
  template: '',
})
export class MockColumnFilterComponent {
  @Input() field: string;

  @Input() type = 'text';

  @Input() display = 'row';

  @Input() showMenu = true;

  @Input() matchMode: string;

  @Input() operator: string = FilterOperator.AND;

  @Input() showOperator = true;

  @Input() showClearButton = true;

  @Input() showApplyButton = true;

  @Input() showMatchModes = true;

  @Input() showAddButton = true;

  @Input() hideOnClear = false;

  @Input() placeholder: string;

  @Input() matchModeOptions: SelectItem[];

  @Input() maxConstraints = 2;

  @Input() minFractionDigits: number;

  @Input() maxFractionDigits: number;

  @Input() prefix: string;

  @Input() suffix: string;

  @Input() locale: string;

  @Input() localeMatcher: string;

  @Input() currency: string;

  @Input() currencyDisplay: string;

  @Input() useGrouping = true;
}

@Directive({
  selector: '[pSortableColumn]',
})
export class MockSortableColumnDirective {
  @Input('pSortableColumn') field: string;
  @Input() pSortableColumnDisabled: boolean;
}

@Component({
  selector: 'p-sortIcon',
  template: '',
})
export class MockSortIconComponent {
  @Input() field;
}

@Component({
  selector: 'p-column',
  template: '',
})
export class MockColumnComponent {}

@Injectable()
export class MockConfirmationService {
  confirm = jasmine.createSpy('confirm').and.callFake((obj: Confirmation) => {
    obj.accept();
  });
}

@Injectable()
export class MockMessageService {
  messageObserverStub = new Subject<any>();
  add = jasmine.createSpy('add');

  clear() {}

  messageObserver = new Subject<any>().asObservable();

  clearObserver = new Subject<any>().asObservable();
}

@Component({
  selector: 'p-menubar',
  template: '',
})
export class MockMenubarComponent {
  @Input() model;
}

@Component({
  selector: 'p-splitButton',
  template: '',
})
export class MockSplitButtonComponent {
  @Input() model;
}

@Component({
  selector: 'p-steps',
  template: '',
})
export class MockStepsComponent {
  @Input() model;
  @Input() activeIndex;
}

@Component({
  selector: 'p-autoComplete',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MockAutoCompleteComponent),
      multi: true,
    },
  ],
})
export class MockAutoCompleteComponent implements ControlValueAccessor {
  @Input() suggestions: any[];
  @Input() style: any;
  @Input() minLength: any;

  value: any;

  onModelChange = () => {};
  onModelTouched = () => {};

  constructor(private cd: ChangeDetectorRef) {}

  writeValue(value: any): void {
    this.value = value;
    this.cd.markForCheck();
  }

  registerOnChange(fn: () => void): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onModelTouched = fn;
  }
}

@Component({
  selector: 'p-calendar',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MockCalendarComponent),
      multi: true,
    },
  ],
})
export class MockCalendarComponent implements ControlValueAccessor {
  @Input() readonlyInput: any;
  @Input() style: any;
  @Input() minDate: any;
  @Input() maxDate: any;
  @Input() showIcon: any;

  value: any;

  onModelChange = () => {};
  onModelTouched = () => {};

  constructor(private cd: ChangeDetectorRef) {}

  writeValue(value: any): void {
    this.value = value;
    this.cd.markForCheck();
  }

  registerOnChange(fn: () => void): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onModelTouched = fn;
  }
}
