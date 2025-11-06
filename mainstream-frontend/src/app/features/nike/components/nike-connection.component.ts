import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NikeService, NikeInstructionsResponse } from '../services/nike.service';

@Component({
  selector: 'app-nike-connection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="nike-connection-card">
      <div class="header">
        <div class="title-section">
          <h3>Nike Run Club Integration</h3>
        </div>
        <span class="status-badge" [class.connected]="isConnected" [class.disconnected]="!isConnected">
          {{ isConnected ? 'Connected' : 'Not Connected' }}
        </span>
      </div>

      <div class="content">
        <p class="description">
          Connect your Nike Run Club account to automatically sync your running activities.
        </p>

        <div *ngIf="!isConnected" class="connection-section">
          <div class="instructions-box">
            <h4>{{ instructions?.title || 'How to Get Your Nike Access Token' }}</h4>
            <ol>
              <li *ngFor="let step of instructions?.steps">{{ step }}</li>
            </ol>
            <p class="note"><strong>Note:</strong> {{ instructions?.note }}</p>
          </div>

          <div class="token-input-section">
            <label for="accessToken">Nike Access Token:</label>
            <textarea
              id="accessToken"
              [(ngModel)]="accessToken"
              placeholder="Paste your Nike access token here..."
              rows="4"
              [disabled]="isLoading">
            </textarea>
            <button
              class="btn btn-connect"
              (click)="connectToNike()"
              [disabled]="isLoading || !accessToken?.trim()">
              {{ isLoading ? 'Connecting...' : 'Connect to Nike' }}
            </button>
          </div>
        </div>

        <div *ngIf="isConnected" class="connected-info">
          <p><strong>Nike User ID:</strong> {{ nikeUserId }}</p>
          <p *ngIf="connectedAt"><strong>Connected:</strong> {{ connectedAt | date:'medium' }}</p>

          <div class="actions">
            <button
              class="btn btn-sync"
              (click)="syncActivities()"
              [disabled]="isLoading">
              {{ isLoading ? 'Syncing...' : 'Sync Activities' }}
            </button>
            <button
              class="btn btn-disconnect"
              (click)="disconnectNike()"
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
    .nike-connection-card {
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

    .instructions-box {
      background: #f0f8ff;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 24px;
      border-left: 4px solid #007bff;
    }

    .instructions-box h4 {
      margin-top: 0;
      color: #007bff;
    }

    .instructions-box ol {
      margin: 16px 0;
      padding-left: 20px;
    }

    .instructions-box li {
      margin: 8px 0;
      line-height: 1.5;
    }

    .instructions-box .note {
      margin-top: 16px;
      font-size: 14px;
      color: #666;
      background: #fff;
      padding: 12px;
      border-radius: 4px;
    }

    .token-input-section {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .token-input-section label {
      font-weight: 600;
      color: #333;
    }

    .token-input-section textarea {
      padding: 12px;
      border: 2px solid #ddd;
      border-radius: 8px;
      font-family: monospace;
      font-size: 13px;
      resize: vertical;
      transition: border-color 0.3s ease;
    }

    .token-input-section textarea:focus {
      outline: none;
      border-color: #007bff;
    }

    .token-input-section textarea:disabled {
      background: #f5f5f5;
      cursor: not-allowed;
    }

    .connected-info {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
    }

    .connected-info p {
      margin: 8px 0;
    }

    .actions {
      display: flex;
      gap: 12px;
      margin-top: 20px;
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
      background: #000;
      color: white;
    }

    .btn-connect:hover:not(:disabled) {
      background: #333;
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

    .connection-section {
      margin-top: 20px;
    }
  `]
})
export class NikeConnectionComponent implements OnInit {
  isConnected = false;
  isLoading = false;
  nikeUserId?: string;
  connectedAt?: string;
  accessToken = '';
  instructions?: NikeInstructionsResponse;
  lastSyncResult?: { success: boolean; message: string; syncedCount?: number };
  errorMessage?: string;

  constructor(private nikeService: NikeService) {}

  ngOnInit() {
    this.loadStatus();
    this.loadInstructions();
  }

  loadStatus() {
    this.nikeService.getStatus().subscribe({
      next: (status) => {
        this.isConnected = status.connected;
        this.nikeUserId = status.nikeUserId;
        this.connectedAt = status.connectedAt;
      },
      error: (err) => {
        console.error('Failed to load Nike status:', err);
      }
    });
  }

  loadInstructions() {
    this.nikeService.getInstructions().subscribe({
      next: (instructions) => {
        this.instructions = instructions;
      },
      error: (err) => {
        console.error('Failed to load instructions:', err);
      }
    });
  }

  connectToNike() {
    if (!this.accessToken || !this.accessToken.trim()) {
      this.errorMessage = 'Please enter your Nike access token';
      return;
    }

    this.isLoading = true;
    this.errorMessage = undefined;

    this.nikeService.connectNike(this.accessToken.trim()).subscribe({
      next: (response) => {
        if (response.success) {
          this.isConnected = true;
          this.nikeUserId = response.nikeUserId;
          this.connectedAt = response.connectedAt;
          this.accessToken = ''; // Clear the token from UI
          this.lastSyncResult = {
            success: true,
            message: 'Successfully connected to Nike Run Club!'
          };
        } else {
          this.errorMessage = response.message;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to connect to Nike. Please verify your access token and try again.';
        this.isLoading = false;
      }
    });
  }

  disconnectNike() {
    if (!confirm('Are you sure you want to disconnect your Nike Run Club account?')) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = undefined;

    this.nikeService.disconnectNike().subscribe({
      next: (response) => {
        if (response.success) {
          this.isConnected = false;
          this.nikeUserId = undefined;
          this.connectedAt = undefined;
          this.lastSyncResult = {
            success: true,
            message: 'Successfully disconnected from Nike Run Club'
          };
        } else {
          this.errorMessage = response.message;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to disconnect from Nike. Please try again.';
        this.isLoading = false;
      }
    });
  }

  syncActivities() {
    this.isLoading = true;
    this.errorMessage = undefined;
    this.lastSyncResult = undefined;

    this.nikeService.syncActivities().subscribe({
      next: (response) => {
        this.lastSyncResult = {
          success: response.success,
          message: response.message,
          syncedCount: response.syncedCount
        };
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to sync activities. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
