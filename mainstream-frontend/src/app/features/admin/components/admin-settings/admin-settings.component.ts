import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SettingsService, AppSettings } from '../../services/settings.service';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-settings.component.html',
  styleUrls: ['./admin-settings.component.scss']
})
export class AdminSettingsComponent implements OnInit {
  settings: AppSettings[] = [];
  maintenanceMode: boolean = false;
  loading: boolean = false;
  updateSuccess: boolean = false;
  errorMessage: string = '';

  constructor(private settingsService: SettingsService) {}

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.loading = true;
    this.settingsService.getAllSettings().subscribe({
      next: (settings) => {
        this.settings = settings;
        const maintenanceSetting = settings.find(s => s.key === 'maintenance_mode');
        if (maintenanceSetting) {
          this.maintenanceMode = maintenanceSetting.value === 'true';
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading settings:', error);
        this.errorMessage = 'Fehler beim Laden der Einstellungen';
        this.loading = false;
      }
    });
  }

  toggleMaintenanceMode(): void {
    this.loading = true;
    this.errorMessage = '';
    this.updateSuccess = false;

    const newValue = this.maintenanceMode ? 'true' : 'false';

    this.settingsService.updateSetting('maintenance_mode', newValue).subscribe({
      next: (updatedSetting) => {
        console.log('Maintenance mode updated:', updatedSetting);
        this.updateSuccess = true;
        this.loading = false;

        // Hide success message after 3 seconds
        setTimeout(() => {
          this.updateSuccess = false;
        }, 3000);
      },
      error: (error) => {
        console.error('Error updating maintenance mode:', error);
        this.errorMessage = 'Fehler beim Aktualisieren des Wartungsmodus';
        // Revert the toggle
        this.maintenanceMode = !this.maintenanceMode;
        this.loading = false;
      }
    });
  }
}
