import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { Subject, takeUntil } from 'rxjs';
import { FitUploadService } from '../../services/fit-upload.service';

export interface FitUploadDialogData {
  // Any initial data if needed
}

export interface FitUploadResult {
  success: boolean;
  fileName?: string;
  fileId?: number;
}

@Component({
  selector: 'app-fit-upload-dialog',
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatCardModule,
    MatDividerModule
  ],
  templateUrl: './fit-upload-dialog.component.html',
  styleUrl: './fit-upload-dialog.component.scss'
})
export class FitUploadDialogComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  selectedFile: File | null = null;
  isUploading = false;
  uploadProgress = 0;
  isDragOver = false;
  uploadError: string | null = null;
  uploadSuccess = false;
  uploadResult: any = null;

  constructor(
    public dialogRef: MatDialogRef<FitUploadDialogComponent, FitUploadResult>,
    @Inject(MAT_DIALOG_DATA) public data: FitUploadDialogData,
    private fitUploadService: FitUploadService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFileSelection(input.files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFileSelection(files[0]);
    }
  }

  private handleFileSelection(file: File): void {
    // Reset previous state
    this.uploadError = null;
    this.uploadSuccess = false;
    this.uploadResult = null;

    // Validate file type
    if (!this.isValidFitFile(file)) {
      this.uploadError = 'Bitte wählen Sie eine gültige .fit Datei aus.';
      return;
    }

    // Validate file size (max 50MB)
    const maxSizeBytes = 50 * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      this.uploadError = 'Die Datei ist zu groß. Maximale Größe: 50MB.';
      return;
    }

    this.selectedFile = file;
  }

  private isValidFitFile(file: File): boolean {
    // Check file extension
    const fileName = file.name.toLowerCase();
    if (!fileName.endsWith('.fit')) {
      return false;
    }

    // Check MIME type (browsers might not set this correctly for .fit files)
    const validMimeTypes = [
      'application/octet-stream',
      'application/fit',
      'application/x-fit'
    ];

    return file.type === '' || validMimeTypes.includes(file.type.toLowerCase());
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;
    this.uploadError = null;

    this.fitUploadService.uploadFitFile(this.selectedFile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (event) => {
          if (event.type === 'progress') {
            this.uploadProgress = event.progress;
          } else if (event.type === 'success') {
            this.uploadSuccess = true;
            this.uploadResult = event.result;
            this.isUploading = false;
            
            this.snackBar.open(
              'FIT-Datei erfolgreich hochgeladen und verarbeitet!', 
              'Schließen', 
              { duration: 5000, panelClass: ['success-snackbar'] }
            );

            // Auto-close dialog after success
            setTimeout(() => {
              this.closeDialog(true);
            }, 2000);
          }
        },
        error: (error) => {
          console.error('Upload error:', error);
          this.isUploading = false;
          this.uploadProgress = 0;
          
          // Handle different error types
          if (error.status === 400) {
            this.uploadError = 'Ungültige FIT-Datei oder bereits vorhanden.';
          } else if (error.status === 413) {
            this.uploadError = 'Datei zu groß.';
          } else if (error.status === 422) {
            this.uploadError = 'FIT-Datei konnte nicht verarbeitet werden.';
          } else {
            this.uploadError = 'Upload fehlgeschlagen. Bitte versuchen Sie es erneut.';
          }

          this.snackBar.open(
            this.uploadError, 
            'Schließen', 
            { duration: 5000, panelClass: ['error-snackbar'] }
          );
        }
      });
  }

  removeFile(): void {
    this.selectedFile = null;
    this.uploadError = null;
    this.uploadSuccess = false;
    this.uploadResult = null;
  }

  closeDialog(success: boolean = false): void {
    const result: FitUploadResult = {
      success: success,
      fileName: this.selectedFile?.name,
      fileId: this.uploadResult?.id
    };
    
    this.dialogRef.close(result);
  }

  // Utility methods
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  getFileIcon(): string {
    return 'insert_drive_file';
  }

  formatDuration(seconds: number): string {
    if (!seconds) return '00:00:00';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
}