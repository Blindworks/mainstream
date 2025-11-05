import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

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
    loadComponent: () => import('./features/competitions/components/competition-list/competition-list.component')
      .then(m => m.CompetitionListComponent),
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
    path: 'about',
    loadComponent: () => import('./pages/about/about.component').then(m => m.AboutComponent)
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
