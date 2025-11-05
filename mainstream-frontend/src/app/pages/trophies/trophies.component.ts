import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrophyListComponent } from '../../features/trophies/components/trophy-list/trophy-list.component';

@Component({
  selector: 'app-trophies',
  imports: [
    CommonModule,
    TrophyListComponent
  ],
  templateUrl: './trophies.component.html',
  styleUrl: './trophies.component.scss'
})
export class TrophiesComponent {
  selectedTrophyId: number | null = null;

  onTrophySelected(trophyId: number): void {
    this.selectedTrophyId = trophyId;
    // TODO: Navigate to trophy details or show in side panel
    console.log('Trophy selected:', trophyId);
  }
}
