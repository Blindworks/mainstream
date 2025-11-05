import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { RouteListComponent } from '../route-list/route-list.component';
import { RouteUploadComponent } from '../route-upload/route-upload.component';
import { TrophyManagementComponent } from '../trophy-management/trophy-management.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    RouteListComponent,
    RouteUploadComponent,
    TrophyManagementComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent {
  activeTab: 'routes' | 'upload' | 'trophies' = 'routes';

  setActiveTab(tab: 'routes' | 'upload' | 'trophies'): void {
    this.activeTab = tab;
  }
}
