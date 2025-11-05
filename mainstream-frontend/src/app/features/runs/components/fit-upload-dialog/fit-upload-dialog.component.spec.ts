import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FitUploadDialogComponent } from './fit-upload-dialog.component';
import { FitUploadService } from '../../services/fit-upload.service';

describe('FitUploadDialogComponent', () => {
  let component: FitUploadDialogComponent;
  let fixture: ComponentFixture<FitUploadDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FitUploadDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: FitUploadService, useValue: {} },
        { provide: MatSnackBar, useValue: {} }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FitUploadDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});