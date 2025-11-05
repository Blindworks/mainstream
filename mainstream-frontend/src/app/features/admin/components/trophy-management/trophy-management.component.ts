import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, DailyWinner } from '../../services/admin.service';

@Component({
  selector: 'app-trophy-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trophy-management.component.html',
  styleUrls: ['./trophy-management.component.css']
})
export class TrophyManagementComponent implements OnInit {
  loading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  recentWinners: DailyWinner[] = [];
  loadingWinners = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadRecentWinners();
  }

  initializeTrophies(): void {
    this.loading = true;
    this.clearMessages();

    this.adminService.initializeTrophies().subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = `Successfully initialized ${response.count} trophies`;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Failed to initialize trophies';
      }
    });
  }

  calculateDailyWinners(): void {
    this.loading = true;
    this.clearMessages();

    this.adminService.calculateDailyWinners().subscribe({
      next: (winners) => {
        this.loading = false;
        this.successMessage = `Successfully calculated ${winners.length} daily winners`;
        this.loadRecentWinners();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Failed to calculate daily winners';
      }
    });
  }

  loadRecentWinners(): void {
    this.loadingWinners = true;

    this.adminService.getRecentWinners(7).subscribe({
      next: (winners) => {
        this.recentWinners = winners;
        this.loadingWinners = false;
      },
      error: (error) => {
        console.error('Failed to load recent winners:', error);
        this.loadingWinners = false;
      }
    });
  }

  getCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      'EARLIEST_RUN': 'Frühaufsteher',
      'LATEST_RUN': 'Nachteule',
      'LONGEST_STREAK': 'Longest Streak',
      'MOST_RUNS': 'Consistency King',
      'MOST_ROUTES': 'Explorer',
      'LONGEST_DISTANCE': 'Distance Hero',
      'FASTEST_TIME': 'Speed Demon'
    };
    return labels[category] || category;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  private clearMessages(): void {
    this.successMessage = null;
    this.errorMessage = null;
  }
}
