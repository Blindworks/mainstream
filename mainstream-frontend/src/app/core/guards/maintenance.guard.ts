import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, catchError, of } from 'rxjs';
import { SettingsService } from '../../features/admin/services/settings.service';
import { AuthService } from '../../features/users/services/auth.service';

/**
 * Guard that checks if maintenance mode is active and redirects non-admin users
 * to the maintenance page.
 */
export const maintenanceGuard: CanActivateFn = () => {
  const settingsService = inject(SettingsService);
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if user is admin
  const isAdmin = authService.currentUser?.role === 'ADMIN';

  // Admins can always access
  if (isAdmin) {
    return true;
  }

  // Check maintenance mode status for non-admin users
  return settingsService.getMaintenanceModeStatus().pipe(
    map(status => {
      if (status.enabled) {
        // Maintenance mode is active, redirect to maintenance page
        router.navigate(['/maintenance']);
        return false;
      }
      // Maintenance mode is not active, allow access
      return true;
    }),
    catchError(error => {
      console.error('Error checking maintenance mode:', error);
      // If we can't check maintenance mode, allow access to avoid blocking users
      return of(true);
    })
  );
};
