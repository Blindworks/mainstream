import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AdminService, PredefinedRoute } from '../../services/admin.service';
import { RouteMapComponent } from '../route-map/route-map.component';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-route-list',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './route-list.component.html',
  styleUrls: ['./route-list.component.css']
})
export class RouteListComponent implements OnInit {
  routes: PredefinedRoute[] = [];
  loading: boolean = false;
  error: string | null = null;
  showActiveOnly: boolean = false;

  constructor(
    private adminService: AdminService,
    private dialog: MatDialog
  ) {}

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

  showRouteOnMap(route: PredefinedRoute): void {
    this.dialog.open(RouteMapComponent, {
      width: '90vw',
      maxWidth: '1000px',
      height: 'auto',
      maxHeight: '90vh',
      data: {
        routeId: route.id,
        routeName: route.name
      }
    });
  }

  onImageUpload(event: any, route: PredefinedRoute): void {
    const file = event.target.files[0];
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      this.error = 'Bitte wähle eine gültige Bilddatei (JPEG, PNG, GIF, WebP)';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.error = 'Die Bilddatei ist zu groß (max. 5MB)';
      return;
    }

    this.adminService.uploadRouteImage(route.id, file).subscribe({
      next: (updatedRoute) => {
        // Update route in list
        const index = this.routes.findIndex(r => r.id === updatedRoute.id);
        if (index !== -1) {
          this.routes[index] = updatedRoute;
        }
        this.error = null;
      },
      error: (error) => {
        this.error = 'Fehler beim Hochladen des Bildes: ' + (error.error?.message || error.error || 'Unbekannter Fehler');
        console.error('Image upload error:', error);
      }
    });

    // Reset file input
    event.target.value = '';
  }

  deleteRouteImage(route: PredefinedRoute): void {
    if (!confirm('Möchtest du das Bild dieser Route wirklich löschen?')) {
      return;
    }

    this.adminService.deleteRouteImage(route.id).subscribe({
      next: (updatedRoute) => {
        // Update route in list
        const index = this.routes.findIndex(r => r.id === updatedRoute.id);
        if (index !== -1) {
          this.routes[index] = updatedRoute;
        }
        this.error = null;
      },
      error: (error) => {
        this.error = 'Fehler beim Löschen des Bildes: ' + (error.error?.message || error.error || 'Unbekannter Fehler');
        console.error('Image delete error:', error);
      }
    });
  }

  getRouteImageUrl(route: PredefinedRoute): string {
    if (route.imageUrl) {
      return `${environment.apiUrl}${route.imageUrl}`;
    }
    return '';
  }
}
