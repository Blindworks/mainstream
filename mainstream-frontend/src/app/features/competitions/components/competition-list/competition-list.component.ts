import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatBadgeModule } from '@angular/material/badge';
import {
  CompetitionSummary,
  CompetitionStatus,
  getCompetitionTypeLabel,
  getCompetitionStatusLabel,
  getDifficultyLabel,
  getCompetitionStatusColor,
  getDifficultyColor
} from '../../models/competition.model';
import { CompetitionService } from '../../services/competition.service';

@Component({
  selector: 'app-competition-list',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTabsModule,
    MatBadgeModule
  ],
  templateUrl: './competition-list.component.html',
  styleUrl: './competition-list.component.scss'
})
export class CompetitionListComponent implements OnInit {
  @Output() competitionSelected = new EventEmitter<number>();
  @Input() selectedCompetitionId: number | null = null;

  competitions: CompetitionSummary[] = [];
  totalCompetitions = 0;
  pageSize = 12;
  currentPage = 0;
  isLoading = false;
  currentTab = 0;

  CompetitionStatus = CompetitionStatus;

  constructor(public competitionService: CompetitionService) {}

  ngOnInit(): void {
    this.loadCompetitions();
  }

  loadCompetitions(): void {
    this.isLoading = true;

    // Tab 0: All, Tab 1: Active, Tab 2: Upcoming, Tab 3: My Competitions
    switch (this.currentTab) {
      case 0:
        this.loadAllCompetitions();
        break;
      case 1:
        this.loadCompetitionsByStatus(CompetitionStatus.ACTIVE);
        break;
      case 2:
        this.loadCompetitionsByStatus(CompetitionStatus.UPCOMING);
        break;
      case 3:
        this.loadMyCompetitions();
        break;
    }
  }

  loadAllCompetitions(): void {
    this.competitionService.getAllCompetitions(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.competitions = response.content;
        this.totalCompetitions = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading competitions:', error);
        this.isLoading = false;
      }
    });
  }

  loadCompetitionsByStatus(status: CompetitionStatus): void {
    this.competitionService.getCompetitionsByStatus(status, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.competitions = response.content;
        this.totalCompetitions = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading competitions:', error);
        this.isLoading = false;
      }
    });
  }

  loadMyCompetitions(): void {
    // For now, filter by isUserParticipating
    // In a real app, we'd call a dedicated endpoint
    this.competitionService.getAllCompetitions(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.competitions = response.content.filter(c => c.isUserParticipating);
        this.totalCompetitions = this.competitions.length;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading competitions:', error);
        this.isLoading = false;
      }
    });
  }

  onTabChange(index: number): void {
    this.currentTab = index;
    this.currentPage = 0;
    this.loadCompetitions();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCompetitions();
  }

  onCompetitionClick(competition: CompetitionSummary): void {
    this.competitionSelected.emit(competition.id);
  }

  onJoinCompetition(competition: CompetitionSummary, event: Event): void {
    event.stopPropagation();

    if (!this.canJoin(competition)) {
      return;
    }

    this.competitionService.joinCompetition(competition.id).subscribe({
      next: () => {
        console.log('Successfully joined competition');
        this.loadCompetitions(); // Reload to update participation status
      },
      error: (error) => {
        console.error('Error joining competition:', error);
      }
    });
  }

  getStatusColor(status: CompetitionStatus): string {
    return getCompetitionStatusColor(status);
  }

  getStatusLabel(status: CompetitionStatus): string {
    return getCompetitionStatusLabel(status);
  }

  getTypeLabel(type: any): string {
    return getCompetitionTypeLabel(type);
  }

  getDifficultyLabel(difficulty: any): string {
    return getDifficultyLabel(difficulty);
  }

  getDifficultyColor(difficulty: any): string {
    return getDifficultyColor(difficulty);
  }

  formatDate(date: Date): string {
    return this.competitionService.formatDate(date);
  }

  formatDateTime(date: Date): string {
    return this.competitionService.formatDateTime(date);
  }

  getDaysRemaining(endDate: Date): number {
    return this.competitionService.getDaysRemaining(endDate);
  }

  isSelected(competitionId: number): boolean {
    return this.selectedCompetitionId === competitionId;
  }

  getCompetitionIcon(type: any): string {
    // Return appropriate icons based on competition type
    return 'emoji_events';
  }

  canJoin(competition: CompetitionSummary): boolean {
    return !competition.isUserParticipating &&
           (competition.status === CompetitionStatus.UPCOMING ||
            competition.status === CompetitionStatus.ACTIVE) &&
           (!competition.maxParticipants ||
            competition.currentParticipants < competition.maxParticipants);
  }
}
