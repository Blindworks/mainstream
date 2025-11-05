import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../services/auth.service';
import { UserRegistration } from '../../models/user.model';

@Component({
  selector: 'app-user-registration',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="registration-container">
      <mat-card class="registration-card">
        <mat-card-header>
          <mat-card-title>MainStream Registration</mat-card-title>
          <mat-card-subtitle>Erstelle dein Läufer-Profil</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          <form [formGroup]="registrationForm" (ngSubmit)="onSubmit()" class="registration-form">
            
            <!-- Email and Password Section -->
            <div class="form-section">
              <h3>Account Informationen</h3>
              
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>E-Mail</mat-label>
                <input matInput type="email" formControlName="email" required>
                <mat-icon matSuffix>email</mat-icon>
                @if (registrationForm.get('email')?.hasError('required') && registrationForm.get('email')?.touched) {
                  <mat-error>E-Mail ist erforderlich</mat-error>
                }
                @if (registrationForm.get('email')?.hasError('email') && registrationForm.get('email')?.touched) {
                  <mat-error>Ungültige E-Mail-Adresse</mat-error>
                }
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Passwort</mat-label>
                <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" required>
                <button type="button" mat-icon-button matSuffix (click)="togglePasswordVisibility()">
                  <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
                </button>
                @if (registrationForm.get('password')?.hasError('required') && registrationForm.get('password')?.touched) {
                  <mat-error>Passwort ist erforderlich</mat-error>
                }
                @if (registrationForm.get('password')?.hasError('minlength') && registrationForm.get('password')?.touched) {
                  <mat-error>Passwort muss mindestens 6 Zeichen haben</mat-error>
                }
              </mat-form-field>
            </div>

            <!-- Personal Information Section -->
            <div class="form-section">
              <h3>Persönliche Informationen</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Vorname</mat-label>
                  <input matInput formControlName="firstName" required>
                  @if (registrationForm.get('firstName')?.hasError('required') && registrationForm.get('firstName')?.touched) {
                    <mat-error>Vorname ist erforderlich</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Nachname</mat-label>
                  <input matInput formControlName="lastName" required>
                  @if (registrationForm.get('lastName')?.hasError('required') && registrationForm.get('lastName')?.touched) {
                    <mat-error>Nachname ist erforderlich</mat-error>
                  }
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Geburtsdatum</mat-label>
                  <input matInput [matDatepicker]="picker" formControlName="dateOfBirth">
                  <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
                  <mat-datepicker #picker></mat-datepicker>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Geschlecht</mat-label>
                  <mat-select formControlName="gender">
                    <mat-option value="MALE">Männlich</mat-option>
                    <mat-option value="FEMALE">Weiblich</mat-option>
                    <mat-option value="OTHER">Divers</mat-option>
                    <mat-option value="PREFER_NOT_TO_SAY">Keine Angabe</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Telefonnummer (optional)</mat-label>
                <input matInput type="tel" formControlName="phoneNumber">
                <mat-icon matSuffix>phone</mat-icon>
              </mat-form-field>
            </div>

            <!-- Running Profile Section -->
            <div class="form-section">
              <h3>Läufer-Profil</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Fitness Level</mat-label>
                  <mat-select formControlName="fitnessLevel">
                    <mat-option value="BEGINNER">Anfänger</mat-option>
                    <mat-option value="INTERMEDIATE">Fortgeschritten</mat-option>
                    <mat-option value="ADVANCED">Erfahren</mat-option>
                    <mat-option value="EXPERT">Experte</mat-option>
                  </mat-select>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Bevorzugte Einheit</mat-label>
                  <mat-select formControlName="preferredDistanceUnit">
                    <mat-option value="KILOMETERS">Kilometer</mat-option>
                    <mat-option value="MILES">Meilen</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Bio (optional)</mat-label>
                <textarea matInput formControlName="bio" rows="3" 
                         placeholder="Erzähle etwas über dich und deine Lauferfahrung..."></textarea>
              </mat-form-field>

              <mat-checkbox formControlName="isPublicProfile" class="full-width">
                Öffentliches Profil (andere Benutzer können dein Profil sehen)
              </mat-checkbox>
            </div>

            <!-- Submit Section -->
            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" 
                      [disabled]="registrationForm.invalid || isSubmitting()" 
                      class="submit-button">
                @if (isSubmitting()) {
                  <mat-spinner diameter="20" class="submit-spinner"></mat-spinner>
                  Registrierung läuft...
                } @else {
                  Registrieren
                }
              </button>

              <div class="login-link">
                Bereits ein Account? 
                <a routerLink="/auth/login" mat-button>Hier einloggen</a>
              </div>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .registration-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .registration-card {
      width: 100%;
      max-width: 600px;
      margin: auto;
    }

    .registration-form {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .form-section {
      margin-bottom: 20px;
    }

    .form-section h3 {
      margin: 0 0 15px 0;
      color: #333;
      font-weight: 500;
      border-bottom: 2px solid #667eea;
      padding-bottom: 5px;
    }

    .form-row {
      display: flex;
      gap: 16px;
      align-items: flex-start;
    }

    .full-width {
      width: 100%;
    }

    .half-width {
      width: calc(50% - 8px);
    }

    .form-actions {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 20px;
    }

    .submit-button {
      height: 48px;
      font-size: 16px;
      position: relative;
    }

    .submit-spinner {
      margin-right: 10px;
    }

    .login-link {
      text-align: center;
      color: #666;
    }

    .login-link a {
      color: #667eea;
      text-decoration: none;
    }

    @media (max-width: 600px) {
      .form-row {
        flex-direction: column;
        gap: 0;
      }

      .half-width {
        width: 100%;
      }

      .registration-container {
        padding: 10px;
      }
    }
  `]
})
export class UserRegistrationComponent {
  registrationForm: FormGroup;
  hidePassword = signal(true);
  isSubmitting = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registrationForm = this.createForm();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      // Required fields
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      
      // Optional fields
      dateOfBirth: [''],
      gender: [''],
      phoneNumber: [''],
      bio: [''],
      fitnessLevel: ['BEGINNER'],
      preferredDistanceUnit: ['KILOMETERS'],
      isPublicProfile: [false]
    });
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update(value => !value);
  }

  onSubmit(): void {
    if (this.registrationForm.valid) {
      this.isSubmitting.set(true);
      
      const formValue = this.registrationForm.value;
      
      // Format date if provided
      if (formValue.dateOfBirth) {
        formValue.dateOfBirth = new Date(formValue.dateOfBirth).toISOString().split('T')[0];
      }

      // Clean up empty optional fields
      const registrationData: UserRegistration = {
        email: formValue.email,
        password: formValue.password,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        dateOfBirth: formValue.dateOfBirth || undefined,
        gender: formValue.gender || undefined,
        phoneNumber: formValue.phoneNumber?.trim() || undefined,
        bio: formValue.bio?.trim() || undefined,
        fitnessLevel: formValue.fitnessLevel || 'BEGINNER',
        preferredDistanceUnit: formValue.preferredDistanceUnit || 'KILOMETERS',
        isPublicProfile: formValue.isPublicProfile || false
      };

      this.authService.register(registrationData).subscribe({
        next: (user) => {
          this.isSubmitting.set(false);
          this.snackBar.open(
            `Willkommen ${user.firstName}! Registrierung erfolgreich. Du kannst dich jetzt einloggen.`,
            'OK',
            { duration: 5000 }
          );
          this.router.navigate(['/auth/login']);
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Registration error:', error);
          
          let errorMessage = 'Registrierung fehlgeschlagen. Bitte versuche es erneut.';
          if (error.error?.message) {
            errorMessage = error.error.message;
          } else if (error.status === 409) {
            errorMessage = 'Ein Benutzer mit dieser E-Mail existiert bereits.';
          }
          
          this.snackBar.open(errorMessage, 'OK', { duration: 5000 });
        }
      });
    }
  }
}