import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../shared/services/api.service';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/users`;
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, user);
  }

  uploadAvatar(userId: number, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<User>(`${this.baseUrl}/${userId}/avatar`, formData);
  }

  deleteAvatar(userId: number): Observable<User> {
    return this.http.delete<User>(`${this.baseUrl}/${userId}/avatar`);
  }
}
