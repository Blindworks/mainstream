import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AppSettings {
  key: string;
  value: string;
  description: string;
}

export interface MaintenanceModeStatus {
  enabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Get all settings
   */
  getAllSettings(): Observable<AppSettings[]> {
    return this.http.get<AppSettings[]>(`${this.apiUrl}/api/settings`);
  }

  /**
   * Get a specific setting
   */
  getSetting(key: string): Observable<AppSettings> {
    return this.http.get<AppSettings>(`${this.apiUrl}/api/settings/${key}`);
  }

  /**
   * Update a setting
   */
  updateSetting(key: string, value: string): Observable<AppSettings> {
    return this.http.patch<AppSettings>(`${this.apiUrl}/api/settings/${key}`, { value });
  }

  /**
   * Check maintenance mode status (public endpoint)
   */
  getMaintenanceModeStatus(): Observable<MaintenanceModeStatus> {
    return this.http.get<MaintenanceModeStatus>(`${this.apiUrl}/api/settings/maintenance-mode`);
  }
}
