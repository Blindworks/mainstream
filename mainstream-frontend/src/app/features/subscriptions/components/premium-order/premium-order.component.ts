import { Component, signal, OnInit, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SUBSCRIPTION_PLANS, SubscriptionPlan } from '../../models/subscription.model';
import { AuthService } from '../../../users/services/auth.service';

export interface OrderData {
  billingInfo: {
    firstName: string;
    lastName: string;
    email: string;
    street: string;
    city: string;
    postalCode: string;
    country: string;
  };
  paymentMethod: string;
  plan: SubscriptionPlan;
  acceptTerms: boolean;
}

@Component({
  selector: 'app-premium-order',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatRadioModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatStepperModule,
    TranslocoModule
  ],
  templateUrl: './premium-order.component.html',
  styleUrl: './premium-order.component.scss'
})
export class PremiumOrderComponent implements OnInit {
  @Output() orderCompleted = new EventEmitter<OrderData>();
  @Output() orderCancelled = new EventEmitter<void>();

  billingForm!: FormGroup;
  paymentForm!: FormGroup;

  isProcessing = signal(false);
  currentStep = signal(0);
  orderSuccess = signal(false);

  premiumPlan: SubscriptionPlan;

  paymentMethods = [
    { id: 'credit_card', name: 'premiumOrder.payment.creditCard', icon: 'credit_card' },
    { id: 'paypal', name: 'premiumOrder.payment.paypal', icon: 'account_balance_wallet' },
    { id: 'sepa', name: 'premiumOrder.payment.sepa', icon: 'account_balance' }
  ];

  countries = [
    { code: 'DE', name: 'Deutschland' },
    { code: 'AT', name: 'Österreich' },
    { code: 'CH', name: 'Schweiz' }
  ];

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private translocoService: TranslocoService,
    private authService: AuthService
  ) {
    this.premiumPlan = SUBSCRIPTION_PLANS.find(plan => plan.id === 'premium')!;
  }

  ngOnInit(): void {
    this.initializeForms();
  }

  private initializeForms(): void {
    const currentUser = this.authService.currentUser;

    this.billingForm = this.fb.group({
      firstName: [currentUser?.firstName || '', [Validators.required, Validators.minLength(2)]],
      lastName: [currentUser?.lastName || '', [Validators.required, Validators.minLength(2)]],
      email: [currentUser?.email || '', [Validators.required, Validators.email]],
      street: ['', [Validators.required]],
      city: ['', [Validators.required]],
      postalCode: ['', [Validators.required, Validators.pattern(/^\d{4,5}$/)]],
      country: ['DE', [Validators.required]]
    });

    this.paymentForm = this.fb.group({
      paymentMethod: ['credit_card', [Validators.required]],
      acceptTerms: [false, [Validators.requiredTrue]]
    });
  }

  nextStep(): void {
    if (this.currentStep() === 0 && this.billingForm.valid) {
      this.currentStep.set(1);
    } else if (this.currentStep() === 1 && this.paymentForm.valid) {
      this.currentStep.set(2);
    }
  }

  previousStep(): void {
    if (this.currentStep() > 0) {
      this.currentStep.set(this.currentStep() - 1);
    }
  }

  goToStep(step: number): void {
    if (step < this.currentStep()) {
      this.currentStep.set(step);
    }
  }

  async submitOrder(): Promise<void> {
    if (!this.billingForm.valid || !this.paymentForm.valid) {
      this.snackBar.open(
        this.translocoService.translate('premiumOrder.errors.formInvalid'),
        this.translocoService.translate('common.ok'),
        { duration: 3000 }
      );
      return;
    }

    this.isProcessing.set(true);

    const orderData: OrderData = {
      billingInfo: this.billingForm.value,
      paymentMethod: this.paymentForm.get('paymentMethod')?.value,
      plan: this.premiumPlan,
      acceptTerms: this.paymentForm.get('acceptTerms')?.value
    };

    try {
      // Simulate API call - in production, this would call a real payment service
      await this.simulatePaymentProcessing();

      this.orderSuccess.set(true);
      this.snackBar.open(
        this.translocoService.translate('premiumOrder.success.orderCompleted'),
        this.translocoService.translate('common.ok'),
        { duration: 5000 }
      );

      this.orderCompleted.emit(orderData);
    } catch (error) {
      this.snackBar.open(
        this.translocoService.translate('premiumOrder.errors.orderFailed'),
        this.translocoService.translate('common.ok'),
        { duration: 5000, panelClass: ['error-snackbar'] }
      );
    } finally {
      this.isProcessing.set(false);
    }
  }

  private simulatePaymentProcessing(): Promise<void> {
    return new Promise((resolve) => {
      setTimeout(() => resolve(), 2000);
    });
  }

  cancelOrder(): void {
    this.orderCancelled.emit();
  }

  getStepIcon(step: number): string {
    if (step < this.currentStep()) {
      return 'check_circle';
    }
    return step === this.currentStep() ? 'radio_button_checked' : 'radio_button_unchecked';
  }

  getMonthlyPrice(): string {
    return `${this.premiumPlan.currency}${this.premiumPlan.price.toFixed(2)}`;
  }

  getYearlyPrice(): string {
    const yearlyTotal = this.premiumPlan.price * 12;
    return `${this.premiumPlan.currency}${yearlyTotal.toFixed(2)}`;
  }
}
