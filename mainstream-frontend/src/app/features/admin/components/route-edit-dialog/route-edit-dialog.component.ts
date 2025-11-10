import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AdminService, PredefinedRoute } from '../../services/admin.service';

export interface RouteEditDialogData {
  route: PredefinedRoute;
}

@Component({
  selector: 'app-route-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './route-edit-dialog.component.html',
  styleUrls: ['./route-edit-dialog.component.css']
})
export class RouteEditDialogComponent {
  routeName: string;
  routeDescription: string;
  routeCity: string;
  saving: boolean = false;
  error: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<RouteEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RouteEditDialogData,
    private adminService: AdminService
  ) {
    this.routeName = data.route.name;
    this.routeDescription = data.route.description || '';
    this.routeCity = data.route.city || '';
  }

  onSave(): void {
    if (!this.routeName.trim()) {
      this.error = 'Route name is required';
      return;
    }

    this.saving = true;
    this.error = null;

    this.adminService.updateRoute(
      this.data.route.id,
      this.routeName.trim(),
      this.routeDescription.trim(),
      this.routeCity.trim()
    ).subscribe({
      next: (updatedRoute) => {
        this.saving = false;
        this.dialogRef.close(updatedRoute);
      },
      error: (err) => {
        this.saving = false;
        this.error = err.error?.message || err.error || 'Failed to update route';
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
