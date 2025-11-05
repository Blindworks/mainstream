import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, DailyWinner, CreateTrophyRequest, UpdateTrophyRequest } from '../../services/admin.service';
import { Trophy, TrophyType, TrophyCategory } from '../../../trophies/models/trophy.model';

@Component({
  selector: 'app-trophy-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trophy-management.component.html',
  styleUrls: ['./trophy-management.component.css']
})
export class TrophyManagementComponent implements OnInit {
  loading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  recentWinners: DailyWinner[] = [];
  loadingWinners = false;

  // Trophy management
  trophies: Trophy[] = [];
  loadingTrophies = false;
  showTrophyForm = false;
  editingTrophy: Trophy | null = null;

  // Form model
  trophyForm = {
    code: '',
    name: '',
    description: '',
    type: 'DISTANCE_MILESTONE' as keyof typeof TrophyType,
    category: 'BEGINNER' as keyof typeof TrophyCategory,
    iconUrl: '',
    criteriaValue: undefined as number | undefined,
    isActive: true,
    displayOrder: undefined as number | undefined
  };

  // Enums for dropdowns
  trophyTypes = Object.keys(TrophyType);
  trophyCategories = Object.keys(TrophyCategory);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadRecentWinners();
    this.loadTrophies();
  }

  initializeTrophies(): void {
    this.loading = true;
    this.clearMessages();

    this.adminService.initializeTrophies().subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response;
        this.loadTrophies();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error || 'Failed to initialize trophies';
      }
    });
  }

  calculateDailyWinners(): void {
    this.loading = true;
    this.clearMessages();

    this.adminService.calculateDailyWinners().subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response;
        this.loadRecentWinners();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error || 'Failed to calculate daily winners';
      }
    });
  }

  loadRecentWinners(): void {
    this.loadingWinners = true;

    this.adminService.getRecentWinners(7).subscribe({
      next: (winners) => {
        this.recentWinners = winners;
        this.loadingWinners = false;
      },
      error: (error) => {
        console.error('Failed to load recent winners:', error);
        this.loadingWinners = false;
      }
    });
  }

  // Trophy CRUD operations
  loadTrophies(): void {
    this.loadingTrophies = true;
    this.clearMessages();

    this.adminService.getAllTrophiesAdmin().subscribe({
      next: (trophies) => {
        this.trophies = trophies;
        this.loadingTrophies = false;
      },
      error: (error) => {
        console.error('Failed to load trophies:', error);
        this.errorMessage = 'Failed to load trophies';
        this.loadingTrophies = false;
      }
    });
  }

  openCreateForm(): void {
    this.resetForm();
    this.editingTrophy = null;
    this.showTrophyForm = true;
    this.clearMessages();
  }

  openEditForm(trophy: Trophy): void {
    this.editingTrophy = trophy;
    this.trophyForm = {
      code: trophy.code,
      name: trophy.name,
      description: trophy.description,
      type: trophy.type as keyof typeof TrophyType,
      category: trophy.category as keyof typeof TrophyCategory,
      iconUrl: trophy.iconUrl || '',
      criteriaValue: trophy.criteriaValue,
      isActive: trophy.isActive,
      displayOrder: trophy.displayOrder
    };
    this.showTrophyForm = true;
    this.clearMessages();
  }

  cancelForm(): void {
    this.showTrophyForm = false;
    this.editingTrophy = null;
    this.resetForm();
    this.clearMessages();
  }

  saveTrophy(): void {
    if (!this.validateForm()) {
      return;
    }

    if (this.editingTrophy) {
      this.updateTrophy();
    } else {
      this.createTrophy();
    }
  }

  createTrophy(): void {
    this.loading = true;
    this.clearMessages();

    const request: CreateTrophyRequest = {
      code: this.trophyForm.code,
      name: this.trophyForm.name,
      description: this.trophyForm.description,
      type: this.trophyForm.type,
      category: this.trophyForm.category,
      iconUrl: this.trophyForm.iconUrl || undefined,
      criteriaValue: this.trophyForm.criteriaValue,
      isActive: this.trophyForm.isActive,
      displayOrder: this.trophyForm.displayOrder
    };

    this.adminService.createTrophy(request).subscribe({
      next: (trophy) => {
        this.loading = false;
        this.successMessage = `Trophy "${trophy.name}" created successfully`;
        this.loadTrophies();
        this.cancelForm();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Failed to create trophy';
      }
    });
  }

  updateTrophy(): void {
    if (!this.editingTrophy) return;

    this.loading = true;
    this.clearMessages();

    const request: UpdateTrophyRequest = {
      name: this.trophyForm.name,
      description: this.trophyForm.description,
      iconUrl: this.trophyForm.iconUrl || undefined,
      criteriaValue: this.trophyForm.criteriaValue,
      isActive: this.trophyForm.isActive,
      displayOrder: this.trophyForm.displayOrder
    };

    this.adminService.updateTrophy(this.editingTrophy.id, request).subscribe({
      next: (trophy) => {
        this.loading = false;
        this.successMessage = `Trophy "${trophy.name}" updated successfully`;
        this.loadTrophies();
        this.cancelForm();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Failed to update trophy';
      }
    });
  }

  deleteTrophy(trophy: Trophy): void {
    if (!confirm(`Are you sure you want to delete the trophy "${trophy.name}"? This action cannot be undone.`)) {
      return;
    }

    this.loading = true;
    this.clearMessages();

    this.adminService.deleteTrophy(trophy.id).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = `Trophy "${trophy.name}" deleted successfully`;
        this.loadTrophies();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Failed to delete trophy';
      }
    });
  }

  toggleTrophyActive(trophy: Trophy): void {
    this.loading = true;
    this.clearMessages();

    const action = trophy.isActive
      ? this.adminService.deactivateTrophy(trophy.id)
      : this.adminService.activateTrophy(trophy.id);

    action.subscribe({
      next: (updatedTrophy) => {
        this.loading = false;
        this.successMessage = `Trophy "${updatedTrophy.name}" ${updatedTrophy.isActive ? 'activated' : 'deactivated'} successfully`;
        this.loadTrophies();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Failed to toggle trophy status';
      }
    });
  }

  validateForm(): boolean {
    if (!this.trophyForm.name.trim()) {
      this.errorMessage = 'Trophy name is required';
      return false;
    }

    if (!this.editingTrophy && !this.trophyForm.code.trim()) {
      this.errorMessage = 'Trophy code is required';
      return false;
    }

    if (!this.trophyForm.description.trim()) {
      this.errorMessage = 'Trophy description is required';
      return false;
    }

    return true;
  }

  resetForm(): void {
    this.trophyForm = {
      code: '',
      name: '',
      description: '',
      type: 'DISTANCE_MILESTONE',
      category: 'BEGINNER',
      iconUrl: '',
      criteriaValue: undefined,
      isActive: true,
      displayOrder: undefined
    };
  }

  getCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      'EARLIEST_RUN': 'Frühaufsteher',
      'LATEST_RUN': 'Nachteule',
      'LONGEST_STREAK': 'Longest Streak',
      'MOST_RUNS': 'Consistency King',
      'MOST_ROUTES': 'Explorer',
      'LONGEST_DISTANCE': 'Distance Hero',
      'FASTEST_TIME': 'Speed Demon'
    };
    return labels[category] || category;
  }

  getTrophyTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      'DISTANCE_MILESTONE': 'Distanz-Meilenstein',
      'STREAK': 'Serie',
      'ROUTE_COMPLETION': 'Streckenabschluss',
      'CONSISTENCY': 'Beständigkeit',
      'TIME_BASED': 'Zeitbasiert',
      'EXPLORER': 'Entdecker',
      'SPECIAL': 'Spezial'
    };
    return labels[type] || type;
  }

  getTrophyCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      'BEGINNER': 'Anfänger',
      'INTERMEDIATE': 'Fortgeschritten',
      'ADVANCED': 'Erweitert',
      'ELITE': 'Elite',
      'SPECIAL': 'Spezial'
    };
    return labels[category] || category;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  private clearMessages(): void {
    this.successMessage = null;
    this.errorMessage = null;
  }
}
