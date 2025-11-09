import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../shared/services/api.service';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {}

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiService.getBaseUrl()}/users/${id}`, {
      headers: this.apiService.getAuthHeaders()
    });
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(
      `${this.apiService.getBaseUrl()}/users/${id}`,
      user,
      { headers: this.apiService.getAuthHeaders() }
    );
  }

  uploadAvatar(userId: number, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);

    // Get auth headers without Content-Type (browser will set it with boundary for multipart)
    const headers = this.apiService.getAuthHeaders();
    headers.delete('Content-Type');

    return this.http.post<User>(
      `${this.apiService.getBaseUrl()}/users/${userId}/avatar`,
      formData,
      { headers }
    );
  }

  deleteAvatar(userId: number): Observable<User> {
    return this.http.delete<User>(
      `${this.apiService.getBaseUrl()}/users/${userId}/avatar`,
      { headers: this.apiService.getAuthHeaders() }
    );
  }
}
