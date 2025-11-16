import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface GarminConnectionResponse {
  success: boolean;
  message: string;
  garminUserId?: string;
  connectedAt?: string;
}

export interface GarminSyncResponse {
  success: boolean;
  message: string;
  syncedCount?: number;
  runs?: any[];
}

export interface GarminAuthUrlResponse {
  authUrl: string;
}

export interface GarminStatusResponse {
  connected: boolean;
  garminUserId?: string;
  connectedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GarminService {
  private apiUrl = `${environment.apiUrl}/api/garmin`;

  constructor(private http: HttpClient) {}

  /**
   * Gets the Garmin OAuth authorization URL
   */
  getAuthUrl(): Observable<GarminAuthUrlResponse> {
    return this.http.get<GarminAuthUrlResponse>(`${this.apiUrl}/auth-url`);
  }

  /**
   * Connects user's Garmin account
   */
  connectGarmin(authorizationCode: string): Observable<GarminConnectionResponse> {
    const params = new HttpParams().set('code', authorizationCode);
    return this.http.post<GarminConnectionResponse>(`${this.apiUrl}/connect`, null, { params });
  }

  /**
   * Disconnects user's Garmin account
   */
  disconnectGarmin(): Observable<GarminConnectionResponse> {
    return this.http.delete<GarminConnectionResponse>(`${this.apiUrl}/disconnect`);
  }

  /**
   * Syncs activities from Garmin
   */
  syncActivities(since?: Date): Observable<GarminSyncResponse> {
    let params = new HttpParams();
    if (since) {
      params = params.set('since', since.toISOString());
    }
    return this.http.post<GarminSyncResponse>(`${this.apiUrl}/sync`, null, { params });
  }

  /**
   * Gets Garmin connection status
   */
  getStatus(): Observable<GarminStatusResponse> {
    return this.http.get<GarminStatusResponse>(`${this.apiUrl}/status`);
  }

  /**
   * Opens Garmin authorization in a new window
   */
  async openGarminAuth(): Promise<string | null> {
    const authUrlResponse = await this.getAuthUrl().toPromise();
    if (!authUrlResponse) {
      throw new Error('Failed to get Garmin auth URL');
    }

    const width = 600;
    const height = 700;
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;

    const popup = window.open(
      authUrlResponse.authUrl,
      'Garmin Authorization',
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
          if (popupUrl.includes('garmin/callback')) {
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
          // Cross-origin error - popup is still on Garmin's domain
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
