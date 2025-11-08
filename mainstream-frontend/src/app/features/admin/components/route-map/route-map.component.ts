import { Component, Inject, OnInit, AfterViewInit, OnDestroy, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import * as L from 'leaflet';
import { AdminService, PredefinedRoute, RouteTrackPoint } from '../../services/admin.service';

export interface RouteMapDialogData {
  routeId?: number;
  routeName: string;
  route?: any; // Accept both AdminService.PredefinedRoute and routes/models PredefinedRoute types
}

@Component({
  selector: 'app-route-map',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './route-map.component.html',
  styleUrls: ['./route-map.component.css']
})
export class RouteMapComponent implements OnInit, AfterViewInit, OnDestroy {
  private map?: L.Map;
  route?: any; // Can be either AdminService.PredefinedRoute or routes/models PredefinedRoute
  loading = true;
  error?: string;

  constructor(
    public dialogRef: MatDialogRef<RouteMapComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RouteMapDialogData,
    @Optional() private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.loadRoute();
  }

  ngAfterViewInit(): void {
    // Map initialization will happen after route is loaded
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  loadRoute(): void {
    this.loading = true;
    this.error = undefined;

    // If route data is already provided, use it directly
    if (this.data.route) {
      this.route = this.data.route;
      this.loading = false;

      // Initialize map after a short delay to ensure DOM is ready
      setTimeout(() => this.initializeMap(), 100);
      return;
    }

    // Otherwise, load from server using routeId
    if (!this.data.routeId) {
      this.error = 'Keine Route-ID angegeben';
      this.loading = false;
      return;
    }

    if (!this.adminService) {
      this.error = 'Service nicht verfügbar';
      this.loading = false;
      return;
    }

    this.adminService.getRouteById(this.data.routeId).subscribe({
      next: (route) => {
        this.route = route;
        this.loading = false;

        // Initialize map after a short delay to ensure DOM is ready
        setTimeout(() => this.initializeMap(), 100);
      },
      error: (err) => {
        console.error('Error loading route:', err);
        this.error = 'Fehler beim Laden der Route';
        this.loading = false;
      }
    });
  }

  private initializeMap(): void {
    if (!this.route?.trackPoints || this.route.trackPoints.length === 0) {
      this.error = 'Keine Trackpunkte verfügbar';
      return;
    }

    // Initialize the map
    this.map = L.map('route-map', {
      center: [this.route.startLatitude, this.route.startLongitude],
      zoom: 13
    });

    // Add OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Create path from track points
    const coordinates: L.LatLngExpression[] = this.route.trackPoints
      .sort((a: RouteTrackPoint, b: RouteTrackPoint) => a.sequenceNumber - b.sequenceNumber)
      .map((point: RouteTrackPoint) => [point.latitude, point.longitude] as L.LatLngExpression);

    // Draw the route as a polyline
    const polyline = L.polyline(coordinates, {
      color: '#2196F3',
      weight: 4,
      opacity: 0.8
    }).addTo(this.map);

    // Add start marker
    const startPoint = this.route.trackPoints[0];
    L.marker([startPoint.latitude, startPoint.longitude], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(this.map).bindPopup('Start');

    // Add end marker
    const endPoint = this.route.trackPoints[this.route.trackPoints.length - 1];
    L.marker([endPoint.latitude, endPoint.longitude], {
      icon: L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    }).addTo(this.map).bindPopup('Ziel');

    // Fit map bounds to show entire route
    this.map.fitBounds(polyline.getBounds(), {
      padding: [50, 50]
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
