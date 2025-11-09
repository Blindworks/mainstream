import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { AuthService } from '../../../features/users/services/auth.service';
import { User, AuthState } from '../../../features/users/models/user.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    RouterModule,
    TranslocoModule
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit, OnDestroy {
  authState: AuthState = {
    isAuthenticated: false,
    user: null,
    token: null
  };

  currentLang: string = 'de';
  private authSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private translocoService: TranslocoService
  ) {}

  ngOnInit(): void {
    this.authSubscription = this.authService.authState$.subscribe(state => {
      this.authState = state;
    });

    // Get current language
    this.currentLang = this.translocoService.getActiveLang();
  }

  ngOnDestroy(): void {
    this.authSubscription?.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
  }

  switchLanguage(lang: string): void {
    this.translocoService.setActiveLang(lang);
    this.currentLang = lang;
  }

  getLanguageLabel(lang: string): string {
    return lang === 'de' ? 'Deutsch' : 'English';
  }
}
