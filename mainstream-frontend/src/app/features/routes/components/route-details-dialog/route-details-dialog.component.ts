import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
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
    @Inject(MAT_DIALOG_DATA) public route: PredefinedRoute
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
    // TODO: Implement map view
    console.log('View map for route:', this.route.id);
    this.dialogRef.close();
  }
}
