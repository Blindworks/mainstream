import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PricingCardsComponent } from '../../features/subscriptions/components/pricing-cards/pricing-cards.component';
import { PremiumOrderComponent, OrderData } from '../../features/subscriptions/components/premium-order/premium-order.component';
import { SubscriptionPlan, SUBSCRIPTION_PLANS } from '../../features/subscriptions/models/subscription.model';

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    PricingCardsComponent,
    PremiumOrderComponent
  ],
  templateUrl: './subscriptions.component.html',
  styleUrl: './subscriptions.component.scss'
})
export class SubscriptionsComponent implements OnInit {
  plans: SubscriptionPlan[] = SUBSCRIPTION_PLANS;
  currentPlanId: string = 'free'; // Default to free plan
  showOrderForm = signal(false);

  constructor(private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    // TODO: Load user's current subscription from backend
    // For now, we default to 'free'
  }

  onPlanSelected(plan: SubscriptionPlan): void {
    if (plan.id === 'free') {
      // Handle free plan selection
      this.snackBar.open('Sie nutzen bereits den kostenlosen Plan', 'OK', {
        duration: 3000
      });
    } else if (plan.id === 'premium') {
      this.showOrderForm.set(true);
    }
  }

  onOrderCompleted(orderData: OrderData): void {
    // Handle successful order
    this.currentPlanId = 'premium';
    this.showOrderForm.set(false);
    // TODO: Save subscription to backend
  }

  onOrderCancelled(): void {
    this.showOrderForm.set(false);
  }
}
