import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';

/** Angular component that displays header for the application
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: true,
  imports: [RouterLink],
})
export class HeaderComponent {}
