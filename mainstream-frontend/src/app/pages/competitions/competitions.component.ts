import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompetitionListComponent } from '../../features/competitions/components/competition-list/competition-list.component';

@Component({
  selector: 'app-competitions',
  imports: [
    CommonModule,
    CompetitionListComponent
  ],
  templateUrl: './competitions.component.html',
  styleUrl: './competitions.component.scss'
})
export class CompetitionsComponent {
  selectedCompetitionId: number | null = null;

  onCompetitionSelected(competitionId: number): void {
    this.selectedCompetitionId = competitionId;
    // TODO: Navigate to competition details or show in side panel
    console.log('Competition selected:', competitionId);
  }
}
