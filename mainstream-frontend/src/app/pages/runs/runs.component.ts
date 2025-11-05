import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RunListComponent } from '../../features/runs/components/run-list/run-list.component';
import { RunDetailsComponent } from '../../features/runs/components/run-details/run-details.component';
import { FitUploadDialogComponent, FitUploadResult } from '../../features/runs/components/fit-upload-dialog/fit-upload-dialog.component';

@Component({
  selector: 'app-runs',
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    RunListComponent,
    RunDetailsComponent
  ],
  templateUrl: './runs.component.html',
  styleUrl: './runs.component.scss'
})
export class RunsComponent {
  selectedRunId: number | null = null;

  constructor(private dialog: MatDialog) {}

  onRunSelected(runId: number): void {
    this.selectedRunId = runId;
  }

  openUploadDialog(): void {
    const dialogRef = this.dialog.open(FitUploadDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      disableClose: false,
      data: {}
    });

    dialogRef.afterClosed().subscribe((result: FitUploadResult | undefined) => {
      if (result && result.success) {
        console.log('Upload successful:', result);
        // Refresh the runs list to show the new upload
        this.refreshRunsList();
        
        // Optionally select the newly uploaded run
        if (result.fileId) {
          this.selectedRunId = result.fileId;
        }
      }
    });
  }

  private refreshRunsList(): void {
    // This will trigger the run list component to reload
    // We could implement this with a service or event system
    // For now, we'll just reset the selected run to trigger a refresh
    window.location.reload(); // Simple approach - could be improved
  }
}