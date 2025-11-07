import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PredefinedRoute } from '../models/predefined-route.model';
import { ApiService } from '../../../shared/services/api.service';

@Injectable({
  providedIn: 'root'
})
export class PredefinedRouteService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/routes`;
  }

  /**
   * Get all predefined routes
   */
  getAllRoutes(activeOnly: boolean = true): Observable<PredefinedRoute[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());

    return this.http.get<any[]>(`${this.baseUrl}`, { params }).pipe(
      map(routes => routes.map(route => this.mapToPredefinedRoute(route)))
    );
  }

  /**
   * Get all predefined routes with statistics
   */
  getAllRoutesWithStats(activeOnly: boolean = true): Observable<PredefinedRoute[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());

    return this.http.get<any[]>(`${this.baseUrl}/with-stats`, { params }).pipe(
      map(routes => routes.map(route => this.mapToPredefinedRoute(route)))
    );
  }

  /**
   * Get route by ID
   */
  getRouteById(id: number): Observable<PredefinedRoute> {
    return this.http.get<any>(`${this.baseUrl}/${id}`).pipe(
      map(route => this.mapToPredefinedRoute(route))
    );
  }

  // Mapping function
  private mapToPredefinedRoute(data: any): PredefinedRoute {
    return {
      id: data.id,
      name: data.name,
      description: data.description,
      originalFilename: data.originalFilename,
      distanceMeters: data.distanceMeters,
      elevationGainMeters: data.elevationGainMeters,
      elevationLossMeters: data.elevationLossMeters,
      startLatitude: data.startLatitude,
      startLongitude: data.startLongitude,
      isActive: data.isActive,
      trackPointCount: data.trackPointCount,
      createdAt: new Date(data.createdAt),
      updatedAt: new Date(data.updatedAt),
      trackPoints: data.trackPoints,
      stats: data.stats
    };
  }

  // Formatting helpers
  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  }

  formatDistance(meters: number): string {
    const km = meters / 1000;
    return `${km.toFixed(2)} km`;
  }

  formatElevation(meters: number | undefined): string {
    if (meters === undefined || meters === null) return 'N/A';
    return `${meters.toFixed(0)} m`;
  }
}
