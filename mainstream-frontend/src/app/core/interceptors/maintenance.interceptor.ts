import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const maintenanceInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Check if it's a 503 Service Unavailable error (maintenance mode)
      if (error.status === 503 && !router.url.includes('/maintenance')) {
        // Redirect to maintenance page
        router.navigate(['/maintenance']);
      }

      return throwError(() => error);
    })
  );
};
