import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { RouteListComponent } from '../route-list/route-list.component';
import { RouteUploadComponent } from '../route-upload/route-upload.component';
import { TrophyManagementComponent } from '../trophy-management/trophy-management.component';
import { AdminSettingsComponent } from '../admin-settings/admin-settings.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    RouteListComponent,
    RouteUploadComponent,
    TrophyManagementComponent,
    AdminSettingsComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent {
  activeTab: 'routes' | 'upload' | 'trophies' | 'settings' = 'routes';

  setActiveTab(tab: 'routes' | 'upload' | 'trophies' | 'settings'): void {
    this.activeTab = tab;
  }
}
