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
      <div class="login-wrapper">
        <!-- Branding Section -->
        <div class="branding">
          <div class="brand-icon">
            <mat-icon>waves</mat-icon>
          </div>
          <h1 class="brand-title">Mainstream</h1>
        </div>

        <!-- Login Card -->
        <mat-card class="login-card">
          <mat-card-header class="card-header">
            <mat-card-title class="card-title">{{ 'auth.login.title' | transloco }}</mat-card-title>
            <mat-card-subtitle class="card-subtitle">{{ 'auth.login.subtitle' | transloco }}</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content class="card-content">
            <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">

              <!-- Email Field -->
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>{{ 'auth.login.email' | transloco }}</mat-label>
                <input
                  matInput
                  type="email"
                  formControlName="email"
                  required
                  [attr.aria-label]="'auth.login.email' | transloco"
                  autocomplete="email">
                <mat-icon matSuffix aria-hidden="true">email</mat-icon>
                @if (loginForm.get('email')?.hasError('required') && loginForm.get('email')?.touched) {
                  <mat-error>{{ 'auth.login.emailRequired' | transloco }}</mat-error>
                }
                @if (loginForm.get('email')?.hasError('email') && loginForm.get('email')?.touched) {
                  <mat-error>{{ 'auth.login.emailInvalid' | transloco }}</mat-error>
                }
              </mat-form-field>

              <!-- Password Field -->
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>{{ 'auth.login.password' | transloco }}</mat-label>
                <input
                  matInput
                  [type]="hidePassword() ? 'password' : 'text'"
                  formControlName="password"
                  required
                  [attr.aria-label]="'auth.login.password' | transloco"
                  autocomplete="current-password">
                <button
                  type="button"
                  mat-icon-button
                  matSuffix
                  (click)="togglePasswordVisibility()"
                  [attr.aria-label]="hidePassword() ? 'Show password' : 'Hide password'"
                  class="password-toggle">
                  <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
                </button>
                @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                  <mat-error>{{ 'auth.login.passwordRequired' | transloco }}</mat-error>
                }
              </mat-form-field>

              <!-- Forgot Password Link -->
              <div class="forgot-password-link">
                <a routerLink="/auth/forgot-password" mat-button color="primary" class="link-button">
                  {{ 'auth.login.forgotPassword' | transloco }}
                </a>
              </div>

              <!-- Submit Button -->
              <button
                mat-raised-button
                color="primary"
                type="submit"
                [disabled]="loginForm.invalid || isSubmitting()"
                class="submit-button"
                [attr.aria-busy]="isSubmitting()">
                @if (isSubmitting()) {
                  <mat-spinner diameter="20" class="submit-spinner" aria-hidden="true"></mat-spinner>
                  <span>{{ 'auth.login.loggingIn' | transloco }}</span>
                } @else {
                  <span>{{ 'auth.login.loginButton' | transloco }}</span>
                }
              </button>

              <!-- Divider -->
              <div class="divider">
                <span class="divider-text">{{ 'common.or' | transloco }}</span>
              </div>

              <!-- Register Link -->
              <div class="register-section">
                <span class="register-text">{{ 'auth.login.noAccount' | transloco }}</span>
                <a routerLink="/auth/register" mat-stroked-button color="primary" class="register-button">
                  {{ 'auth.login.registerNow' | transloco }}
                </a>
              </div>
            </form>

            <!-- Demo Credentials Section -->
            <div class="demo-section">
              <div class="demo-header">
                <mat-icon class="demo-icon" aria-hidden="true">info</mat-icon>
                <span class="demo-title">{{ 'auth.login.demoAccount' | transloco }}</span>
              </div>
              <p class="demo-subtitle">{{ 'auth.login.demoSubtitle' | transloco }}</p>
              <div class="demo-credentials">
                <div class="credential-item">
                  <span class="credential-label">{{ 'auth.login.email' | transloco }}:</span>
                  <code class="credential-value">demo&#64;mainstream.com</code>
                </div>
                <div class="credential-item">
                  <span class="credential-label">{{ 'auth.login.password' | transloco }}:</span>
                  <code class="credential-value">demo123</code>
                </div>
              </div>
              <button
                mat-button
                color="accent"
                (click)="fillDemoCredentials()"
                class="demo-fill-button"
                aria-label="Fill form with demo credentials">
                <mat-icon aria-hidden="true">arrow_upward</mat-icon>
                <span>{{ 'auth.login.useDemoData' | transloco }}</span>
              </button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    /* ===== Container & Layout ===== */
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 24px;
      background: linear-gradient(135deg, #0077BE 0%, #005a8f 50%, #003d5c 100%);
      position: relative;
      overflow-y: auto;
    }

    .login-container::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
                  radial-gradient(circle at 80% 80%, rgba(0, 119, 190, 0.3) 0%, transparent 50%);
      pointer-events: none;
    }

    .login-wrapper {
      width: 100%;
      max-width: 480px;
      position: relative;
      z-index: 1;
      display: flex;
      flex-direction: column;
      gap: 32px;
    }

    /* ===== Branding ===== */
    .branding {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      animation: fadeInDown 0.6s ease-out;
    }

    .brand-icon {
      width: 64px;
      height: 64px;
      background: rgba(255, 255, 255, 0.95);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15),
                  0 0 0 1px rgba(255, 255, 255, 0.1);
      transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .brand-icon:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2),
                  0 0 0 1px rgba(255, 255, 255, 0.1);
    }

    .brand-icon mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      color: #0077BE;
    }

    .brand-title {
      margin: 0;
      font-size: 32px;
      font-weight: 600;
      color: white;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      letter-spacing: -0.5px;
    }

    /* ===== Card ===== */
    .login-card {
      background: rgba(255, 255, 255, 0.98) !important;
      backdrop-filter: blur(10px);
      border-radius: 16px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
                  0 0 0 1px rgba(255, 255, 255, 0.1);
      overflow: hidden;
      animation: fadeInUp 0.6s ease-out 0.2s both;
    }

    .card-header {
      padding: 32px 32px 24px;
      background: linear-gradient(135deg, rgba(0, 119, 190, 0.05) 0%, rgba(255, 255, 255, 0) 100%);
    }

    .card-title {
      font-size: 24px;
      font-weight: 600;
      color: #1a1a1a !important;
      margin: 0;
      letter-spacing: -0.3px;
    }

    .card-subtitle {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.6) !important;
      margin: 8px 0 0;
      line-height: 1.5;
    }

    .card-content {
      padding: 0 32px 32px;
      color: #1a1a1a !important;
    }

    /* ===== Form ===== */
    .login-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .full-width {
      width: 100%;
    }

    /* ===== Form Fields ===== */
    ::ng-deep .login-form .mat-mdc-form-field {
      margin-bottom: 4px;
    }

    ::ng-deep .login-form .mat-mdc-text-field-wrapper {
      padding-bottom: 0;
    }

    /* Force light colors for form fields */
    ::ng-deep .login-card .mat-mdc-form-field {
      .mat-mdc-input-element {
        color: #1a1a1a !important;
        caret-color: #0077BE !important;
      }

      .mat-mdc-floating-label,
      .mdc-floating-label {
        color: rgba(0, 0, 0, 0.6) !important;
      }

      .mat-mdc-form-field-error {
        color: #EF4444 !important;
      }

      .mdc-notched-outline__leading,
      .mdc-notched-outline__notch,
      .mdc-notched-outline__trailing {
        border-color: rgba(0, 0, 0, 0.38) !important;
      }

      &.mat-focused {
        .mdc-notched-outline__leading,
        .mdc-notched-outline__notch,
        .mdc-notched-outline__trailing {
          border-color: #0077BE !important;
        }

        .mat-mdc-floating-label,
        .mdc-floating-label {
          color: #0077BE !important;
        }
      }
    }

    .password-toggle {
      transition: color 0.2s ease;
      color: rgba(0, 0, 0, 0.54) !important;
    }

    .password-toggle:hover {
      color: #0077BE !important;
    }

    /* ===== Forgot Password Link ===== */
    .forgot-password-link {
      display: flex;
      justify-content: flex-end;
      margin-top: -8px;
      margin-bottom: 8px;
    }

    .link-button {
      font-size: 13px;
      height: auto;
      line-height: 1;
      padding: 4px 8px;
      min-width: auto;
    }

    /* ===== Submit Button ===== */
    .submit-button {
      height: 48px;
      font-size: 15px;
      font-weight: 500;
      border-radius: 8px;
      margin-top: 8px;
      transition: all 0.3s ease;
      box-shadow: 0 2px 8px rgba(0, 119, 190, 0.3);
    }

    .submit-button:not(:disabled):hover {
      box-shadow: 0 4px 12px rgba(0, 119, 190, 0.4);
      transform: translateY(-1px);
    }

    .submit-button:disabled {
      opacity: 0.6;
    }

    .submit-button span {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .submit-spinner {
      display: inline-block;
    }

    /* ===== Divider ===== */
    .divider {
      position: relative;
      text-align: center;
      margin: 24px 0 20px;
    }

    .divider::before {
      content: '';
      position: absolute;
      left: 0;
      right: 0;
      top: 50%;
      height: 1px;
      background: linear-gradient(90deg, transparent 0%, rgba(0, 0, 0, 0.12) 50%, transparent 100%);
    }

    .divider-text {
      position: relative;
      display: inline-block;
      padding: 0 16px;
      background: rgba(255, 255, 255, 0.98);
      color: rgba(0, 0, 0, 0.5);
      font-size: 13px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    /* ===== Register Section ===== */
    .register-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: rgba(0, 119, 190, 0.03);
      border-radius: 8px;
      border: 1px solid rgba(0, 119, 190, 0.1);
    }

    .register-text {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.7) !important;
    }

    .register-button {
      height: 40px;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      min-width: 160px;
      transition: all 0.2s ease;
    }

    .register-button:hover {
      background: rgba(0, 119, 190, 0.04);
      transform: translateY(-1px);
    }

    /* ===== Demo Section ===== */
    .demo-section {
      margin-top: 24px;
      padding: 20px;
      background: linear-gradient(135deg, rgba(255, 193, 7, 0.08) 0%, rgba(255, 152, 0, 0.08) 100%);
      border-radius: 12px;
      border: 1px solid rgba(255, 193, 7, 0.2);
    }

    .demo-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;
    }

    .demo-icon {
      color: #f57c00;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .demo-title {
      font-size: 15px;
      font-weight: 600;
      color: #e65100 !important;
      letter-spacing: -0.2px;
    }

    .demo-subtitle {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.6) !important;
      margin: 0 0 16px;
      line-height: 1.5;
    }

    .demo-credentials {
      display: flex;
      flex-direction: column;
      gap: 10px;
      margin-bottom: 16px;
    }

    .credential-item {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .credential-label {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.7) !important;
      font-weight: 500;
      min-width: 70px;
    }

    .credential-value {
      font-family: 'Roboto Mono', 'Courier New', monospace;
      font-size: 13px;
      background: rgba(255, 255, 255, 0.7) !important;
      padding: 6px 12px;
      border-radius: 6px;
      border: 1px solid rgba(255, 152, 0, 0.2);
      color: #e65100 !important;
      font-weight: 500;
      user-select: all;
      transition: all 0.2s ease;
    }

    .credential-value:hover {
      background: rgba(255, 255, 255, 0.9);
      border-color: rgba(255, 152, 0, 0.4);
    }

    .demo-fill-button {
      width: 100%;
      height: 40px;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: all 0.2s ease;
    }

    .demo-fill-button:hover {
      background: rgba(255, 193, 7, 0.12);
    }

    /* ===== Animations ===== */
    @keyframes fadeInDown {
      from {
        opacity: 0;
        transform: translateY(-20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    /* ===== Responsive Design ===== */
    @media (max-width: 640px) {
      .login-container {
        padding: 16px;
      }

      .login-wrapper {
        gap: 24px;
      }

      .branding {
        gap: 12px;
      }

      .brand-icon {
        width: 56px;
        height: 56px;
      }

      .brand-icon mat-icon {
        font-size: 36px;
        width: 36px;
        height: 36px;
      }

      .brand-title {
        font-size: 28px;
      }

      .card-header {
        padding: 24px 24px 20px;
      }

      .card-title {
        font-size: 22px;
      }

      .card-subtitle {
        font-size: 13px;
      }

      .card-content {
        padding: 0 24px 24px;
      }

      .login-form {
        gap: 14px;
      }

      .submit-button {
        height: 44px;
        font-size: 14px;
      }

      .demo-section {
        padding: 16px;
      }

      .credential-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 6px;
      }

      .credential-label {
        min-width: auto;
      }

      .credential-value {
        width: 100%;
        text-align: center;
      }
    }

    @media (max-width: 400px) {
      .login-container {
        padding: 12px;
      }

      .card-header {
        padding: 20px 20px 16px;
      }

      .card-content {
        padding: 0 20px 20px;
      }

      .register-section {
        padding: 12px;
      }
    }

    /* ===== Focus Styles ===== */
    :focus-visible {
      outline: 2px solid #0077BE;
      outline-offset: 2px;
    }

    /* ===== Print Styles ===== */
    @media print {
      .login-container {
        background: white;
      }

      .demo-section {
        display: none;
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