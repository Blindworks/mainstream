import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface StravaConnectionResponse {
  success: boolean;
  message: string;
  stravaUserId?: number;
  connectedAt?: string;
}

export interface StravaSyncResponse {
  success: boolean;
  message: string;
  syncedCount?: number;
  runs?: any[];
}

export interface StravaAuthUrlResponse {
  authUrl: string;
}

export interface StravaStatusResponse {
  connected: boolean;
  stravaUserId?: number;
  connectedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class StravaService {
  private apiUrl = `${environment.apiUrl}/api/strava`;

  constructor(private http: HttpClient) {}

  /**
   * Gets the Strava OAuth authorization URL
   */
  getAuthUrl(): Observable<StravaAuthUrlResponse> {
    return this.http.get<StravaAuthUrlResponse>(`${this.apiUrl}/auth-url`);
  }

  /**
   * Connects user's Strava account
   */
  connectStrava(authorizationCode: string): Observable<StravaConnectionResponse> {
    const params = new HttpParams().set('code', authorizationCode);
    return this.http.post<StravaConnectionResponse>(`${this.apiUrl}/connect`, null, { params });
  }

  /**
   * Disconnects user's Strava account
   */
  disconnectStrava(): Observable<StravaConnectionResponse> {
    return this.http.delete<StravaConnectionResponse>(`${this.apiUrl}/disconnect`);
  }

  /**
   * Syncs activities from Strava
   */
  syncActivities(since?: Date): Observable<StravaSyncResponse> {
    let params = new HttpParams();
    if (since) {
      params = params.set('since', since.toISOString());
    }
    return this.http.post<StravaSyncResponse>(`${this.apiUrl}/sync`, null, { params });
  }

  /**
   * Gets Strava connection status
   */
  getStatus(): Observable<StravaStatusResponse> {
    return this.http.get<StravaStatusResponse>(`${this.apiUrl}/status`);
  }

  /**
   * Opens Strava authorization in a new window
   */
  async openStravaAuth(): Promise<string | null> {
    const authUrlResponse = await this.getAuthUrl().toPromise();
    if (!authUrlResponse) {
      throw new Error('Failed to get Strava auth URL');
    }

    const width = 600;
    const height = 700;
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;

    const popup = window.open(
      authUrlResponse.authUrl,
      'Strava Authorization',
      `width=${width},height=${height},left=${left},top=${top}`
    );

    return new Promise((resolve, reject) => {
      const checkPopup = setInterval(() => {
        if (!popup || popup.closed) {
          clearInterval(checkPopup);
          reject(new Error('Authorization window was closed'));
          return;
        }

        try {
          const popupUrl = popup.location.href;
          if (popupUrl.includes('strava/callback')) {
            const url = new URL(popupUrl);
            const code = url.searchParams.get('code');
            const error = url.searchParams.get('error');

            clearInterval(checkPopup);
            popup.close();

            if (error) {
              reject(new Error(`Authorization failed: ${error}`));
            } else if (code) {
              resolve(code);
            } else {
              reject(new Error('No authorization code received'));
            }
          }
        } catch (e) {
          // Cross-origin error - popup is still on Strava's domain
        }
      }, 500);

      // Timeout after 5 minutes
      setTimeout(() => {
        clearInterval(checkPopup);
        if (popup && !popup.closed) {
          popup.close();
        }
        reject(new Error('Authorization timeout'));
      }, 300000);
    });
  }
}
