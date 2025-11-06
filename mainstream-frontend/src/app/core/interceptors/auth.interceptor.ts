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
    if (req.url.includes('/api/auth/')) {
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
          if (error.status === 401) {
            this.authService.logout();
          }
          return throwError(() => error);
        })
      );
    }

    return next.handle(req);
  }
}