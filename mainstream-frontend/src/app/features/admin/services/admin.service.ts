import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface PredefinedRoute {
  id: number;
  name: string;
  description: string;
  originalFilename: string;
  distanceMeters: number;
  elevationGainMeters: number;
  elevationLossMeters: number;
  startLatitude: number;
  startLongitude: number;
  isActive: boolean;
  trackPointCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface DailyWinner {
  id: number;
  winnerDate: string;
  category: string;
  userId: number;
  userName: string;
  userFirstName: string;
  userLastName: string;
  activityId: number | null;
  achievementValue: number | null;
  achievementDescription: string | null;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Upload GPX file to create predefined route
   */
  uploadGpxRoute(file: File, name: string, description: string): Observable<PredefinedRoute> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', name);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<PredefinedRoute>(`${this.apiUrl}/api/routes/upload`, formData);
  }

  /**
   * Get all predefined routes
   */
  getAllRoutes(activeOnly: boolean = false): Observable<PredefinedRoute[]> {
    if (activeOnly) {
      return this.http.get<PredefinedRoute[]>(`${this.apiUrl}/api/routes`, {
        params: { activeOnly: 'true' }
      });
    }
    return this.http.get<PredefinedRoute[]>(`${this.apiUrl}/api/routes`);
  }

  /**
   * Get route by ID
   */
  getRouteById(id: number): Observable<PredefinedRoute> {
    return this.http.get<PredefinedRoute>(`${this.apiUrl}/api/routes/${id}`);
  }

  /**
   * Activate a route
   */
  activateRoute(id: number): Observable<PredefinedRoute> {
    return this.http.put<PredefinedRoute>(`${this.apiUrl}/api/routes/${id}/activate`, {});
  }

  /**
   * Deactivate a route
   */
  deactivateRoute(id: number): Observable<PredefinedRoute> {
    return this.http.put<PredefinedRoute>(`${this.apiUrl}/api/routes/${id}/deactivate`, {});
  }

  /**
   * Initialize default trophies
   */
  initializeTrophies(): Observable<string> {
    return this.http.post(`${this.apiUrl}/api/trophies/initialize`, {}, { responseType: 'text' }) as Observable<string>;
  }

  /**
   * Calculate daily winners manually
   */
  calculateDailyWinners(date?: string): Observable<string> {
    if (date) {
      return this.http.post(`${this.apiUrl}/api/daily-winners/calculate`, {}, {
        params: { date },
        responseType: 'text'
      }) as Observable<string>;
    }
    return this.http.post(`${this.apiUrl}/api/daily-winners/calculate`, {}, {
      responseType: 'text'
    }) as Observable<string>;
  }

  /**
   * Get recent daily winners
   */
  getRecentWinners(days: number = 7): Observable<DailyWinner[]> {
    return this.http.get<DailyWinner[]>(`${this.apiUrl}/api/daily-winners/recent`, {
      params: { days: days.toString() }
    });
  }
}
