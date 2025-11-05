import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, PredefinedRoute } from '../../services/admin.service';

@Component({
  selector: 'app-route-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './route-list.component.html',
  styleUrls: ['./route-list.component.css']
})
export class RouteListComponent implements OnInit {
  routes: PredefinedRoute[] = [];
  loading: boolean = false;
  error: string | null = null;
  showActiveOnly: boolean = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadRoutes();
  }

  loadRoutes(): void {
    this.loading = true;
    this.error = null;

    this.adminService.getAllRoutes(this.showActiveOnly).subscribe({
      next: (routes) => {
        this.routes = routes;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Fehler beim Laden der Routen';
        this.loading = false;
        console.error('Load routes error:', error);
      }
    });
  }

  toggleRouteStatus(route: PredefinedRoute): void {
    const action = route.isActive
      ? this.adminService.deactivateRoute(route.id)
      : this.adminService.activateRoute(route.id);

    action.subscribe({
      next: (updatedRoute) => {
        // Update route in list
        const index = this.routes.findIndex(r => r.id === updatedRoute.id);
        if (index !== -1) {
          this.routes[index] = updatedRoute;
        }
      },
      error: (error) => {
        this.error = 'Fehler beim Ändern des Route-Status';
        console.error('Toggle status error:', error);
      }
    });
  }

  toggleFilter(): void {
    this.showActiveOnly = !this.showActiveOnly;
    this.loadRoutes();
  }

  getStatusBadgeClass(route: PredefinedRoute): string {
    return route.isActive ? 'badge-active' : 'badge-inactive';
  }

  getStatusText(route: PredefinedRoute): string {
    return route.isActive ? 'Aktiv' : 'Inaktiv';
  }

  formatDistance(meters: number): string {
    return (meters / 1000).toFixed(2) + ' km';
  }

  formatElevation(meters: number): string {
    return meters ? Math.round(meters) + ' m' : '0 m';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
