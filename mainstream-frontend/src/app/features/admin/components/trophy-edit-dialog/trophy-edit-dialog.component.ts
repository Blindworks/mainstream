import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Trophy, TrophyType, TrophyCategory, CheckScope } from '../../../trophies/models/trophy.model';
import { AdminService } from '../../services/admin.service';

export interface TrophyEditDialogData {
  trophy: Trophy;
}

@Component({
  selector: 'app-trophy-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './trophy-edit-dialog.component.html',
  styleUrls: ['./trophy-edit-dialog.component.css']
})
export class TrophyEditDialogComponent implements OnInit {
  saving = false;
  errorMessage = '';

  trophyForm = {
    code: '',
    name: '',
    description: '',
    type: 'DISTANCE_MILESTONE' as TrophyType,
    category: 'BEGINNER' as TrophyCategory,
    iconUrl: '',
    criteriaValue: undefined as number | undefined,
    isActive: true,
    displayOrder: undefined as number | undefined,
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,
    collectionRadiusMeters: undefined as number | undefined,
    validFrom: undefined as string | undefined,
    validUntil: undefined as string | undefined,
    imageUrl: '',
    checkScope: undefined as CheckScope | undefined,
    criteriaConfig: ''
  };

  trophyTypes: TrophyType[] = [
    TrophyType.DISTANCE_MILESTONE,
    TrophyType.STREAK,
    TrophyType.ROUTE_COMPLETION,
    TrophyType.CONSISTENCY,
    TrophyType.TIME_BASED,
    TrophyType.EXPLORER,
    TrophyType.LOCATION_BASED,
    TrophyType.SPECIAL
  ];

  trophyCategories: TrophyCategory[] = [
    TrophyCategory.BEGINNER,
    TrophyCategory.INTERMEDIATE,
    TrophyCategory.ADVANCED,
    TrophyCategory.ELITE,
    TrophyCategory.SPECIAL
  ];

  checkScopes: CheckScope[] = [
    CheckScope.SINGLE_ACTIVITY,
    CheckScope.TOTAL,
    CheckScope.TIME_RANGE,
    CheckScope.COUNT
  ];

  constructor(
    public dialogRef: MatDialogRef<TrophyEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TrophyEditDialogData,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    // Pre-fill form with existing trophy values
    const trophy = this.data.trophy;
    this.trophyForm = {
      code: trophy.code,
      name: trophy.name,
      description: trophy.description,
      type: trophy.type,
      category: trophy.category,
      iconUrl: trophy.iconUrl || '',
      criteriaValue: trophy.criteriaValue,
      isActive: trophy.isActive,
      displayOrder: trophy.displayOrder,
      latitude: trophy.latitude,
      longitude: trophy.longitude,
      collectionRadiusMeters: trophy.collectionRadiusMeters,
      validFrom: trophy.validFrom ? this.formatDateForInput(new Date(trophy.validFrom)) : undefined,
      validUntil: trophy.validUntil ? this.formatDateForInput(new Date(trophy.validUntil)) : undefined,
      imageUrl: trophy.imageUrl || '',
      checkScope: trophy.checkScope,
      criteriaConfig: trophy.criteriaConfig || ''
    };
  }

  isLocationBased(): boolean {
    return this.trophyForm.type === TrophyType.LOCATION_BASED;
  }

  saveTrophy(): void {
    if (!this.validateForm()) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const updateRequest = {
      name: this.trophyForm.name,
      description: this.trophyForm.description,
      iconUrl: this.trophyForm.iconUrl || undefined,
      criteriaValue: this.trophyForm.criteriaValue,
      isActive: this.trophyForm.isActive,
      displayOrder: this.trophyForm.displayOrder,
      latitude: this.trophyForm.latitude,
      longitude: this.trophyForm.longitude,
      collectionRadiusMeters: this.trophyForm.collectionRadiusMeters,
      validFrom: this.trophyForm.validFrom ? this.formatDateForBackend(this.trophyForm.validFrom) : undefined,
      validUntil: this.trophyForm.validUntil ? this.formatDateForBackend(this.trophyForm.validUntil) : undefined,
      imageUrl: this.trophyForm.imageUrl || undefined,
      checkScope: this.trophyForm.checkScope,
      criteriaConfig: this.trophyForm.criteriaConfig || undefined
    };

    this.adminService.updateTrophy(this.data.trophy.id, updateRequest).subscribe({
      next: (updatedTrophy) => {
        this.dialogRef.close(updatedTrophy);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Fehler beim Aktualisieren der Trophäe';
        this.saving = false;
      }
    });
  }

  validateForm(): boolean {
    if (!this.trophyForm.name || !this.trophyForm.description) {
      this.errorMessage = 'Name und Beschreibung sind erforderlich';
      return false;
    }

    if (this.isLocationBased()) {
      if (this.trophyForm.latitude === undefined || this.trophyForm.longitude === undefined) {
        this.errorMessage = 'Breitengrad und Längengrad sind für ortsbasierte Trophäen erforderlich';
        return false;
      }
      if (this.trophyForm.collectionRadiusMeters === undefined) {
        this.errorMessage = 'Sammelradius ist für ortsbasierte Trophäen erforderlich';
        return false;
      }
    }

    return true;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  private formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  private formatDateForBackend(dateString: string): string {
    return new Date(dateString).toISOString();
  }
}
