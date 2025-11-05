import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, PredefinedRoute } from '../../services/admin.service';

@Component({
  selector: 'app-route-upload',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './route-upload.component.html',
  styleUrls: ['./route-upload.component.css']
})
export class RouteUploadComponent {
  selectedFile: File | null = null;
  routeName: string = '';
  routeDescription: string = '';
  uploading: boolean = false;
  uploadSuccess: boolean = false;
  uploadError: string | null = null;
  uploadedRoute: PredefinedRoute | null = null;

  constructor(private adminService: AdminService) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.name.toLowerCase().endsWith('.gpx')) {
        this.selectedFile = file;
        this.uploadError = null;

        // Auto-fill route name from filename if empty
        if (!this.routeName) {
          this.routeName = file.name.replace('.gpx', '').replace(/[_-]/g, ' ');
        }
      } else {
        this.uploadError = 'Bitte wähle eine GPX-Datei aus';
        this.selectedFile = null;
      }
    }
  }

  onUpload(): void {
    if (!this.selectedFile || !this.routeName.trim()) {
      this.uploadError = 'Bitte wähle eine Datei und gib einen Namen ein';
      return;
    }

    this.uploading = true;
    this.uploadError = null;
    this.uploadSuccess = false;

    this.adminService.uploadGpxRoute(
      this.selectedFile,
      this.routeName.trim(),
      this.routeDescription.trim()
    ).subscribe({
      next: (route) => {
        this.uploading = false;
        this.uploadSuccess = true;
        this.uploadedRoute = route;
        this.resetForm();
      },
      error: (error) => {
        this.uploading = false;
        this.uploadError = error.error?.message || error.error || 'Fehler beim Hochladen der Route';
        console.error('Upload error:', error);
      }
    });
  }

  resetForm(): void {
    this.selectedFile = null;
    this.routeName = '';
    this.routeDescription = '';
    // Reset file input
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  dismissSuccess(): void {
    this.uploadSuccess = false;
    this.uploadedRoute = null;
  }
}
