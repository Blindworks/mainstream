import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

import { ApiService } from '../../../shared/services/api.service';
import { User, UserRegistration, LoginRequest, AuthResponse, AuthState } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'mainstream_token';
  private readonly USER_KEY = 'mainstream_user';

  private authState = signal<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null
  });

  private authSubject = new BehaviorSubject<AuthState>(this.authState());

  constructor(
    private http: HttpClient,
    private apiService: ApiService,
    private router: Router
  ) {
    this.initializeAuthState();
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

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    
    const newState: AuthState = {
      isAuthenticated: false,
      user: null,
      token: null
    };
    
    this.updateAuthState(newState);
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
}