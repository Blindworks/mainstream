import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Run, RunStatus, RunType } from '../../models/run.model';
import { RunService, RouteMatchResponse, UserActivity } from '../../services/run.service';
import { RunRoundsTrophiesComponent } from '../run-rounds-trophies/run-rounds-trophies.component';

@Component({
  selector: 'app-run-details',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatButtonModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RunRoundsTrophiesComponent
  ],
  templateUrl: './run-details.component.html',
  styleUrl: './run-details.component.scss'
})
export class RunDetailsComponent implements OnChanges {
  @Input() runId: number | null = null;

  run: Run | null = null;
  isLoading = false;
  error: string | null = null;

  // Route matching state
  isMatching = false;
  matchedActivity: UserActivity | null = null;
  matchError: string | null = null;

  constructor(
    private runService: RunService,
    private snackBar: MatSnackBar
  ) {
    console.log('DEBUG: RunDetailsComponent constructor called');
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('DEBUG: ngOnChanges called', changes);
    console.log('DEBUG: runId:', this.runId);

    if (changes['runId'] && this.runId) {
      console.log('DEBUG: Loading run details for runId:', this.runId);
      this.loadRunDetails();
    } else if (changes['runId'] && !this.runId) {
      console.log('DEBUG: runId is null, clearing run data');
      this.run = null;
    }
  }

  private loadRunDetails(): void {
    console.log('DEBUG: loadRunDetails() called with runId:', this.runId);

    if (!this.runId) {
      console.log('DEBUG: runId is null/undefined, returning');
      return;
    }

    this.isLoading = true;
    this.error = null;
    console.log('DEBUG: Starting API call to getRunById:', this.runId);

    this.runService.getRunById(this.runId).subscribe({
      next: (run) => {
        console.log('DEBUG: ========== Run data received ==========');
        console.log('DEBUG: Full run object:', run);
        console.log('DEBUG: averageSpeedKmh:', run.averageSpeedKmh, typeof run.averageSpeedKmh);
        console.log('DEBUG: maxSpeedKmh:', run.maxSpeedKmh, typeof run.maxSpeedKmh);
        console.log('DEBUG: ========================================');
        this.run = run;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('DEBUG: ERROR loading run details:', error);
        console.error('DEBUG: Error status:', error.status);
        console.error('DEBUG: Error message:', error.message);
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

  /**
   * Calculates pace in seconds per kilometer from speed in km/h
   * Formula: pace (s/km) = 3600 / speed (km/h)
   */
  calculatePaceFromSpeed(speedKmh: number | undefined): number | undefined {
    if (!speedKmh || speedKmh === 0) return undefined;
    return 3600 / speedKmh; // Convert to seconds per km
  }

  /**
   * Formats pace from speed (km/h) to min/km format
   */
  formatPaceFromSpeed(speedKmh: number | undefined): string {
    const paceSeconds = this.calculatePaceFromSpeed(speedKmh);
    if (!paceSeconds) return '--:--';

    const minutes = Math.floor(paceSeconds / 60);
    const seconds = Math.floor(paceSeconds % 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')} min/km`;
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

  /**
   * Trigger route matching for this run
   */
  matchRunToRoute(): void {
    if (!this.runId) return;

    this.isMatching = true;
    this.matchError = null;

    this.runService.matchRunToRoute(this.runId).subscribe({
      next: (response: RouteMatchResponse) => {
        this.isMatching = false;

        if (response.matched && response.activity) {
          this.matchedActivity = response.activity;
          this.snackBar.open(
            `Route gefunden: ${response.activity.matchedRouteName} (${response.activity.routeCompletionPercentage?.toFixed(1)}% abgeschlossen)`,
            'Schließen',
            { duration: 5000, panelClass: 'success-snackbar' }
          );
        } else {
          this.snackBar.open(
            'Keine passende Route gefunden',
            'OK',
            { duration: 3000 }
          );
        }
      },
      error: (error) => {
        console.error('Error matching route:', error);
        this.isMatching = false;
        this.matchError = 'Fehler beim Route-Matching';
        this.snackBar.open(
          'Fehler beim Abgleich mit vordefinierten Routen',
          'Schließen',
          { duration: 3000, panelClass: 'error-snackbar' }
        );
      }
    });
  }

  /**
   * Get direction label in German
   */
  getDirectionLabel(direction: string | undefined): string {
    switch (direction) {
      case 'CLOCKWISE':
        return 'Im Uhrzeigersinn';
      case 'COUNTER_CLOCKWISE':
        return 'Gegen den Uhrzeigersinn';
      default:
        return 'Unbekannt';
    }
  }

  /**
   * Format completion percentage
   */
  formatPercentage(value: number | undefined): string {
    if (value === undefined || value === null) return '--';
    return `${value.toFixed(1)}%`;
  }

  /**
   * Format accuracy in meters
   */
  formatAccuracy(meters: number | undefined): string {
    if (meters === undefined || meters === null) return '--';
    return `${meters.toFixed(1)} m`;
  }
}