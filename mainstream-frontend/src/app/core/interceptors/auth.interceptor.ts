import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuthService } from '../../features/users/services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip auth headers for auth endpoints
    if (req.url.includes('/api/auth/login') || req.url.includes('/api/auth/register')) {
      return next.handle(req);
    }

    // Add auth headers if user is authenticated
    const token = this.authService.token;
    const userId = this.authService.currentUser?.id;

    if (token && !this.authService.isTokenExpired()) {
      const headers: any = {
        Authorization: `Bearer ${token}`
      };

      // Add X-User-Id header if user ID is available
      if (userId) {
        headers['X-User-Id'] = userId.toString();
      }

      const authReq = req.clone({
        setHeaders: headers
      });

      return next.handle(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
          this.handleAuthError(error);
          return throwError(() => error);
        })
      );
    }

    // Handle requests without token
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        this.handleAuthError(error);
        return throwError(() => error);
      })
    );
  }

  private handleAuthError(error: HttpErrorResponse): void {
    // Handle 401 Unauthorized - session expired or invalid token
    if (error.status === 401) {
      // Check if we're already on the login page to avoid redirect loops
      if (!window.location.pathname.includes('/auth/login')) {
        this.authService.logout('unauthorized');
      }
    }

    // Handle 403 Forbidden - user doesn't have permission
    // Don't logout for 403, just let the error propagate
    // The component can handle showing an appropriate message
  }
}