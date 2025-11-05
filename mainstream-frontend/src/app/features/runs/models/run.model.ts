export interface Run {
  id: number;
  userId: number;
  title: string;
  description?: string;
  startTime: Date;
  endTime?: Date;
  durationSeconds?: number;
  distanceMeters?: number;
  averagePaceSecondsPerKm?: number;
  maxSpeedKmh?: number;
  averageSpeedKmh?: number;
  caloriesBurned?: number;
  elevationGainMeters?: number;
  elevationLossMeters?: number;
  runType: RunType;
  status: RunStatus;
  weatherCondition?: string;
  temperatureCelsius?: number;
  humidityPercentage?: number;
  isPublic: boolean;
  routeId?: number;
  createdAt: Date;
  updatedAt: Date;
}

export enum RunType {
  OUTDOOR = 'OUTDOOR',
  TREADMILL = 'TREADMILL', 
  TRACK = 'TRACK',
  TRAIL = 'TRAIL'
}

export enum RunStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE', 
  COMPLETED = 'COMPLETED',
  PAUSED = 'PAUSED',
  CANCELLED = 'CANCELLED'
}

export interface RunSummary {
  id: number;
  title: string;
  startTime: Date;
  durationSeconds?: number;
  distanceKm?: number;
  averagePace?: string;
  runType: RunType;
  status: RunStatus;
  caloriesBurned?: number;
}