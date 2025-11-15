import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { PasswordResetComponent } from './password-reset.component';
import { AuthService } from '../../services/auth.service';
import { TranslocoService } from '@jsverse/transloco';

describe('PasswordResetComponent', () => {
  let component: PasswordResetComponent;
  let fixture: ComponentFixture<PasswordResetComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let translocoService: jasmine.SpyObj<TranslocoService>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['requestPasswordReset', 'resetPassword']);
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);
    const translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);

    await TestBed.configureTestingModule({
      imports: [
        PasswordResetComponent,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: TranslocoService, useValue: translocoServiceSpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;
    translocoService = TestBed.inject(TranslocoService) as jasmine.SpyObj<TranslocoService>;

    fixture = TestBed.createComponent(PasswordResetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize request form with email field', () => {
    expect(component.requestForm.get('email')).toBeTruthy();
  });

  it('should initialize reset form with password and confirmPassword fields', () => {
    expect(component.resetForm.get('password')).toBeTruthy();
    expect(component.resetForm.get('confirmPassword')).toBeTruthy();
  });

  it('should validate email format in request form', () => {
    const emailControl = component.requestForm.get('email');

    emailControl?.setValue('invalid-email');
    expect(emailControl?.hasError('email')).toBeTruthy();

    emailControl?.setValue('valid@email.com');
    expect(emailControl?.hasError('email')).toBeFalsy();
  });

  it('should validate password match in reset form', () => {
    const passwordControl = component.resetForm.get('password');
    const confirmPasswordControl = component.resetForm.get('confirmPassword');

    passwordControl?.setValue('password123');
    confirmPasswordControl?.setValue('different123');

    expect(component.resetForm.hasError('passwordMismatch')).toBeTruthy();

    confirmPasswordControl?.setValue('password123');
    expect(component.resetForm.hasError('passwordMismatch')).toBeFalsy();
  });

  it('should call authService.requestPasswordReset on valid request form submission', () => {
    authService.requestPasswordReset.and.returnValue(of(void 0));
    translocoService.translate.and.returnValue('Success');

    component.requestForm.patchValue({ email: 'test@example.com' });
    component.onRequestSubmit();

    expect(authService.requestPasswordReset).toHaveBeenCalledWith('test@example.com');
    expect(component.emailSent()).toBeTruthy();
  });

  it('should handle error on request form submission', () => {
    authService.requestPasswordReset.and.returnValue(throwError(() => ({ error: { message: 'Error' } })));
    translocoService.translate.and.returnValue('Error message');

    component.requestForm.patchValue({ email: 'test@example.com' });
    component.onRequestSubmit();

    expect(snackBar.open).toHaveBeenCalled();
    expect(component.isSubmitting()).toBeFalsy();
  });

  it('should call authService.resetPassword on valid reset form submission', () => {
    authService.resetPassword.and.returnValue(of(void 0));
    translocoService.translate.and.returnValue('Success');
    component.resetToken = 'test-token';
    component.isResetMode.set(true);

    component.resetForm.patchValue({
      password: 'newpassword123',
      confirmPassword: 'newpassword123'
    });
    component.onResetSubmit();

    expect(authService.resetPassword).toHaveBeenCalledWith('test-token', 'newpassword123');
  });

  it('should toggle password visibility', () => {
    const initialValue = component.hidePassword();
    component.togglePasswordVisibility();
    expect(component.hidePassword()).toBe(!initialValue);
  });

  it('should toggle confirm password visibility', () => {
    const initialValue = component.hideConfirmPassword();
    component.toggleConfirmPasswordVisibility();
    expect(component.hideConfirmPassword()).toBe(!initialValue);
  });
});
