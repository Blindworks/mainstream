import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {
  TodayStats,
  WeekStats,
  RecentAchievement,
  PersonalStatsData,
  DayStatus
} from './personal-stats.model';

/**
 * PersonalStatsComponent
 *
 * Displays personal running statistics in three cards:
 * 1. Today's Stats - Distance and activity status
 * 2. This Week - Visual week overview with daily completion status
 * 3. Recent Achievements - New trophies/badges earned
 *
 * Uses Angular Signals for reactive state management
 */
@Component({
  selector: 'app-personal-stats',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './personal-stats.component.html',
  styleUrl: './personal-stats.component.scss'
})
export class PersonalStatsComponent implements OnInit {
  // Signal-based state management
  protected readonly statsData = signal<PersonalStatsData>({
    today: {
      distanceKm: 0,
      durationSeconds: 0,
      runsCount: 0,
      goalAchieved: false,
      dailyGoalKm: 5
    },
    week: {
      days: [],
      totalDistanceKm: 0,
      activeDays: 0,
      currentStreak: 0
    },
    recentAchievements: [],
    loading: true
  });

  // Computed values derived from statsData
  protected readonly todayStats = computed(() => this.statsData().today);
  protected readonly weekStats = computed(() => this.statsData().week);
  protected readonly recentAchievements = computed(() => this.statsData().recentAchievements);
  protected readonly loading = computed(() => this.statsData().loading);

  constructor() {}

  ngOnInit(): void {
    this.loadPersonalStats();
  }

  /**
   * Loads personal statistics
   * TODO: Replace with actual service calls when available
   */
  private loadPersonalStats(): void {
    // Simulate loading delay
    setTimeout(() => {
      const mockData = this.generateMockPersonalStats();
      this.statsData.set(mockData);
    }, 300);

    // Production implementation:
    // this.personalStatsService.getPersonalStats().subscribe({
    //   next: (data) => this.statsData.set({ ...data, loading: false }),
    //   error: (error) => {
    //     console.error('Error loading personal stats:', error);
    //     this.statsData.update(current => ({ ...current, loading: false }));
    //   }
    // });
  }

  /**
   * Formats duration from seconds to human-readable format
   */
  formatDuration(seconds: number): string {
    if (seconds === 0) return '0m';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  }

  /**
   * Gets the status text for today's activity
   */
  getTodayStatusText(): string {
    const today = this.todayStats();

    if (today.runsCount === 0) {
      return 'Noch keine Aktivität heute';
    }

    if (today.lastRunTime) {
      const hours = today.lastRunTime.getHours();
      const minutes = today.lastRunTime.getMinutes();
      const timeStr = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
      return `Letzter Lauf um ${timeStr} Uhr`;
    }

    return `${today.runsCount} ${today.runsCount === 1 ? 'Lauf' : 'Läufe'} heute`;
  }

  /**
   * Gets the appropriate icon for today's status
   */
  getTodayStatusIcon(): string {
    const today = this.todayStats();
    return today.runsCount > 0 ? 'directions_run' : 'schedule';
  }

  /**
   * Generates mock personal stats for development
   * TODO: Remove when real service is integrated
   */
  private generateMockPersonalStats(): PersonalStatsData {
    // Generate today's stats
    const hasRunToday = Math.random() > 0.3; // 70% chance of having run today
    const todayDistance = hasRunToday ? 3.5 + Math.random() * 7 : 0;
    const todayDuration = hasRunToday ? Math.floor((todayDistance / 0.15) * 60) : 0; // ~6:30 min/km pace
    const runsCount = hasRunToday ? (Math.random() > 0.7 ? 2 : 1) : 0;

    const lastRunTime = hasRunToday ? new Date() : undefined;
    if (lastRunTime) {
      lastRunTime.setHours(lastRunTime.getHours() - Math.floor(Math.random() * 8));
    }

    const todayStats: TodayStats = {
      distanceKm: todayDistance,
      durationSeconds: todayDuration,
      runsCount,
      lastRunTime,
      goalAchieved: todayDistance >= 5,
      dailyGoalKm: 5
    };

    // Generate week stats
    const weekDays = this.generateWeekDays();
    const activeDays = weekDays.filter(d => d.hasRun).length;
    const totalDistance = weekDays.reduce((sum, d) => sum + (d.distanceKm || 0), 0);

    // Calculate current streak
    let currentStreak = 0;
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (let i = weekDays.length - 1; i >= 0; i--) {
      const day = weekDays[i];
      const dayDate = new Date(day.date);
      dayDate.setHours(0, 0, 0, 0);

      if (dayDate <= today && day.hasRun) {
        currentStreak++;
      } else if (dayDate < today) {
        break;
      }
    }

    const weekStats: WeekStats = {
      days: weekDays,
      totalDistanceKm: totalDistance,
      activeDays,
      currentStreak
    };

    // Generate recent achievements
    const recentAchievements = this.generateMockAchievements();

    return {
      today: todayStats,
      week: weekStats,
      recentAchievements,
      loading: false
    };
  }

  /**
   * Generates mock week days (Monday to Sunday)
   */
  private generateWeekDays(): DayStatus[] {
    const dayLabels = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
    const today = new Date();
    const currentDayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, ...
    const mondayOffset = currentDayOfWeek === 0 ? -6 : 1 - currentDayOfWeek;

    const monday = new Date(today);
    monday.setDate(today.getDate() + mondayOffset);
    monday.setHours(0, 0, 0, 0);

    return Array.from({ length: 7 }, (_, i) => {
      const date = new Date(monday);
      date.setDate(monday.getDate() + i);

      const isToday = date.toDateString() === today.toDateString();
      const isFuture = date > today;

      // Don't mark future days as having runs
      const hasRun = !isFuture && Math.random() > 0.4; // 60% chance for past/today
      const distanceKm = hasRun ? 3 + Math.random() * 8 : undefined;

      return {
        dayOfWeek: i,
        dayLabel: dayLabels[i],
        hasRun,
        distanceKm,
        date,
        isToday
      };
    });
  }

  /**
   * Generates mock recent achievements
   */
  private generateMockAchievements(): RecentAchievement[] {
    const possibleAchievements = [
      {
        id: 'ach-1',
        name: 'Erste 10km',
        description: 'Laufe zum ersten Mal 10 Kilometer',
        icon: '🏃',
        category: 'DISTANCE' as const
      },
      {
        id: 'ach-2',
        name: '7-Tage-Streak',
        description: 'Laufe 7 Tage in Folge',
        icon: '🔥',
        category: 'STREAK' as const
      },
      {
        id: 'ach-3',
        name: 'Main-Rundläufer',
        description: 'Vollende die komplette Mainufer-Runde',
        icon: '🌊',
        category: 'ROUTE' as const
      },
      {
        id: 'ach-4',
        name: 'Frühaufsteher',
        description: 'Laufe vor 7 Uhr morgens',
        icon: '🌅',
        category: 'SPECIAL' as const
      },
      {
        id: 'ach-5',
        name: 'Speedster',
        description: 'Laufe unter 5:00 min/km Pace',
        icon: '⚡',
        category: 'SPEED' as const
      }
    ];

    // Randomly select 2-3 achievements
    const count = 2 + Math.floor(Math.random() * 2);
    const shuffled = [...possibleAchievements].sort(() => Math.random() - 0.5);
    const selected = shuffled.slice(0, count);

    return selected.map(ach => {
      const daysAgo = Math.floor(Math.random() * 5); // Earned within last 5 days
      const earnedAt = new Date();
      earnedAt.setDate(earnedAt.getDate() - daysAgo);

      return {
        ...ach,
        earnedAt,
        isNew: daysAgo <= 2 // New if earned in last 2 days
      };
    });
  }
}
