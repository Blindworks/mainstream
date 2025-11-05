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
  activityId: number;
  achievementValue: number;
  achievementDescription: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080/api';

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

    return this.http.post<PredefinedRoute>(`${this.apiUrl}/routes/upload`, formData);
  }

  /**
   * Get all predefined routes
   */
  getAllRoutes(activeOnly: boolean = false): Observable<PredefinedRoute[]> {
    const params = activeOnly ? { activeOnly: 'true' } : {};
    return this.http.get<PredefinedRoute[]>(`${this.apiUrl}/routes`, { params });
  }

  /**
   * Get route by ID
   */
  getRouteById(id: number): Observable<PredefinedRoute> {
    return this.http.get<PredefinedRoute>(`${this.apiUrl}/routes/${id}`);
  }

  /**
   * Activate a route
   */
  activateRoute(id: number): Observable<PredefinedRoute> {
    return this.http.put<PredefinedRoute>(`${this.apiUrl}/routes/${id}/activate`, {});
  }

  /**
   * Deactivate a route
   */
  deactivateRoute(id: number): Observable<PredefinedRoute> {
    return this.http.put<PredefinedRoute>(`${this.apiUrl}/routes/${id}/deactivate`, {});
  }

  /**
   * Initialize default trophies
   */
  initializeTrophies(): Observable<string> {
    return this.http.post(`${this.apiUrl}/trophies/initialize`, {}, { responseType: 'text' });
  }

  /**
   * Calculate daily winners manually
   */
  calculateDailyWinners(date?: string): Observable<string> {
    const params = date ? { date } : {};
    return this.http.post(`${this.apiUrl}/daily-winners/calculate`, {}, {
      params,
      responseType: 'text'
    });
  }

  /**
   * Get recent daily winners
   */
  getRecentWinners(days: number = 7): Observable<DailyWinner[]> {
    return this.http.get<DailyWinner[]>(`${this.apiUrl}/daily-winners/recent`, {
      params: { days: days.toString() }
    });
  }
}
