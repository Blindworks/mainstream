export interface Competition {
  id: number;
  title: string;
  description?: string;
  type: CompetitionType;
  status: CompetitionStatus;
  difficulty?: DifficultyLevel;
  startDate: Date;
  endDate: Date;
  prizeDescription?: string;
  rules?: string;
  maxParticipants?: number;
  currentParticipants: number;
  iconUrl?: string;
  createdById: number;
  createdByName?: string;
  createdAt: Date;
  updatedAt: Date;
  isUserParticipating: boolean;
}

export interface CompetitionSummary {
  id: number;
  title: string;
  description?: string;
  type: CompetitionType;
  status: CompetitionStatus;
  difficulty?: DifficultyLevel;
  startDate: Date;
  endDate: Date;
  maxParticipants?: number;
  currentParticipants: number;
  iconUrl?: string;
  isUserParticipating: boolean;
}

export interface CompetitionParticipant {
  id: number;
  competitionId: number;
  userId: number;
  userName: string;
  joinedDate: Date;
  status: ParticipantStatus;
  finalScore?: number;
  bestPerformance?: number;
  position?: number;
  currentPosition?: number;
  currentScore?: number;
}

export interface LeaderboardEntry {
  position: number;
  userId: number;
  userName: string;
  score: number;
  bestPerformance?: number;
  performanceUnit: string;
  isCurrentUser: boolean;
}

export enum CompetitionType {
  FASTEST_5K = 'FASTEST_5K',
  FASTEST_10K = 'FASTEST_10K',
  FASTEST_HALF_MARATHON = 'FASTEST_HALF_MARATHON',
  FASTEST_MARATHON = 'FASTEST_MARATHON',
  MOST_DISTANCE = 'MOST_DISTANCE',
  MOST_RUNS = 'MOST_RUNS',
  MOST_ELEVATION = 'MOST_ELEVATION',
  LONGEST_SINGLE_RUN = 'LONGEST_SINGLE_RUN',
  CONSISTENCY_CHALLENGE = 'CONSISTENCY_CHALLENGE',
  SPECIFIC_ROUTE = 'SPECIFIC_ROUTE',
  TEAM_CHALLENGE = 'TEAM_CHALLENGE',
  CUSTOM = 'CUSTOM'
}

export enum CompetitionStatus {
  DRAFT = 'DRAFT',
  UPCOMING = 'UPCOMING',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum DifficultyLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  ELITE = 'ELITE'
}

export enum ParticipantStatus {
  REGISTERED = 'REGISTERED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  DISQUALIFIED = 'DISQUALIFIED',
  WITHDRAWN = 'WITHDRAWN'
}

// Helper functions for display
export function getCompetitionTypeLabel(type: CompetitionType): string {
  const labels: Record<CompetitionType, string> = {
    [CompetitionType.FASTEST_5K]: 'Schnellster 5K',
    [CompetitionType.FASTEST_10K]: 'Schnellster 10K',
    [CompetitionType.FASTEST_HALF_MARATHON]: 'Schnellster Halbmarathon',
    [CompetitionType.FASTEST_MARATHON]: 'Schnellster Marathon',
    [CompetitionType.MOST_DISTANCE]: 'Meiste Distanz',
    [CompetitionType.MOST_RUNS]: 'Meiste Läufe',
    [CompetitionType.MOST_ELEVATION]: 'Meiste Höhenmeter',
    [CompetitionType.LONGEST_SINGLE_RUN]: 'Längster Einzellauf',
    [CompetitionType.CONSISTENCY_CHALLENGE]: 'Konsistenz Challenge',
    [CompetitionType.SPECIFIC_ROUTE]: 'Spezifische Route',
    [CompetitionType.TEAM_CHALLENGE]: 'Team Challenge',
    [CompetitionType.CUSTOM]: 'Benutzerdefiniert'
  };
  return labels[type];
}

export function getCompetitionStatusLabel(status: CompetitionStatus): string {
  const labels: Record<CompetitionStatus, string> = {
    [CompetitionStatus.DRAFT]: 'Entwurf',
    [CompetitionStatus.UPCOMING]: 'Bevorstehend',
    [CompetitionStatus.ACTIVE]: 'Aktiv',
    [CompetitionStatus.COMPLETED]: 'Abgeschlossen',
    [CompetitionStatus.CANCELLED]: 'Abgesagt'
  };
  return labels[status];
}

export function getDifficultyLabel(difficulty: DifficultyLevel): string {
  const labels: Record<DifficultyLevel, string> = {
    [DifficultyLevel.BEGINNER]: 'Anfänger',
    [DifficultyLevel.INTERMEDIATE]: 'Fortgeschritten',
    [DifficultyLevel.ADVANCED]: 'Erfahren',
    [DifficultyLevel.ELITE]: 'Elite'
  };
  return labels[difficulty];
}

export function getCompetitionStatusColor(status: CompetitionStatus): string {
  const colors: Record<CompetitionStatus, string> = {
    [CompetitionStatus.DRAFT]: 'accent',
    [CompetitionStatus.UPCOMING]: 'primary',
    [CompetitionStatus.ACTIVE]: 'warn',
    [CompetitionStatus.COMPLETED]: '',
    [CompetitionStatus.CANCELLED]: ''
  };
  return colors[status];
}

export function getDifficultyColor(difficulty: DifficultyLevel): string {
  const colors: Record<DifficultyLevel, string> = {
    [DifficultyLevel.BEGINNER]: 'primary',
    [DifficultyLevel.INTERMEDIATE]: 'accent',
    [DifficultyLevel.ADVANCED]: 'warn',
    [DifficultyLevel.ELITE]: 'warn'
  };
  return colors[difficulty];
}
