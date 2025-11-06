import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PersonalStatsComponent } from './personal-stats/personal-stats.component';
import { CommunityMapComponent } from './community-map/community-map.component';

/**
 * LandingPageComponent
 *
 * Main container component for the landing page featuring a split-screen layout:
 * - Left (35%): Personal running statistics
 * - Right (65%): Interactive community map with route visualization
 *
 * Responsive Design:
 * - Desktop: Side-by-side layout
 * - Mobile (< 768px): Stacked vertically with community map on top
 *
 * This component serves as a dashboard view showing both personal progress
 * and community activity, encouraging engagement and motivation.
 */
@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [
    CommonModule,
    PersonalStatsComponent,
    CommunityMapComponent
  ],
  templateUrl: './landing-page.component.html',
  styleUrl: './landing-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LandingPageComponent {
  constructor() {}
}
