import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterModule } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { AuthService } from '../../../features/users/services/auth.service';
import { RunService } from '../../../features/runs/services/run.service';
import { TrophyService } from '../../../features/trophies/services/trophy.service';
import { RunSummary } from '../../../features/runs/models/run.model';
import { TrophyWithProgress } from '../../../features/trophies/models/trophy.model';

@Component({
  selector: 'app-daily-overview',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    RouterModule,
    TranslocoModule
  ],
  templateUrl: './daily-overview.component.html',
  styleUrl: './daily-overview.component.scss'
})
export class DailyOverviewComponent implements OnInit {
  isAuthenticated = false;
  todayRuns: RunSummary[] = [];
  todayDistance = 0;
  todayDuration = 0;
  dailyGoalKm = 5; // Default daily goal
  availableTrophies: TrophyWithProgress[] = [];
  loading = true;

  constructor(
    private authService: AuthService,
    private runService: RunService,
    private trophyService: TrophyService,
    private translocoService: TranslocoService
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated;

    if (this.isAuthenticated) {
      this.loadTodayData();
    } else {
      this.loading = false;
    }
  }

  private loadTodayData(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) {
      this.loading = false;
      return;
    }

    // Load user's runs
    this.runService.getUserRuns(userId, 0, 100).subscribe({
      next: (response) => {
        // Filter runs from today
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        this.todayRuns = response.content.filter(run => {
          const runDate = new Date(run.startTime);
          runDate.setHours(0, 0, 0, 0);
          return runDate.getTime() === today.getTime();
        });

        // Calculate today's totals
        this.todayDistance = this.todayRuns.reduce((sum, run) =>
          sum + (run.distanceKm || 0), 0);
        this.todayDuration = this.todayRuns.reduce((sum, run) =>
          sum + (run.durationSeconds || 0), 0);

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading runs:', error);
        this.loading = false;
      }
    });

    // Load available trophies (not yet earned)
    this.trophyService.getTrophiesWithProgress().subscribe({
      next: (trophies) => {
        // Show only unearned trophies, limit to 3
        this.availableTrophies = trophies
          .filter(t => !t.isEarned && t.isActive)
          .slice(0, 3);
      },
      error: (error) => {
        console.error('Error loading trophies:', error);
      }
    });
  }

  get progressPercentage(): number {
    return Math.min((this.todayDistance / this.dailyGoalKm) * 100, 100);
  }

  get remainingDistance(): number {
    return Math.max(this.dailyGoalKm - this.todayDistance, 0);
  }

  formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  }

  getTrophyIcon(trophy: TrophyWithProgress): string {
    if (trophy.iconUrl) {
      return trophy.iconUrl;
    }

    // Default icons based on trophy type
    const iconMap: { [key: string]: string } = {
      'DISTANCE_MILESTONE': 'stars',
      'STREAK': 'local_fire_department',
      'ROUTE_COMPLETION': 'flag',
      'CONSISTENCY': 'calendar_today',
      'TIME_BASED': 'timer',
      'EXPLORER': 'explore',
      'SPECIAL': 'emoji_events'
    };

    return iconMap[trophy.type] || 'emoji_events';
  }

  getRunsText(): string {
    if (this.todayRuns.length === 0) {
      return this.translocoService.translate('dailyOverview.authenticated.noRunToday');
    } else if (this.todayRuns.length === 1) {
      return this.translocoService.translate('dailyOverview.authenticated.oneRunCompleted');
    } else {
      return this.translocoService.translate('dailyOverview.authenticated.multipleRunsCompleted', {
        count: this.todayRuns.length
      });
    }
  }
}
