/**
 * Model definitions for Personal Stats component
 */

/**
 * Today's running statistics
 */
export interface TodayStats {
  /** Distance run today in kilometers */
  distanceKm: number;

  /** Total duration in seconds */
  durationSeconds: number;

  /** Number of runs completed today */
  runsCount: number;

  /** Timestamp of the last run (if any) */
  lastRunTime?: Date;

  /** Whether the daily goal has been achieved */
  goalAchieved: boolean;

  /** Daily goal in kilometers */
  dailyGoalKm: number;
}

/**
 * Week statistics with daily completion status
 */
export interface WeekStats {
  /** Array of 7 days (Monday to Sunday) */
  days: DayStatus[];

  /** Total distance for the week in km */
  totalDistanceKm: number;

  /** Number of active running days */
  activeDays: number;

  /** Current streak in days */
  currentStreak: number;
}

/**
 * Status for a single day in the week
 */
export interface DayStatus {
  /** Day of week (0 = Monday, 6 = Sunday) */
  dayOfWeek: number;

  /** Day label (Mo, Di, Mi, etc.) */
  dayLabel: string;

  /** Whether a run was completed on this day */
  hasRun: boolean;

  /** Distance run on this day in km */
  distanceKm?: number;

  /** Date for this day */
  date: Date;

  /** Whether this is today */
  isToday: boolean;
}

/**
 * Achievement/Trophy information for display
 */
export interface RecentAchievement {
  /** Achievement ID */
  id: string;

  /** Achievement name */
  name: string;

  /** Achievement description */
  description: string;

  /** Icon emoji or URL */
  icon: string;

  /** When the achievement was earned */
  earnedAt: Date;

  /** Achievement category */
  category: 'DISTANCE' | 'STREAK' | 'ROUTE' | 'SPEED' | 'SPECIAL';

  /** Whether this achievement is new (earned in last 7 days) */
  isNew: boolean;
}

/**
 * Complete personal stats data structure
 */
export interface PersonalStatsData {
  /** Today's statistics */
  today: TodayStats;

  /** This week's statistics */
  week: WeekStats;

  /** Recent achievements (max 3) */
  recentAchievements: RecentAchievement[];

  /** Whether data is loading */
  loading: boolean;
}
