import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { DailyOverviewComponent } from './daily-overview/daily-overview.component';
import { TopRoutesTodayComponent } from './top-routes-today/top-routes-today.component';
import { DashboardService, DashboardStats, PeriodStats } from '../../shared/services/dashboard.service';

@Component({
  selector: 'app-home',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    RouterModule,
    DailyOverviewComponent,
    TopRoutesTodayComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  todayStats: PeriodStats = { activeUsers: 0, competitions: 0, runs: 0, trophies: 0 };
  monthStats: PeriodStats = { activeUsers: 0, competitions: 0, runs: 0, trophies: 0 };
  yearStats: PeriodStats = { activeUsers: 0, competitions: 0, runs: 0, trophies: 0 };

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
      icon: 'workspace_premium',
      title: 'Trophies',
      description: 'Collect trophies and achievements as you progress in your running journey.',
      action: 'View Trophies',
      route: '/trophies'
    }
  ];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  loadDashboardStats(): void {
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats: DashboardStats) => {
        this.todayStats = stats.today;
        this.monthStats = stats.thisMonth;
        this.yearStats = stats.thisYear;
      },
      error: (error) => {
        console.error('Error fetching dashboard stats:', error);
      }
    });
  }
}
