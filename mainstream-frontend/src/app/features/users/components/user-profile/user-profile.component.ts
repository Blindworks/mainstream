import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StravaConnectionComponent } from '../../../strava/components/strava-connection.component';
import { NikeConnectionComponent } from '../../../nike/components/nike-connection.component';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { ApiService } from '../../../../shared/services/api.service';

@Component({
  selector: 'app-user-profile',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    StravaConnectionComponent,
    NikeConnectionComponent
  ],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent implements OnInit {
  profileForm!: FormGroup;
  currentUser = signal<User | null>(null);
  avatarPreview = signal<string | null>(null);
  selectedFile = signal<File | null>(null);
  isSaving = signal(false);
  isUploadingAvatar = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private apiService: ApiService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser;
    if (user) {
      this.currentUser.set(user);
      // Don't set preview on init, let getAvatarUrl() handle it
    }

    this.profileForm = this.fb.group({
      username: [user?.username || '', [Validators.minLength(3), Validators.maxLength(50)]],
      firstName: [user?.firstName || '', [Validators.required, Validators.maxLength(50)]],
      lastName: [user?.lastName || '', [Validators.required, Validators.maxLength(50)]],
      email: [{ value: user?.email || '', disabled: true }],
      phoneNumber: [user?.phoneNumber || '', Validators.maxLength(20)],
      bio: [user?.bio || ''],
      city: [user?.city || '', Validators.maxLength(100)]
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      // Validate file type
      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
      if (!allowedTypes.includes(file.type)) {
        this.snackBar.open('Bitte wählen Sie ein gültiges Bildformat (JPEG, PNG, GIF, WEBP)', 'Schließen', {
          duration: 5000
        });
        return;
      }

      // Validate file size (5MB max)
      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        this.snackBar.open('Die Datei ist zu groß. Maximale Größe: 5MB', 'Schließen', {
          duration: 5000
        });
        return;
      }

      this.selectedFile.set(file);

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  uploadAvatar(): void {
    const file = this.selectedFile();
    const user = this.currentUser();

    if (!file || !user) return;

    this.isUploadingAvatar.set(true);

    this.userService.uploadAvatar(user.id, file).subscribe({
      next: (updatedUser) => {
        this.currentUser.set(updatedUser);
        this.avatarPreview.set(null); // Clear preview to show the uploaded image from server
        this.selectedFile.set(null);
        this.snackBar.open('Avatar erfolgreich hochgeladen', 'Schließen', { duration: 3000 });
        this.isUploadingAvatar.set(false);

        // Update auth service user
        this.updateAuthServiceUser(updatedUser);
      },
      error: (error) => {
        console.error('Failed to upload avatar:', error);
        this.snackBar.open('Fehler beim Hochladen des Avatars', 'Schließen', { duration: 5000 });
        this.isUploadingAvatar.set(false);
      }
    });
  }

  deleteAvatar(): void {
    const user = this.currentUser();
    if (!user) return;

    this.isUploadingAvatar.set(true);

    this.userService.deleteAvatar(user.id).subscribe({
      next: (updatedUser) => {
        this.currentUser.set(updatedUser);
        this.avatarPreview.set(null);
        this.selectedFile.set(null);
        this.snackBar.open('Avatar erfolgreich gelöscht', 'Schließen', { duration: 3000 });
        this.isUploadingAvatar.set(false);

        // Update auth service user
        this.updateAuthServiceUser(updatedUser);
      },
      error: (error) => {
        console.error('Failed to delete avatar:', error);
        this.snackBar.open('Fehler beim Löschen des Avatars', 'Schließen', { duration: 5000 });
        this.isUploadingAvatar.set(false);
      }
    });
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.snackBar.open('Bitte füllen Sie alle erforderlichen Felder korrekt aus', 'Schließen', {
        duration: 5000
      });
      return;
    }

    const user = this.currentUser();
    if (!user) return;

    this.isSaving.set(true);

    const formValue = this.profileForm.value;
    const updatedData: Partial<User> = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      phoneNumber: formValue.phoneNumber,
      bio: formValue.bio,
      city: formValue.city,
      email: user.email // Keep original email
    };

    // Only include username if it's not empty
    if (formValue.username && formValue.username.trim() !== '') {
      updatedData.username = formValue.username.trim();
    }

    this.userService.updateUser(user.id, updatedData).subscribe({
      next: (updatedUser) => {
        this.currentUser.set(updatedUser);
        this.snackBar.open('Profil erfolgreich aktualisiert', 'Schließen', { duration: 3000 });
        this.isSaving.set(false);

        // Update auth service user
        this.updateAuthServiceUser(updatedUser);
      },
      error: (error) => {
        console.error('Failed to update profile:', error);
        this.snackBar.open('Fehler beim Aktualisieren des Profils', 'Schließen', { duration: 5000 });
        this.isSaving.set(false);
      }
    });
  }

  private updateAuthServiceUser(user: User): void {
    // Update user in localStorage
    localStorage.setItem('mainstream_user', JSON.stringify(user));
  }

  getAvatarUrl(): string {
    const preview = this.avatarPreview();
    const user = this.currentUser();

    if (preview) return preview;

    if (user?.profilePictureUrl) {
      // If it's a relative path (uploaded file), prepend backend URL
      if (user.profilePictureUrl.startsWith('/uploads/')) {
        return `${this.apiService.apiUrl}${user.profilePictureUrl}`;
      }
      // Otherwise it's an external URL (like ui-avatars.com)
      return user.profilePictureUrl;
    }

    // Default avatar
    return `https://ui-avatars.com/api/?name=${user?.firstName}+${user?.lastName}&background=9c27b0&color=fff&size=128`;
  }
}
