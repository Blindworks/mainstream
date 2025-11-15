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
  templateUrl: './user-registration.component.html',
  styleUrls: ['./user-registration.component.scss']
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