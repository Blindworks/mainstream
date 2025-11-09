export interface RouteStats {
  routeId: number;
  todayCount: number;
  thisWeekCount: number;
  thisMonthCount: number;
  thisYearCount: number;
  totalCount: number;
}

export interface PredefinedRoute {
  id: number;
  name: string;
  description?: string;
  imageUrl?: string;
  originalFilename: string;
  distanceMeters: number;
  elevationGainMeters?: number;
  elevationLossMeters?: number;
  startLatitude?: number;
  startLongitude?: number;
  isActive: boolean;
  trackPointCount: number;
  createdAt: Date;
  updatedAt: Date;
  trackPoints?: RouteTrackPoint[];
  stats?: RouteStats;
}

export interface RouteTrackPoint {
  id: number;
  sequenceNumber: number;
  latitude: number;
  longitude: number;
  elevation?: number;
  distanceFromStartMeters: number;
}

// Helper functions for display
export function formatDistance(meters: number): string {
  const km = meters / 1000;
  return `${km.toFixed(2)} km`;
}

export function formatElevation(meters: number | undefined): string {
  if (meters === undefined || meters === null) return 'N/A';
  return `${meters.toFixed(0)} m`;
}

export function getRouteStatusColor(isActive: boolean): string {
  return isActive ? 'primary' : '';
}

export function getRouteStatusLabel(isActive: boolean): string {
  return isActive ? 'Aktiv' : 'Inaktiv';
}
