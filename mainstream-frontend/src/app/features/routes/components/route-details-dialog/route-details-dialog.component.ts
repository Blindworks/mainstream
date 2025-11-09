import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import {
  PredefinedRoute,
  formatDistance,
  formatElevation,
  getRouteStatusLabel,
  getRouteStatusColor
} from '../../models/predefined-route.model';
import { PredefinedRouteService } from '../../services/predefined-route.service';
import { RouteMapComponent, RouteMapDialogData } from '../../../../features/admin/components/route-map/route-map.component';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-route-details-dialog',
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './route-details-dialog.component.html',
  styleUrl: './route-details-dialog.component.scss'
})
export class RouteDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<RouteDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public route: PredefinedRoute,
    private dialog: MatDialog,
    private routeService: PredefinedRouteService
  ) {}

  formatDistance(meters: number): string {
    return formatDistance(meters);
  }

  formatElevation(meters: number | undefined): string {
    return formatElevation(meters);
  }

  getStatusLabel(isActive: boolean): string {
    return getRouteStatusLabel(isActive);
  }

  getStatusColor(isActive: boolean): string {
    return getRouteStatusColor(isActive);
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  }

  hasElevationData(): boolean {
    return this.route.elevationGainMeters !== undefined &&
           this.route.elevationGainMeters !== null &&
           this.route.elevationGainMeters > 0;
  }

  onClose(): void {
    this.dialogRef.close();
  }

  onViewMap(): void {
    // Load the full route data with trackpoints
    this.routeService.getRouteById(this.route.id).subscribe({
      next: (routeWithTrackpoints) => {
        // Open the map dialog with the full route data
        this.dialog.open(RouteMapComponent, {
          width: '90vw',
          maxWidth: '1000px',
          height: 'auto',
          maxHeight: '90vh',
          data: {
            routeName: routeWithTrackpoints.name,
            route: routeWithTrackpoints
          }
        });
      },
      error: (error) => {
        console.error('Error loading route for map:', error);
        // Could show an error message here
      }
    });
  }

  getRouteImageUrl(): string {
    if (this.route.imageUrl) {
      return `${environment.apiUrl}${this.route.imageUrl}`;
    }
    return 'assets/images/map_rohling.png';
  }
}
