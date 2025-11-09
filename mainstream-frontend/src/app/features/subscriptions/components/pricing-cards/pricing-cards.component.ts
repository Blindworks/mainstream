import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { TranslocoModule } from '@jsverse/transloco';
import { SubscriptionPlan } from '../../models/subscription.model';

@Component({
  selector: 'app-pricing-cards',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    TranslocoModule
  ],
  templateUrl: './pricing-cards.component.html',
  styleUrl: './pricing-cards.component.scss'
})
export class PricingCardsComponent {
  @Input() plans: SubscriptionPlan[] = [];
  @Input() currentPlanId?: string;
  @Output() planSelected = new EventEmitter<SubscriptionPlan>();

  onSelectPlan(plan: SubscriptionPlan): void {
    this.planSelected.emit(plan);
  }

  isCurrentPlan(planId: string): boolean {
    return this.currentPlanId === planId;
  }
}
