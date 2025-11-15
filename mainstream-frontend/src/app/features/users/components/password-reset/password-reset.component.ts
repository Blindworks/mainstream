import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-password-reset',
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
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss']
})
export class PasswordResetComponent implements OnInit {
  requestForm: FormGroup;
  resetForm: FormGroup;
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);
  isSubmitting = signal(false);
  isResetMode = signal(false);
  emailSent = signal(false);
  resetToken: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private translocoService: TranslocoService
  ) {
    this.requestForm = this.createRequestForm();
    this.resetForm = this.createResetForm();
  }

  ngOnInit(): void {
    // Check if there's a reset token in the URL
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        this.resetToken = params['token'];
        this.isResetMode.set(true);
      }
    });
  }

  private createRequestForm(): FormGroup {
    return this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  private createResetForm(): FormGroup {
    return this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  private passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;

    if (password && confirmPassword && password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update(value => !value);
  }

  toggleConfirmPasswordVisibility(): void {
    this.hideConfirmPassword.update(value => !value);
  }

  onRequestSubmit(): void {
    if (this.requestForm.valid) {
      this.isSubmitting.set(true);

      const email = this.requestForm.value.email;

      this.authService.requestPasswordReset(email).subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.emailSent.set(true);

          const message = this.translocoService.translate('auth.passwordReset.emailSent');
          this.snackBar.open(
            message,
            this.translocoService.translate('common.ok'),
            { duration: 5000 }
          );
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Password reset request error:', error);

          let errorMessage = this.translocoService.translate('auth.passwordReset.requestFailed');
          if (error.error?.message) {
            errorMessage = error.error.message;
          }

          this.snackBar.open(
            errorMessage,
            this.translocoService.translate('common.ok'),
            { duration: 5000 }
          );
        }
      });
    }
  }

  onResetSubmit(): void {
    if (this.resetForm.valid && this.resetToken) {
      this.isSubmitting.set(true);

      const newPassword = this.resetForm.value.password;

      this.authService.resetPassword(this.resetToken, newPassword).subscribe({
        next: () => {
          this.isSubmitting.set(false);

          const message = this.translocoService.translate('auth.passwordReset.resetSuccess');
          this.snackBar.open(
            message,
            this.translocoService.translate('common.ok'),
            { duration: 5000 }
          );

          // Redirect to login after 2 seconds
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Password reset error:', error);

          let errorMessage = this.translocoService.translate('auth.passwordReset.resetFailed');
          if (error.status === 400 || error.status === 404) {
            errorMessage = this.translocoService.translate('auth.passwordReset.invalidToken');
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }

          this.snackBar.open(
            errorMessage,
            this.translocoService.translate('common.ok'),
            { duration: 5000 }
          );
        }
      });
    }
  }

  backToLogin(): void {
    this.router.navigate(['/auth/login']);
  }
}
