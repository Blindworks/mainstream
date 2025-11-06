import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface PeriodStats {
  activeUsers: number;
  competitions: number;
  runs: number;
  trophies: number;
}

export interface DashboardStats {
  today: PeriodStats;
  thisMonth: PeriodStats;
  thisYear: PeriodStats;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/dashboard`;
  }

  /**
   * Get dashboard statistics for today, this month, and this year
   */
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/stats`);
  }
}
