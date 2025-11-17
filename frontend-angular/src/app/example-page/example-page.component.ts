import {Component} from '@angular/core';

/** Component that has no functionality other than plain html text in the template */
@Component({
  selector: 'app-example-page',
  templateUrl: './example-page.component.html',
  styleUrls: ['./example-page.component.scss'],
  standalone: true,
})
export class ExamplePageComponent {}
