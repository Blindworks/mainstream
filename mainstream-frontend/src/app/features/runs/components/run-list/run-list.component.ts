import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { RunSummary, RunStatus, RunType } from '../../models/run.model';
import { RunService } from '../../services/run.service';

@Component({
  selector: 'app-run-list',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatCardModule
  ],
  templateUrl: './run-list.component.html',
  styleUrl: './run-list.component.scss'
})
export class RunListComponent implements OnInit {
  @Output() runSelected = new EventEmitter<number>();
  @Input() selectedRunId: number | null = null;

  runs: RunSummary[] = [];
  totalRuns = 0;
  pageSize = 10;
  currentPage = 0;
  isLoading = false;

  constructor(private runService: RunService) {}

  ngOnInit(): void {
    this.loadRuns();
  }

  loadRuns(): void {
    this.isLoading = true;
    this.runService.getAllRuns(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.runs = response.content;
        this.totalRuns = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading runs:', error);
        this.isLoading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRuns();
  }

  onRunClick(run: RunSummary): void {
    console.log('DEBUG: Run clicked:', run.id, run.title);
    this.runSelected.emit(run.id);
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
      day: '2-digit',
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  formatDuration(seconds: number | undefined): string {
    return this.runService.formatDuration(seconds);
  }

  formatDistance(distanceKm: number | undefined): string {
    return this.runService.formatDistance(distanceKm);
  }

  isSelected(runId: number): boolean {
    return this.selectedRunId === runId;
  }
}
