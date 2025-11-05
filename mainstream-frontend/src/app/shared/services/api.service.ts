import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  get apiUrl(): string {
    return this.baseUrl;
  }
}
