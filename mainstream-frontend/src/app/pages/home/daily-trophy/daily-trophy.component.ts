import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Trophy, UserTrophy } from '../../../features/trophies/models/trophy.model';
import { TrophyService } from '../../../features/trophies/services/trophy.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-daily-trophy',
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule
  ],
  templateUrl: './daily-trophy.component.html',
  styleUrls: ['./daily-trophy.component.scss']
})
export class DailyTrophyComponent implements OnInit {
  // View toggle
  showWeeklyView = false;

  // Daily trophy data
  todaysTrophy: Trophy | null = null;
  winners: UserTrophy[] = [];

  // Weekly trophy data
  weeklyTrophyStats: { trophy: Trophy; count: number; winners: UserTrophy[] }[] = [];
  selectedWeeklyTrophy: { trophy: Trophy; count: number; winners: UserTrophy[] } | null = null;

  // Loading and error states
  isLoading = true;
  isLoadingWeekly = false;
  error: string | null = null;

  constructor(
    private trophyService: TrophyService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDailyTrophy();
  }

  onViewToggle(): void {
    if (this.showWeeklyView && this.weeklyTrophyStats.length === 0) {
      this.loadWeeklyTrophies();
    }
  }

  private loadDailyTrophy(): void {
    this.isLoading = true;
    this.error = null;

    // Load both trophy and winners in parallel
    forkJoin({
      trophy: this.trophyService.getTodaysTrophy().pipe(
        catchError(error => {
          console.error('Error loading today\'s trophy:', error);
          return of(null);
        })
      ),
      winners: this.trophyService.getTodaysTrophyWinners().pipe(
        catchError(error => {
          console.error('Error loading trophy winners:', error);
          return of([]);
        })
      )
    }).subscribe({
      next: (result) => {
        this.todaysTrophy = result.trophy;
        this.winners = result.winners;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading daily trophy data:', error);
        this.error = 'Fehler beim Laden der Tagstrophäe';
        this.isLoading = false;
      }
    });
  }

  private loadWeeklyTrophies(): void {
    this.isLoadingWeekly = true;
    this.error = null;

    this.trophyService.getWeeklyTrophyStats().pipe(
      catchError(error => {
        console.error('Error loading weekly trophies:', error);
        this.error = 'Fehler beim Laden der Wochentrophäen';
        return of([]);
      })
    ).subscribe({
      next: (stats) => {
        this.weeklyTrophyStats = stats;
        if (stats.length > 0) {
          this.selectedWeeklyTrophy = stats[0];
        }
        this.isLoadingWeekly = false;
      },
      error: () => {
        this.isLoadingWeekly = false;
      }
    });
  }

  selectWeeklyTrophy(stat: { trophy: Trophy; count: number; winners: UserTrophy[] }): void {
    this.selectedWeeklyTrophy = stat;
  }

  getTrophyImageUrl(trophy: Trophy): string {
    if (trophy.imageUrl) {
      return trophy.imageUrl;
    }
    // Fallback to icon if no image
    return trophy.iconUrl || 'assets/images/trophy-default.png';
  }

  getTrophyCategoryName(category: string): string {
    return this.trophyService.getTrophyCategoryName(category);
  }

  getCategoryColor(category: string): string {
    return this.trophyService.getCategoryColor(category);
  }

  formatDateTime(date: Date): string {
    return this.trophyService.formatDateTime(date);
  }

  navigateToTrophies(): void {
    this.router.navigate(['/trophies']);
  }

  getWinnerDisplayName(winner: UserTrophy): string {
    // You might want to enhance this to show actual user names if available
    return winner.userName || `User #${winner.userId}`;
  }

  getWinnerInitials(winner: UserTrophy): string {
    const name = this.getWinnerDisplayName(winner);
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  trackByWinnerId(index: number, winner: UserTrophy): number {
    return winner.id;
  }
}
