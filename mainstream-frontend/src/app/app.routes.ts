import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';
import { maintenanceGuard } from './core/guards/maintenance.guard';

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
    canActivate: [AuthGuard, maintenanceGuard]
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
        path: 'forgot-password',
        loadComponent: () => import('./features/users/components/password-reset/password-reset.component')
          .then(m => m.PasswordResetComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./features/users/components/password-reset/password-reset.component')
          .then(m => m.PasswordResetComponent)
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
    canActivate: [AuthGuard, maintenanceGuard]
  },
  {
    path: 'routes',
    loadComponent: () => import('./pages/routes/routes.component')
      .then(m => m.RoutesComponent),
    canActivate: [AuthGuard, maintenanceGuard]
  },
  {
    path: 'trophies',
    loadComponent: () => import('./pages/trophies/trophies.component')
      .then(m => m.TrophiesComponent),
    canActivate: [AuthGuard, maintenanceGuard]
  },
  {
    path: 'runs',
    loadComponent: () => import('./pages/runs/runs.component')
      .then(m => m.RunsComponent),
    canActivate: [AuthGuard, maintenanceGuard]
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/users/components/user-profile/user-profile.component')
      .then(m => m.UserProfileComponent),
    canActivate: [AuthGuard, maintenanceGuard]
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
        path: 'privacy-policy',
        loadComponent: () => import('./pages/privacy-policy/privacy-policy.component').then(m => m.PrivacyPolicyComponent)
    },
    {
    path: 'impressum',
    loadComponent: () => import('./pages/impressum/impressum.component').then(m => m.ImpressumComponent)
  },
  {
    path: 'faq',
    loadComponent: () => import('./pages/faq/faq.component').then(m => m.FaqComponent)
  },
  {
    path: 'strava/callback',
    loadComponent: () => import('./pages/strava/strava-callback.component')
      .then(m => m.StravaCallbackComponent)
  },
  {
    path: 'garmin/callback',
    loadComponent: () => import('./pages/garmin/garmin-callback.component')
      .then(m => m.GarminCallbackComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/components/admin-dashboard/admin-dashboard.component')
      .then(m => m.AdminDashboardComponent),
    canActivate: [AdminGuard]
  },
  {
    path: 'maintenance',
    loadComponent: () => import('./pages/maintenance/maintenance.component')
      .then(m => m.MaintenanceComponent)
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
