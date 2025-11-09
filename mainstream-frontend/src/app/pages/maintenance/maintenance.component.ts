import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SettingsService } from '../../features/admin/services/settings.service';
import { AuthService } from '../../features/users/services/auth.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-maintenance',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './maintenance.component.html',
  styleUrls: ['./maintenance.component.scss']
})
export class MaintenanceComponent implements OnInit, OnDestroy {
  private checkInterval?: Subscription;

  constructor(
    private settingsService: SettingsService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check every 30 seconds if maintenance mode is still active
    this.checkInterval = interval(30000)
      .pipe(
        switchMap(() => this.settingsService.getMaintenanceModeStatus())
      )
      .subscribe({
        next: (status) => {
          if (!status.enabled) {
            // Maintenance mode is no longer active, redirect to home
            this.router.navigate(['/home']);
          }
        },
        error: (error) => {
          console.error('Error checking maintenance mode:', error);
        }
      });
  }

  ngOnDestroy(): void {
    this.checkInterval?.unsubscribe();
  }

  isAdmin(): boolean {
    return this.authService.currentUser?.role === 'ADMIN';
  }

  goToAdmin(): void {
    if (this.isAdmin()) {
      this.router.navigate(['/admin']);
    }
  }
}
