export enum TrophyType {
  DISTANCE_MILESTONE = 'DISTANCE_MILESTONE',
  STREAK = 'STREAK',
  ROUTE_COMPLETION = 'ROUTE_COMPLETION',
  CONSISTENCY = 'CONSISTENCY',
  TIME_BASED = 'TIME_BASED',
  EXPLORER = 'EXPLORER',
  LOCATION_BASED = 'LOCATION_BASED',
  SPECIAL = 'SPECIAL'
}

export enum TrophyCategory {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  ELITE = 'ELITE',
  SPECIAL = 'SPECIAL'
}

export interface Trophy {
  id: number;
  code: string;
  name: string;
  description: string;
  type: TrophyType;
  category: TrophyCategory;
  iconUrl?: string;
  criteriaValue?: number;
  isActive: boolean;
  displayOrder?: number;

  // Location-based trophy fields
  latitude?: number;
  longitude?: number;
  collectionRadiusMeters?: number;
  validFrom?: Date | string;
  validUntil?: Date | string;
  imageUrl?: string;

  createdAt: Date;
  updatedAt: Date;
}

export interface UserTrophy {
  id: number;
  userId: number;
  userName: string;
  trophy: Trophy;
  activityId?: number;
  earnedAt: Date;
  metadata?: string;
}

export interface TrophyWithProgress extends Trophy {
  isEarned: boolean;
  earnedAt?: Date;
  progress?: number;
  progressMax?: number;
}
