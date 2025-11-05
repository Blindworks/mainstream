import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Run, RunStatus, RunType } from '../../models/run.model';
import { RunService } from '../../services/run.service';

@Component({
  selector: 'app-run-details',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatButtonModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './run-details.component.html',
  styleUrl: './run-details.component.scss'
})
export class RunDetailsComponent implements OnChanges {
  @Input() runId: number | null = null;

  run: Run | null = null;
  isLoading = false;
  error: string | null = null;

  constructor(private runService: RunService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['runId'] && this.runId) {
      this.loadRunDetails();
    } else if (changes['runId'] && !this.runId) {
      this.run = null;
    }
  }

  private loadRunDetails(): void {
    if (!this.runId) return;

    this.isLoading = true;
    this.error = null;

    this.runService.getRunById(this.runId).subscribe({
      next: (run) => {
        console.log('DEBUG: Run data received:', run);
        console.log('DEBUG: averageSpeedKmh:', run.averageSpeedKmh, typeof run.averageSpeedKmh);
        console.log('DEBUG: maxSpeedKmh:', run.maxSpeedKmh, typeof run.maxSpeedKmh);
        this.run = run;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading run details:', error);
        this.error = 'Fehler beim Laden der Lauf-Details';
        this.isLoading = false;
      }
    });
  }

  getStatusColor(status: RunStatus): string {
    switch (status) {
      case RunStatus.COMPLETED:
        return 'primary';
      case RunStatus.ACTIVE:
        return 'accent';
      case RunStatus.DRAFT:
        return 'warn';
      default:
        return '';
    }
  }

  getTypeIcon(type: RunType): string {
    switch (type) {
      case RunType.OUTDOOR:
        return 'directions_run';
      case RunType.TREADMILL:
        return 'fitness_center';
      case RunType.TRACK:
        return 'track_changes';
      case RunType.TRAIL:
        return 'terrain';
      default:
        return 'directions_run';
    }
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('de-DE', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  formatDuration(seconds: number | undefined): string {
    return this.runService.formatDuration(seconds);
  }

  formatDistance(meters: number | undefined): string {
    if (!meters) return '0.00 km';
    return `${(meters / 1000).toFixed(2)} km`;
  }

  formatPace(paceSeconds: number | undefined): string {
    if (!paceSeconds) return '--:--';
    
    const minutes = Math.floor(paceSeconds / 60);
    const seconds = Math.floor(paceSeconds % 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')} min/km`;
  }

  formatSpeed(speedKmh: number | undefined): string {
    if (!speedKmh) return '-- km/h';
    return `${speedKmh.toFixed(1)} km/h`;
  }

  formatElevation(elevationMeters: number | undefined): string {
    if (!elevationMeters) return '0 m';
    return `${elevationMeters.toFixed(0)} m`;
  }

  formatTemperature(temp: number | undefined): string {
    if (temp === undefined || temp === null) return '--°C';
    return `${temp}°C`;
  }

  formatHumidity(humidity: number | undefined): string {
    if (!humidity) return '--%';
    return `${humidity}%`;
  }
}