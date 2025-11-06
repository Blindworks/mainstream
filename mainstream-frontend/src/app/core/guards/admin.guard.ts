import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

import { AuthService } from '../../features/users/services/auth.service';

export const AdminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // First check if user is authenticated
  if (!authService.isAuthenticated || authService.isTokenExpired()) {
    router.navigate(['/auth/login']);
    return false;
  }

  // Check if user has ADMIN role
  const currentUser = authService.currentUser;
  if (currentUser && currentUser.role === 'ADMIN') {
    return true;
  }

  // User is authenticated but not an admin
  router.navigate(['/home']);
  return false;
};
