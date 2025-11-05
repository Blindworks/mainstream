import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrophyWithProgress, TrophyCategory } from '../../models/trophy.model';
import { TrophyService } from '../../services/trophy.service';

@Component({
  selector: 'app-trophy-list',
  imports: [CommonModule],
  templateUrl: './trophy-list.component.html',
  styleUrl: './trophy-list.component.scss'
})
export class TrophyListComponent implements OnInit {
  @Input() selectedTrophyId: number | null = null;
  @Output() trophySelected = new EventEmitter<number>();

  trophies: TrophyWithProgress[] = [];
  filteredTrophies: TrophyWithProgress[] = [];
  isLoading = false;
  error: string | null = null;

  // Filter state
  selectedCategory: TrophyCategory | 'ALL' = 'ALL';
  showOnlyEarned = false;

  readonly categories = [
    { value: 'ALL', label: 'Alle' },
    { value: TrophyCategory.BEGINNER, label: 'Anfänger' },
    { value: TrophyCategory.INTERMEDIATE, label: 'Fortgeschritten' },
    { value: TrophyCategory.ADVANCED, label: 'Erweitert' },
    { value: TrophyCategory.ELITE, label: 'Elite' },
    { value: TrophyCategory.SPECIAL, label: 'Spezial' }
  ];

  constructor(public trophyService: TrophyService) {}

  ngOnInit(): void {
    this.loadTrophies();
  }

  loadTrophies(): void {
    this.isLoading = true;
    this.error = null;

    this.trophyService.getTrophiesWithProgress().subscribe({
      next: (trophies) => {
        this.trophies = trophies.sort((a, b) =>
          (a.displayOrder || 0) - (b.displayOrder || 0)
        );
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading trophies:', error);
        this.error = 'Fehler beim Laden der Trophäen';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredTrophies = this.trophies.filter(trophy => {
      // Category filter
      if (this.selectedCategory !== 'ALL' && trophy.category !== this.selectedCategory) {
        return false;
      }

      // Earned filter
      if (this.showOnlyEarned && !trophy.isEarned) {
        return false;
      }

      return true;
    });
  }

  onCategoryChange(category: TrophyCategory | 'ALL'): void {
    this.selectedCategory = category;
    this.applyFilters();
  }

  toggleShowOnlyEarned(): void {
    this.showOnlyEarned = !this.showOnlyEarned;
    this.applyFilters();
  }

  onTrophyClick(trophy: TrophyWithProgress): void {
    this.trophySelected.emit(trophy.id);
  }

  getEarnedCount(): number {
    return this.trophies.filter(t => t.isEarned).length;
  }

  getTotalCount(): number {
    return this.trophies.length;
  }

  getProgressPercentage(): number {
    const total = this.getTotalCount();
    if (total === 0) return 0;
    return Math.round((this.getEarnedCount() / total) * 100);
  }

  getCategoryColor(category: string): string {
    return this.trophyService.getCategoryColor(category);
  }

  formatDate(date: Date | undefined): string {
    if (!date) return '';
    return this.trophyService.formatDate(date);
  }
}
