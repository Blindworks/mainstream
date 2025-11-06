import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'landing',
    loadComponent: () => import('./pages/landing-page/landing-page.component')
      .then(m => m.LandingPageComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/users/components/user-login/user-login.component')
          .then(m => m.UserLoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/users/components/user-registration/user-registration.component')
          .then(m => m.UserRegistrationComponent)
      },
      {
        path: '',
        redirectTo: '/auth/login',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'competitions',
    loadComponent: () => import('./pages/competitions/competitions.component')
      .then(m => m.CompetitionsComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'trophies',
    loadComponent: () => import('./pages/trophies/trophies.component')
      .then(m => m.TrophiesComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'runs',
    loadComponent: () => import('./pages/runs/runs.component')
      .then(m => m.RunsComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/users/components/user-profile/user-profile.component')
      .then(m => m.UserProfileComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'subscriptions',
    loadComponent: () => import('./pages/subscriptions/subscriptions.component')
      .then(m => m.SubscriptionsComponent)
  },
  {
    path: 'about',
    loadComponent: () => import('./pages/about/about.component').then(m => m.AboutComponent)
  },
  {
    path: 'strava/callback',
    loadComponent: () => import('./pages/strava/strava-callback.component')
      .then(m => m.StravaCallbackComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/components/admin-dashboard/admin-dashboard.component')
      .then(m => m.AdminDashboardComponent),
    canActivate: [AdminGuard]
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
