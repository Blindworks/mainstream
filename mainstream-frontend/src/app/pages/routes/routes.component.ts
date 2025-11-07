import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouteListComponent } from '../../features/routes/components/route-list/route-list.component';

@Component({
  selector: 'app-routes',
  imports: [
    CommonModule,
    RouteListComponent
  ],
  templateUrl: './routes.component.html',
  styleUrl: './routes.component.scss'
})
export class RoutesComponent {
  selectedRouteId: number | null = null;

  onRouteSelected(routeId: number): void {
    this.selectedRouteId = routeId;
    // TODO: Navigate to route details or show in side panel
    console.log('Route selected:', routeId);
  }
}
