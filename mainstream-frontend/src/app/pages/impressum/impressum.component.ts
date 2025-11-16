import { Component, inject } from '@angular/core';
import { CommonModule, Location, ViewportScroller } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-impressum',
  standalone: true,
  imports: [CommonModule, TranslocoModule, RouterLink],
  templateUrl: './impressum.component.html',
  styleUrl: './impressum.component.scss'
})
export class ImpressumComponent {
  private location = inject(Location);
  private viewportScroller = inject(ViewportScroller);

  goBack(): void {
    this.location.back();
  }

  scrollToTop(): void {
    this.viewportScroller.scrollToPosition([0, 0]);
  }
}
