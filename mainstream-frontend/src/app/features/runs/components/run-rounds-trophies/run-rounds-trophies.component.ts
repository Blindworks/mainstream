import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RunService } from '../../services/run.service';
import { TrophyService } from '../../../trophies/services/trophy.service';
import { Lap } from '../../models/lap.model';
import { UserTrophy } from '../../../trophies/models/trophy.model';

@Component({
  selector: 'app-run-rounds-trophies',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatExpansionModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './run-rounds-trophies.component.html',
  styleUrl: './run-rounds-trophies.component.scss'
})
export class RunRoundsTrophiesComponent implements OnChanges {
  @Input() runId: number | null = null;
  @Input() activityId: number | null = null;

  laps: Lap[] = [];
  trophies: UserTrophy[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(
    private runService: RunService,
    private trophyService: TrophyService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['runId'] || changes['activityId']) && (this.runId || this.activityId)) {
      this.loadData();
    } else if ((changes['runId'] || changes['activityId']) && !this.runId && !this.activityId) {
      this.laps = [];
      this.trophies = [];
    }
  }

  private loadData(): void {
    if (!this.runId && !this.activityId) {
      return;
    }

    this.isLoading = true;
    this.error = null;

    // Load laps (uses runId)
    if (this.runId) {
      this.runService.getRunLaps(this.runId).subscribe({
        next: (laps) => {
          this.laps = laps;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading laps:', error);
          this.laps = [];
          this.isLoading = false;
        }
      });
    }

    // Load trophies (uses activityId if available, fallback to runId)
    const idToUse = this.activityId || this.runId;
    if (idToUse) {
      console.log('Loading trophies for activity ID:', idToUse);
      this.trophyService.getTrophiesForActivity(idToUse).subscribe({
        next: (trophies) => {
          console.log('Loaded trophies:', trophies);
          this.trophies = trophies;
        },
        error: (error) => {
          console.error('Error loading trophies:', error);
          this.trophies = [];
        }
      });
    }
  }

  formatDuration(seconds: number | undefined): string {
    if (!seconds) return '00:00:00';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  formatDistance(km: number | undefined): string {
    if (!km) return '0.00 km';
    return `${km.toFixed(2)} km`;
  }

  getTrophyCategoryColor(category: string): string {
    return this.trophyService.getCategoryColor(category);
  }
}
