import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GarminService } from '../services/garmin.service';

@Component({
  selector: 'app-garmin-connection',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="garmin-connection-card">
      <div class="header">
        <div class="title-section">
          <img src="assets/garmin-icon.png" alt="Garmin" class="garmin-icon" onerror="this.style.display='none'">
          <h3>Garmin Connect Integration</h3>
        </div>
        <span class="status-badge" [class.connected]="isConnected" [class.disconnected]="!isConnected">
          {{ isConnected ? 'Connected' : 'Not Connected' }}
        </span>
      </div>

      <div class="content">
        <p class="description">
          Connect your Garmin Connect account to automatically sync your running activities.
        </p>

        <div *ngIf="isConnected" class="connected-info">
          <p><strong>Garmin User ID:</strong> {{ garminUserId }}</p>
          <p *ngIf="connectedAt"><strong>Connected:</strong> {{ connectedAt | date:'medium' }}</p>
        </div>

        <div class="actions">
          <button
            *ngIf="!isConnected"
            class="btn btn-connect"
            (click)="connectToGarmin()"
            [disabled]="isLoading">
            {{ isLoading ? 'Connecting...' : 'Connect to Garmin' }}
          </button>

          <div *ngIf="isConnected" class="connected-actions">
            <button
              class="btn btn-sync"
              (click)="syncActivities()"
              [disabled]="isLoading">
              {{ isLoading ? 'Syncing...' : 'Sync Activities' }}
            </button>
            <button
              class="btn btn-disconnect"
              (click)="disconnectGarmin()"
              [disabled]="isLoading">
              Disconnect
            </button>
          </div>
        </div>

        <div *ngIf="lastSyncResult" class="sync-result" [class.success]="lastSyncResult.success" [class.error]="!lastSyncResult.success">
          <p>{{ lastSyncResult.message }}</p>
          <p *ngIf="lastSyncResult.syncedCount !== undefined">
            <strong>{{ lastSyncResult.syncedCount }}</strong> activities synced
          </p>
        </div>

        <div *ngIf="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </div>
    </div>
  `,
  styles: [`
    .garmin-connection-card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      padding: 24px;
      margin: 20px 0;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #e0e0e0;
    }

    .title-section {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .garmin-icon {
      width: 32px;
      height: 32px;
    }

    h3 {
      margin: 0;
      color: #333;
      font-size: 20px;
    }

    .status-badge {
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      text-transform: uppercase;
    }

    .status-badge.connected {
      background: #d4edda;
      color: #155724;
    }

    .status-badge.disconnected {
      background: #f8d7da;
      color: #721c24;
    }

    .content {
      color: #666;
    }

    .description {
      margin-bottom: 20px;
      line-height: 1.6;
    }

    .connected-info {
      background: #f8f9fa;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .connected-info p {
      margin: 8px 0;
    }

    .actions {
      display: flex;
      gap: 12px;
      margin-top: 20px;
    }

    .connected-actions {
      display: flex;
      gap: 12px;
      flex: 1;
    }

    .btn {
      padding: 12px 24px;
      border: none;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      flex: 1;
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-connect {
      background: #007bc3;
      color: white;
    }

    .btn-connect:hover:not(:disabled) {
      background: #0066a3;
    }

    .btn-sync {
      background: #28a745;
      color: white;
    }

    .btn-sync:hover:not(:disabled) {
      background: #218838;
    }

    .btn-disconnect {
      background: #dc3545;
      color: white;
    }

    .btn-disconnect:hover:not(:disabled) {
      background: #c82333;
    }

    .sync-result {
      margin-top: 16px;
      padding: 12px;
      border-radius: 8px;
    }

    .sync-result.success {
      background: #d4edda;
      color: #155724;
      border: 1px solid #c3e6cb;
    }

    .sync-result.error {
      background: #f8d7da;
      color: #721c24;
      border: 1px solid #f5c6cb;
    }

    .sync-result p {
      margin: 4px 0;
    }

    .error-message {
      margin-top: 16px;
      padding: 12px;
      background: #f8d7da;
      color: #721c24;
      border: 1px solid #f5c6cb;
      border-radius: 8px;
    }
  `]
})
export class GarminConnectionComponent implements OnInit {
  isConnected = false;
  isLoading = false;
  garminUserId?: string;
  connectedAt?: string;
  lastSyncResult?: { success: boolean; message: string; syncedCount?: number };
  errorMessage?: string;

  constructor(private garminService: GarminService) {}

  ngOnInit() {
    this.loadStatus();
  }

  loadStatus() {
    this.garminService.getStatus().subscribe({
      next: (status) => {
        this.isConnected = status.connected;
        this.garminUserId = status.garminUserId;
        this.connectedAt = status.connectedAt;
      },
      error: (err) => {
        console.error('Failed to load Garmin status:', err);
      }
    });
  }

  async connectToGarmin() {
    this.isLoading = true;
    this.errorMessage = undefined;

    try {
      const code = await this.garminService.openGarminAuth();
      if (code) {
        this.garminService.connectGarmin(code).subscribe({
          next: (response) => {
            if (response.success) {
              this.isConnected = true;
              this.garminUserId = response.garminUserId;
              this.connectedAt = response.connectedAt;
              this.lastSyncResult = {
                success: true,
                message: 'Successfully connected to Garmin!'
              };
            } else {
              this.errorMessage = response.message;
            }
            this.isLoading = false;
          },
          error: (err) => {
            this.errorMessage = 'Failed to connect to Garmin. Please try again.';
            this.isLoading = false;
          }
        });
      }
    } catch (error: any) {
      this.errorMessage = error.message || 'Failed to authorize with Garmin';
      this.isLoading = false;
    }
  }

  disconnectGarmin() {
    if (!confirm('Are you sure you want to disconnect your Garmin account?')) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = undefined;

    this.garminService.disconnectGarmin().subscribe({
      next: (response) => {
        if (response.success) {
          this.isConnected = false;
          this.garminUserId = undefined;
          this.connectedAt = undefined;
          this.lastSyncResult = {
            success: true,
            message: 'Successfully disconnected from Garmin'
          };
        } else {
          this.errorMessage = response.message;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to disconnect from Garmin. Please try again.';
        this.isLoading = false;
      }
    });
  }

  syncActivities() {
    this.isLoading = true;
    this.errorMessage = undefined;
    this.lastSyncResult = undefined;

    // Sync activities from the last 30 days
    const since = new Date();
    since.setDate(since.getDate() - 30);

    this.garminService.syncActivities(since).subscribe({
      next: (response) => {
        this.lastSyncResult = {
          success: response.success,
          message: response.message,
          syncedCount: response.syncedCount
        };
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to sync activities. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
