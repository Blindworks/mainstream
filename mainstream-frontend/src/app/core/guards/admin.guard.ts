import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';

import { AuthService } from '../../features/users/services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // First check if user is authenticated
    if (!this.authService.isAuthenticated || this.authService.isTokenExpired()) {
      this.router.navigate(['/auth/login']);
      return false;
    }

    // Check if user has ADMIN role
    const currentUser = this.authService.currentUser;
    if (currentUser && currentUser.role === 'ADMIN') {
      return true;
    }

    // User is authenticated but not an admin
    this.router.navigate(['/home']);
    return false;
  }
}
