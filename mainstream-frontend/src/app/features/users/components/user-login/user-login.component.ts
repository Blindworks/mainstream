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
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

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
    MatProgressSpinnerModule,
    TranslocoModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>{{ 'auth.login.title' | transloco }}</mat-card-title>
          <mat-card-subtitle>{{ 'auth.login.subtitle' | transloco }}</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.login.email' | transloco }}</mat-label>
              <input matInput type="email" formControlName="email" required>
              <mat-icon matSuffix>email</mat-icon>
              @if (loginForm.get('email')?.hasError('required') && loginForm.get('email')?.touched) {
                <mat-error>{{ 'auth.login.emailRequired' | transloco }}</mat-error>
              }
              @if (loginForm.get('email')?.hasError('email') && loginForm.get('email')?.touched) {
                <mat-error>{{ 'auth.login.emailInvalid' | transloco }}</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.login.password' | transloco }}</mat-label>
              <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" required>
              <button type="button" mat-icon-button matSuffix (click)="togglePasswordVisibility()">
                <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
              </button>
              @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                <mat-error>{{ 'auth.login.passwordRequired' | transloco }}</mat-error>
              }
            </mat-form-field>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit"
                      [disabled]="loginForm.invalid || isSubmitting()"
                      class="submit-button">
                @if (isSubmitting()) {
                  <mat-spinner diameter="20" class="submit-spinner"></mat-spinner>
                  {{ 'auth.login.loggingIn' | transloco }}
                } @else {
                  <ng-container>
                    <mat-icon>login</mat-icon>
                    {{ 'auth.login.loginButton' | transloco }}
                  </ng-container>
                }
              </button>

              <div class="additional-actions">
                <div class="register-link">
                  {{ 'auth.login.noAccount' | transloco }}
                  <a routerLink="/auth/register" mat-button color="accent">{{ 'auth.login.registerNow' | transloco }}</a>
                </div>

                <div class="forgot-password">
                  <a routerLink="/auth/forgot-password" mat-button>{{ 'auth.login.forgotPassword' | transloco }}</a>
                </div>
              </div>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <!-- Demo Credentials Card -->
      <mat-card class="demo-card">
        <mat-card-header>
          <mat-card-title>{{ 'auth.login.demoAccount' | transloco }}</mat-card-title>
          <mat-card-subtitle>{{ 'auth.login.demoSubtitle' | transloco }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p><strong>{{ 'auth.login.email' | transloco }}:</strong> demo&#64;mainstream.com</p>
          <p><strong>{{ 'auth.login.password' | transloco }}:</strong> demo123</p>
          <button mat-button color="accent" (click)="fillDemoCredentials()">
            <mat-icon>account_circle</mat-icon>
            {{ 'auth.login.useDemoData' | transloco }}
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
      background: linear-gradient(135deg, #0077BE 0%, #005a8f 50%, #003d5c 100%);
      gap: 20px;
    }

    .login-card {
      width: 100%;
      max-width: 400px;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    }

    .demo-card {
      width: 100%;
      max-width: 300px;
      background-color: rgba(255, 255, 255, 0.95);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
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
    private snackBar: MatSnackBar,
    private translocoService: TranslocoService
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
          const message = this.translocoService.translate('auth.login.welcomeBack', { name: response.user.firstName });
          this.snackBar.open(
            message,
            this.translocoService.translate('common.ok'),
            { duration: 3000 }
          );
          this.router.navigate(['/home']);
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Login error:', error);

          let errorMessage = this.translocoService.translate('auth.login.loginFailed');
          if (error.status === 401) {
            errorMessage = this.translocoService.translate('auth.login.invalidCredentials');
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }

          this.snackBar.open(errorMessage, this.translocoService.translate('common.ok'), { duration: 5000 });
        }
      });
    }
  }
}