import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { PredefinedRouteService } from '../../../features/routes/services/predefined-route.service';
import { PredefinedRoute } from '../../../features/routes/models/predefined-route.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-top-routes-today',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './top-routes-today.component.html',
  styleUrls: ['./top-routes-today.component.scss']
})
export class TopRoutesTodayComponent implements OnInit {
  topRoutes: PredefinedRoute[] = [];
  isLoading = true;

  constructor(
    private routeService: PredefinedRouteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTopRoutes();
  }

  private loadTopRoutes(): void {
    this.isLoading = true;
    // Get all active routes with stats, then filter and sort on frontend
    this.routeService.getAllRoutesWithStats(true).subscribe({
      next: (routes) => {
        // Sort by todayCount (descending) and take top 3
        this.topRoutes = routes
          .filter(route => route.stats && route.stats.todayCount > 0)
          .sort((a, b) => (b.stats?.todayCount || 0) - (a.stats?.todayCount || 0))
          .slice(0, 3);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading top routes:', error);
        this.isLoading = false;
      }
    });
  }

  getRouteImageUrl(route: PredefinedRoute): string {
    if (route.imageUrl) {
      return `${environment.apiUrl}${route.imageUrl}`;
    }
    return 'assets/images/map_rohling.png';
  }

  formatDistance(meters: number): string {
    const km = meters / 1000;
    return `${km.toFixed(2)} km`;
  }

  navigateToRoutes(): void {
    this.router.navigate(['/routes']);
  }

  onShowDetails(route: PredefinedRoute, event: Event): void {
    event.stopPropagation();
    // Navigate to routes page and show details
    this.router.navigate(['/routes'], {
      queryParams: { routeId: route.id }
    });
  }
}
