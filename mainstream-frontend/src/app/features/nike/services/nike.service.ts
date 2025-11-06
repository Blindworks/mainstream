import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface NikeConnectionResponse {
  success: boolean;
  message: string;
  nikeUserId?: string;
  connectedAt?: string;
}

export interface NikeSyncResponse {
  success: boolean;
  message: string;
  syncedCount?: number;
  runs?: any[];
}

export interface NikeStatusResponse {
  connected: boolean;
  nikeUserId?: string;
  connectedAt?: string;
}

export interface NikeInstructionsResponse {
  title: string;
  steps: string[];
  note: string;
}

@Injectable({
  providedIn: 'root'
})
export class NikeService {
  private apiUrl = `${environment.apiUrl}/api/nike`;

  constructor(private http: HttpClient) {}

  /**
   * Connects user's Nike account with access token
   */
  connectNike(accessToken: string): Observable<NikeConnectionResponse> {
    return this.http.post<NikeConnectionResponse>(`${this.apiUrl}/connect`, { accessToken });
  }

  /**
   * Disconnects user's Nike account
   */
  disconnectNike(): Observable<NikeConnectionResponse> {
    return this.http.delete<NikeConnectionResponse>(`${this.apiUrl}/disconnect`);
  }

  /**
   * Syncs all activities from Nike
   */
  syncActivities(): Observable<NikeSyncResponse> {
    return this.http.post<NikeSyncResponse>(`${this.apiUrl}/sync`, null);
  }

  /**
   * Gets Nike connection status
   */
  getStatus(): Observable<NikeStatusResponse> {
    return this.http.get<NikeStatusResponse>(`${this.apiUrl}/status`);
  }

  /**
   * Gets instructions for obtaining Nike access token
   */
  getInstructions(): Observable<NikeInstructionsResponse> {
    return this.http.get<NikeInstructionsResponse>(`${this.apiUrl}/instructions`);
  }
}
