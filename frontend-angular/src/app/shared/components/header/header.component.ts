import { Component } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { VoteService } from '../../../core/services/vote.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  constructor(
    private location: Location,
    private router: Router,
    public authService: AuthService,
    private voteService: VoteService
  ) {}

  goBack(): void {
    this.location.back();
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.voteService.clearAllReceipts(); // Clear receipts from memory
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        console.error('Logout error', err);
        this.voteService.clearAllReceipts(); // Clear receipts from memory
        // Force navigation even if API fails
        this.router.navigate(['/auth/login']);
      }
    });
  }
}
