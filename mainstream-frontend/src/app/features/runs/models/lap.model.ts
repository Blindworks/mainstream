export interface Lap {
  id: number;
  lapNumber: number;
  startTime: Date;
  endTime?: Date;

  // Timing
  totalTimerTime?: number; // seconds
  formattedDuration?: string;

  // Distance
  totalDistance?: number; // meters
  distanceKm?: number;

  // Speed
  avgSpeed?: number; // m/s
  maxSpeed?: number; // m/s
  avgSpeedKmh?: number;
  maxSpeedKmh?: number;

  // Pace
  avgPace?: string; // min/km

  // Heart Rate
  avgHeartRate?: number;
  maxHeartRate?: number;

  // Running Dynamics
  avgCadence?: number;
  totalSteps?: number;
  avgStrideLength?: number;

  // Elevation
  totalAscent?: number;
  totalDescent?: number;

  // Energy
  totalCalories?: number;

  // Lap Type
  lapTrigger?: string;
  sport?: string;
}
