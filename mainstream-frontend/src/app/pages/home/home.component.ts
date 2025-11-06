import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { DailyOverviewComponent } from './daily-overview/daily-overview.component';
import { RunService } from '../../features/runs/services/run.service';

@Component({
  selector: 'app-home',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    RouterModule,
    DailyOverviewComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  todayActiveUsersCount: number = 0;

  features = [
    {
      icon: 'emoji_events',
      title: 'Competitions',
      description: 'Join exciting running competitions and challenge yourself against other runners.',
      action: 'View Competitions',
      route: '/competitions'
    },
    {
      icon: 'directions_run',
      title: 'Track Your Runs',
      description: 'Log your running activities and monitor your progress over time.',
      action: 'Log a Run',
      route: '/runs'
    },
    {
      icon: 'leaderboard',
      title: 'Leaderboards',
      description: 'See how you rank against other runners in various categories.',
      action: 'View Rankings',
      route: '/leaderboard'
    }
  ];

  constructor(private runService: RunService) {}

  ngOnInit(): void {
    this.loadTodayActiveUsersCount();
  }

  loadTodayActiveUsersCount(): void {
    this.runService.getTodayActiveUsersCount().subscribe({
      next: (count) => {
        this.todayActiveUsersCount = count;
      },
      error: (error) => {
        console.error('Error fetching today active users count:', error);
        this.todayActiveUsersCount = 0;
      }
    });
  }
}
