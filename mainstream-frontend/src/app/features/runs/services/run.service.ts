import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { Run, RunSummary } from '../models/run.model';
import { Lap } from '../models/lap.model';
import { ApiService } from '../../../shared/services/api.service';

@Injectable({
  providedIn: 'root'
})
export class RunService {
  private baseUrl: string;
  private selectedRunSubject = new BehaviorSubject<Run | null>(null);
  public selectedRun$ = this.selectedRunSubject.asObservable();

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/runs`;
  }

  getAllRuns(page: number = 0, size: number = 20): Observable<{content: RunSummary[], totalElements: number}> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${this.baseUrl}`, { params }).pipe(
      map(response => ({
        content: response.content.map((run: any) => this.mapToRunSummary(run)),
        totalElements: response.totalElements
      }))
    );
  }

  getRunById(id: number): Observable<Run> {
    return this.http.get<any>(`${this.baseUrl}/${id}`).pipe(
      map(run => this.mapToRun(run))
    );
  }

  getUserRuns(userId: number, page: number = 0, size: number = 20): Observable<{content: RunSummary[], totalElements: number}> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${this.baseUrl}/user/${userId}`, { params }).pipe(
      map(response => ({
        content: response.content.map((run: any) => this.mapToRunSummary(run)),
        totalElements: response.totalElements
      }))
    );
  }

  selectRun(run: Run | null): void {
    this.selectedRunSubject.next(run);
  }

  getSelectedRun(): Run | null {
    return this.selectedRunSubject.value;
  }

  private mapToRun(data: any): Run {
    return {
      ...data,
      startTime: new Date(data.startTime),
      endTime: data.endTime ? new Date(data.endTime) : undefined,
      createdAt: new Date(data.createdAt),
      updatedAt: new Date(data.updatedAt)
    };
  }

  private mapToRunSummary(data: any): RunSummary {
    return {
      id: data.id,
      title: data.title,
      startTime: new Date(data.startTime),
      durationSeconds: data.durationSeconds,
      distanceKm: data.distanceMeters ? data.distanceMeters / 1000 : undefined,
      averagePace: this.formatPace(data.averagePaceSecondsPerKm),
      runType: data.runType,
      status: data.status,
      caloriesBurned: data.caloriesBurned
    };
  }

  private formatPace(paceSeconds: number | null | undefined): string {
    if (!paceSeconds || paceSeconds <= 0) return '--:--';
    
    const minutes = Math.floor(paceSeconds / 60);
    const seconds = Math.floor(paceSeconds % 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  formatDuration(durationSeconds: number | null | undefined): string {
    if (!durationSeconds) return '00:00:00';
    
    const hours = Math.floor(durationSeconds / 3600);
    const minutes = Math.floor((durationSeconds % 3600) / 60);
    const seconds = durationSeconds % 60;
    
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  formatDistance(distanceKm: number | null | undefined): string {
    if (!distanceKm) return '0.00 km';
    return `${distanceKm.toFixed(2)} km`;
  }

  getRunLaps(runId: number): Observable<Lap[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${runId}/laps`).pipe(
      map(laps => laps.map(lap => this.mapToLap(lap)))
    );
  }

  private mapToLap(data: any): Lap {
    return {
      ...data,
      startTime: new Date(data.startTime),
      endTime: data.endTime ? new Date(data.endTime) : undefined
    };
  }

  deleteRun(runId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${runId}`);
  }
}