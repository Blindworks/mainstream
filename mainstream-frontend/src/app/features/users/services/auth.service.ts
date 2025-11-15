import { Injectable, signal, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, interval, Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService } from '../../../shared/services/api.service';
import { User, UserRegistration, LoginRequest, AuthResponse, AuthState, PasswordResetRequest, PasswordReset } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService implements OnDestroy {
  private readonly TOKEN_KEY = 'mainstream_token';
  private readonly USER_KEY = 'mainstream_user';
  private readonly SESSION_CHECK_INTERVAL = 60000; // Check every minute
  private readonly SESSION_WARNING_THRESHOLD = 5 * 60; // Warn 5 minutes before expiry

  private authState = signal<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null
  });

  private authSubject = new BehaviorSubject<AuthState>(this.authState());
  private sessionCheckSubscription?: Subscription;
  private hasShownExpiryWarning = false;

  constructor(
    private http: HttpClient,
    private apiService: ApiService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.initializeAuthState();
    this.startSessionMonitoring();
  }

  ngOnDestroy(): void {
    this.stopSessionMonitoring();
  }

  get authState$(): Observable<AuthState> {
    return this.authSubject.asObservable();
  }

  get currentUser(): User | null {
    return this.authState().user;
  }

  get isAuthenticated(): boolean {
    return this.authState().isAuthenticated;
  }

  get token(): string | null {
    return this.authState().token;
  }

  private initializeAuthState(): void {
    const storedToken = localStorage.getItem(this.TOKEN_KEY);
    const storedUser = localStorage.getItem(this.USER_KEY);

    if (storedToken && storedUser) {
      try {
        const user = JSON.parse(storedUser);
        const newState: AuthState = {
          isAuthenticated: true,
          user: user,
          token: storedToken
        };
        this.updateAuthState(newState);
      } catch (error) {
        console.error('Error parsing stored user data:', error);
        this.logout();
      }
    }
  }

  register(userData: UserRegistration): Observable<User> {
    return this.http.post<User>(`${this.apiService.apiUrl}/api/auth/register`, userData);
  }

  login(loginData: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiService.apiUrl}/api/auth/login`, loginData)
      .pipe(
        tap(response => {
          this.handleAuthSuccess(response);
        })
      );
  }

  requestPasswordReset(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiService.apiUrl}/api/auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.apiService.apiUrl}/api/auth/reset-password`, { token, newPassword });
  }

  logout(reason?: 'expired' | 'manual' | 'unauthorized'): void {
    this.stopSessionMonitoring();

    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);

    const newState: AuthState = {
      isAuthenticated: false,
      user: null,
      token: null
    };

    this.updateAuthState(newState);

    // Show appropriate message based on logout reason
    if (reason === 'expired') {
      this.snackBar.open(
        'Deine Sitzung ist abgelaufen. Bitte melde dich erneut an.',
        'OK',
        { duration: 5000, panelClass: ['error-snackbar'] }
      );
    } else if (reason === 'unauthorized') {
      this.snackBar.open(
        'Du wurdest abgemeldet. Bitte melde dich erneut an.',
        'OK',
        { duration: 5000, panelClass: ['error-snackbar'] }
      );
    }

    this.router.navigate(['/auth/login']);
  }

  validateToken(): Observable<boolean> {
    const token = this.token;
    if (!token) {
      return new Observable(observer => {
        observer.next(false);
        observer.complete();
      });
    }

    return this.http.post<boolean>(`${this.apiService.apiUrl}/api/auth/validate`, null, {
      params: { token }
    });
  }

  refreshUserData(): Observable<User> {
    return this.http.get<User>(`${this.apiService.apiUrl}/api/auth/user`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(user => {
        const currentState = this.authState();
        const newState: AuthState = {
          ...currentState,
          user: user
        };
        this.updateAuthState(newState);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      })
    );
  }

  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response.user));

    const newState: AuthState = {
      isAuthenticated: true,
      user: response.user,
      token: response.token
    };

    this.updateAuthState(newState);
    this.restartSessionMonitoring();
  }

  private updateAuthState(newState: AuthState): void {
    this.authState.set(newState);
    this.authSubject.next(newState);
  }

  getAuthHeaders(): { [key: string]: string } {
    const token = this.token;
    if (token) {
      return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      };
    }
    return {};
  }

  isTokenExpired(): boolean {
    const token = this.token;
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Date.now() / 1000;
      return payload.exp < now;
    } catch (error) {
      return true;
    }
  }

  private startSessionMonitoring(): void {
    // Only monitor if user is authenticated
    if (!this.isAuthenticated) {
      return;
    }

    this.stopSessionMonitoring(); // Clean up any existing subscription

    this.sessionCheckSubscription = interval(this.SESSION_CHECK_INTERVAL).subscribe(() => {
      this.checkSessionExpiry();
    });

    // Do an immediate check
    this.checkSessionExpiry();
  }

  private stopSessionMonitoring(): void {
    if (this.sessionCheckSubscription) {
      this.sessionCheckSubscription.unsubscribe();
      this.sessionCheckSubscription = undefined;
    }
    this.hasShownExpiryWarning = false;
  }

  private checkSessionExpiry(): void {
    if (!this.isAuthenticated) {
      this.stopSessionMonitoring();
      return;
    }

    if (this.isTokenExpired()) {
      // Token has expired, log out immediately
      this.logout('expired');
      return;
    }

    // Check if token is about to expire
    const timeUntilExpiry = this.getTimeUntilExpiry();
    if (timeUntilExpiry !== null && timeUntilExpiry <= this.SESSION_WARNING_THRESHOLD && !this.hasShownExpiryWarning) {
      const minutes = Math.ceil(timeUntilExpiry / 60);
      this.snackBar.open(
        `Deine Sitzung läuft in ${minutes} Minute(n) ab.`,
        'OK',
        { duration: 8000, panelClass: ['warning-snackbar'] }
      );
      this.hasShownExpiryWarning = true;
    }
  }

  private getTimeUntilExpiry(): number | null {
    const token = this.token;
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Date.now() / 1000;
      return payload.exp - now;
    } catch (error) {
      return null;
    }
  }

  // Call this after successful login to restart monitoring
  private restartSessionMonitoring(): void {
    this.hasShownExpiryWarning = false;
    this.startSessionMonitoring();
  }
}