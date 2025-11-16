import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-garmin-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="callback-container">
      <div class="callback-content">
        <h2>Garmin Authorization</h2>
        <p *ngIf="!error">Processing authorization...</p>
        <p *ngIf="error" class="error">{{ error }}</p>
      </div>
    </div>
  `,
  styles: [`
    .callback-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px;
    }

    .callback-content {
      text-align: center;
      max-width: 500px;
    }

    h2 {
      margin-bottom: 20px;
      color: #007bc3;
    }

    .error {
      color: #dc3545;
      margin-top: 10px;
    }
  `]
})
export class GarminCallbackComponent implements OnInit {
  error: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    // The authorization code will be handled by the parent window
    // This page just needs to exist for the redirect
    this.route.queryParams.subscribe(params => {
      if (params['error']) {
        this.error = `Authorization failed: ${params['error']}`;
      }
    });
  }
}
