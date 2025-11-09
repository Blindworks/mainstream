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
  selectedImage: File | null = null;
  routeName: string = '';
  routeDescription: string = '';
  uploading: boolean = false;
  uploadSuccess: boolean = false;
  uploadError: string | null = null;
  uploadedRoute: PredefinedRoute | null = null;
  imagePreview: string | null = null;

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

  onImageSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      if (validTypes.includes(file.type)) {
        this.selectedImage = file;
        this.uploadError = null;

        // Create preview
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.imagePreview = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        this.uploadError = 'Bitte wähle eine gültige Bilddatei aus (JPEG, PNG, GIF, WebP)';
        this.selectedImage = null;
        this.imagePreview = null;
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
        // If image is selected, upload it after route is created
        if (this.selectedImage) {
          this.adminService.uploadRouteImage(route.id, this.selectedImage).subscribe({
            next: (updatedRoute) => {
              this.uploading = false;
              this.uploadSuccess = true;
              this.uploadedRoute = updatedRoute;
              this.resetForm();
            },
            error: (error) => {
              this.uploading = false;
              // Route was created but image upload failed
              this.uploadSuccess = true;
              this.uploadedRoute = route;
              this.uploadError = 'Route wurde erstellt, aber Bild-Upload fehlgeschlagen: ' +
                                 (error.error?.message || error.error || 'Unbekannter Fehler');
              console.error('Image upload error:', error);
            }
          });
        } else {
          this.uploading = false;
          this.uploadSuccess = true;
          this.uploadedRoute = route;
          this.resetForm();
        }
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
    this.selectedImage = null;
    this.imagePreview = null;
    this.routeName = '';
    this.routeDescription = '';
    // Reset file inputs
    const fileInputs = document.querySelectorAll('input[type="file"]') as NodeListOf<HTMLInputElement>;
    fileInputs.forEach(input => {
      input.value = '';
    });
  }

  dismissSuccess(): void {
    this.uploadSuccess = false;
    this.uploadedRoute = null;
  }
}
