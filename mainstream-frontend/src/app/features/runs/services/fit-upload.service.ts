import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { ApiService } from '../../../shared/services/api.service';

export interface FitUploadProgress {
  type: 'progress';
  progress: number;
}

export interface FitUploadSuccess {
  type: 'success';
  result: FitUploadResult;
}

export interface FitUploadResult {
  id: number;
  originalFilename: string;
  fileSize: number;
  processingStatus: string;
  activityStartTime: string;
  totalDistance?: number;
  totalTimerTime?: number;
  totalCalories?: number;
  avgSpeed?: number;
  maxHeartRate?: number;
  avgHeartRate?: number;
  message: string;
}

export type FitUploadEvent = FitUploadProgress | FitUploadSuccess;

@Injectable({
  providedIn: 'root'
})
export class FitUploadService {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/fit-files`;
  }

  uploadFitFile(file: File): Observable<FitUploadEvent> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<FitUploadResult>(`${this.baseUrl}/upload`, formData, {
      reportProgress: true,
      observe: 'events'
    }).pipe(
      map((event: HttpEvent<FitUploadResult>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            const progress = event.total 
              ? Math.round(100 * event.loaded / event.total) 
              : 0;
            return { type: 'progress', progress } as FitUploadProgress;
            
          case HttpEventType.Response:
            return { type: 'success', result: event.body! } as FitUploadSuccess;
            
          default:
            return { type: 'progress', progress: 0 } as FitUploadProgress;
        }
      }),
      catchError((error) => {
        console.error('FIT file upload error:', error);
        return throwError(() => error);
      })
    );
  }

  getFitFileUploads(page: number = 0, size: number = 20): Observable<{content: FitUploadResult[], totalElements: number}> {
    return this.http.get<{content: FitUploadResult[], totalElements: number}>(
      `${this.baseUrl}?page=${page}&size=${size}`
    ).pipe(
      catchError((error) => {
        console.error('Error fetching FIT file uploads:', error);
        return throwError(() => error);
      })
    );
  }

  getFitFileById(id: number): Observable<FitUploadResult> {
    return this.http.get<FitUploadResult>(`${this.baseUrl}/${id}`).pipe(
      catchError((error) => {
        console.error('Error fetching FIT file:', error);
        return throwError(() => error);
      })
    );
  }

  deleteFitFile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError((error) => {
        console.error('Error deleting FIT file:', error);
        return throwError(() => error);
      })
    );
  }

  getProcessingStatus(id: number): Observable<{status: string, message?: string}> {
    return this.http.get<{status: string, message?: string}>(
      `${this.baseUrl}/${id}/status`
    ).pipe(
      catchError((error) => {
        console.error('Error fetching processing status:', error);
        return throwError(() => error);
      })
    );
  }

  // Convert FIT upload result to Run format for compatibility
  convertToRun(fitResult: FitUploadResult): any {
    return {
      id: fitResult.id,
      title: this.generateRunTitle(fitResult),
      startTime: new Date(fitResult.activityStartTime),
      durationSeconds: fitResult.totalTimerTime,
      distanceMeters: fitResult.totalDistance,
      totalCalories: fitResult.totalCalories,
      avgSpeed: fitResult.avgSpeed,
      maxHeartRate: fitResult.maxHeartRate,
      avgHeartRate: fitResult.avgHeartRate,
      runType: 'OUTDOOR',
      status: this.mapProcessingStatusToRunStatus(fitResult.processingStatus),
      isPublic: true
    };
  }

  private generateRunTitle(fitResult: FitUploadResult): string {
    const date = new Date(fitResult.activityStartTime);
    const dateStr = date.toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
    
    if (fitResult.totalDistance && fitResult.totalDistance > 0) {
      const distanceKm = (fitResult.totalDistance / 1000).toFixed(1);
      return `Lauf ${distanceKm} km - ${dateStr}`;
    }
    
    return `Lauf - ${dateStr}`;
  }

  private mapProcessingStatusToRunStatus(processingStatus: string): string {
    switch (processingStatus?.toUpperCase()) {
      case 'COMPLETED':
        return 'COMPLETED';
      case 'PROCESSING':
        return 'ACTIVE';
      case 'PENDING':
        return 'DRAFT';
      case 'FAILED':
        return 'CANCELLED';
      default:
        return 'DRAFT';
    }
  }

  // Utility methods
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  formatDuration(seconds: number): string {
    if (!seconds) return '00:00:00';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  isValidFitFile(file: File): boolean {
    // Check file extension
    const fileName = file.name.toLowerCase();
    if (!fileName.endsWith('.fit')) {
      return false;
    }

    // Check file size (max 50MB)
    const maxSizeBytes = 50 * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      return false;
    }

    return true;
  }
}