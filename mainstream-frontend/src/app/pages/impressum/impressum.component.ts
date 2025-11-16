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

  currentDate = this.formatDate(new Date());

  private formatDate(date: Date): string {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }

  goBack(): void {
    this.location.back();
  }

  scrollToTop(): void {
    this.viewportScroller.scrollToPosition([0, 0]);
  }
}
