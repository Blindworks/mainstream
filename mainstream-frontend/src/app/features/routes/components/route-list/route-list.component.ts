import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';
import {
  PredefinedRoute,
  formatDistance,
  formatElevation,
  getRouteStatusLabel,
  getRouteStatusColor
} from '../../models/predefined-route.model';
import { PredefinedRouteService } from '../../services/predefined-route.service';
import { RouteDetailsDialogComponent } from '../route-details-dialog/route-details-dialog.component';

@Component({
  selector: 'app-route-list',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTabsModule,
    MatDialogModule
  ],
  templateUrl: './route-list.component.html',
  styleUrl: './route-list.component.scss'
})
export class RouteListComponent implements OnInit {
  @Output() routeSelected = new EventEmitter<number>();
  @Input() selectedRouteId: number | null = null;

  routes: PredefinedRoute[] = [];
  isLoading = false;
  currentTab = 0;

  constructor(
    public routeService: PredefinedRouteService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadRoutes();
  }

  loadRoutes(): void {
    this.isLoading = true;

    // Tab 0: Active Routes, Tab 1: All Routes
    const activeOnly = this.currentTab === 0;

    this.routeService.getAllRoutesWithStats(activeOnly).subscribe({
      next: (routes) => {
        this.routes = routes;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading routes:', error);
        this.isLoading = false;
      }
    });
  }

  onTabChange(index: number): void {
    this.currentTab = index;
    this.loadRoutes();
  }

  onRouteClick(route: PredefinedRoute): void {
    this.routeSelected.emit(route.id);
  }

  onShowDetails(route: PredefinedRoute, event: Event): void {
    event.stopPropagation();
    this.dialog.open(RouteDetailsDialogComponent, {
      width: '600px',
      data: route
    });
  }

  getStatusColor(isActive: boolean): string {
    return getRouteStatusColor(isActive);
  }

  getStatusLabel(isActive: boolean): string {
    return getRouteStatusLabel(isActive);
  }

  formatDistance(meters: number): string {
    return formatDistance(meters);
  }

  formatElevation(meters: number | undefined): string {
    return formatElevation(meters);
  }

  formatDate(date: Date): string {
    return this.routeService.formatDate(date);
  }

  isSelected(routeId: number): boolean {
    return this.selectedRouteId === routeId;
  }

  getRouteIcon(): string {
    return 'route';
  }

  hasElevationData(route: PredefinedRoute): boolean {
    return route.elevationGainMeters !== undefined &&
           route.elevationGainMeters !== null &&
           route.elevationGainMeters > 0;
  }
}
