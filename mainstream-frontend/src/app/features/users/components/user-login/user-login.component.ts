import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/user.model';

@Component({
  selector: 'app-user-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>MainStream Login</mat-card-title>
          <mat-card-subtitle>Melde dich in deinem Läufer-Account an</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>E-Mail</mat-label>
              <input matInput type="email" formControlName="email" required>
              <mat-icon matSuffix>email</mat-icon>
              @if (loginForm.get('email')?.hasError('required') && loginForm.get('email')?.touched) {
                <mat-error>E-Mail ist erforderlich</mat-error>
              }
              @if (loginForm.get('email')?.hasError('email') && loginForm.get('email')?.touched) {
                <mat-error>Ungültige E-Mail-Adresse</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Passwort</mat-label>
              <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" required>
              <button type="button" mat-icon-button matSuffix (click)="togglePasswordVisibility()">
                <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
              </button>
              @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                <mat-error>Passwort ist erforderlich</mat-error>
              }
            </mat-form-field>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" 
                      [disabled]="loginForm.invalid || isSubmitting()" 
                      class="submit-button">
                @if (isSubmitting()) {
                  <mat-spinner diameter="20" class="submit-spinner"></mat-spinner>
                  Anmeldung läuft...
                } @else {
                  <ng-container>
                    <mat-icon>login</mat-icon>
                    Anmelden
                  </ng-container>
                }
              </button>

              <div class="additional-actions">
                <div class="register-link">
                  Noch kein Account? 
                  <a routerLink="/auth/register" mat-button color="accent">Jetzt registrieren</a>
                </div>
                
                <div class="forgot-password">
                  <a routerLink="/auth/forgot-password" mat-button>Passwort vergessen?</a>
                </div>
              </div>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <!-- Demo Credentials Card -->
      <mat-card class="demo-card">
        <mat-card-header>
          <mat-card-title>Demo Account</mat-card-title>
          <mat-card-subtitle>Zum Testen der Anwendung</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p><strong>E-Mail:</strong> demo&#64;mainstream.com</p>
          <p><strong>Passwort:</strong> demo123</p>
          <button mat-button color="accent" (click)="fillDemoCredentials()">
            <mat-icon>account_circle</mat-icon>
            Demo-Daten verwenden
          </button>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      gap: 20px;
    }

    .login-card {
      width: 100%;
      max-width: 400px;
    }

    .demo-card {
      width: 100%;
      max-width: 300px;
      background-color: rgba(255, 255, 255, 0.95);
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .full-width {
      width: 100%;
    }

    .form-actions {
      display: flex;
      flex-direction: column;
      gap: 20px;
      margin-top: 10px;
    }

    .submit-button {
      height: 48px;
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }

    .submit-spinner {
      margin-right: 10px;
    }

    .additional-actions {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .register-link,
    .forgot-password {
      text-align: center;
      color: #666;
    }

    .register-link a,
    .forgot-password a {
      text-decoration: none;
    }

    .demo-card p {
      margin: 8px 0;
      font-family: 'Roboto Mono', monospace;
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      display: inline-block;
    }

    .demo-card button {
      margin-top: 10px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    @media (max-width: 800px) {
      .login-container {
        flex-direction: column;
        padding: 10px;
      }

      .demo-card {
        max-width: 400px;
      }
    }
  `]
})
export class UserLoginComponent {
  loginForm: FormGroup;
  hidePassword = signal(true);
  isSubmitting = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.createForm();
    
    // Redirect if already logged in
    if (this.authService.isAuthenticated) {
      this.router.navigate(['/home']);
    }
  }

  private createForm(): FormGroup {
    return this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update(value => !value);
  }

  fillDemoCredentials(): void {
    this.loginForm.patchValue({
      email: 'demo@mainstream.com',
      password: 'demo123'
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isSubmitting.set(true);
      
      const loginData: LoginRequest = this.loginForm.value;

      this.authService.login(loginData).subscribe({
        next: (response) => {
          this.isSubmitting.set(false);
          this.snackBar.open(
            `Willkommen zurück, ${response.user.firstName}!`,
            'OK',
            { duration: 3000 }
          );
          this.router.navigate(['/home']);
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Login error:', error);
          
          let errorMessage = 'Anmeldung fehlgeschlagen. Bitte versuche es erneut.';
          if (error.status === 401) {
            errorMessage = 'Ungültige E-Mail oder Passwort.';
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }
          
          this.snackBar.open(errorMessage, 'OK', { duration: 5000 });
        }
      });
    }
  }
}