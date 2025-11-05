import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  Competition,
  CompetitionSummary,
  CompetitionParticipant,
  LeaderboardEntry,
  CompetitionStatus
} from '../models/competition.model';
import { ApiService } from '../../../shared/services/api.service';

@Injectable({
  providedIn: 'root'
})
export class CompetitionService {
  private baseUrl: string;
  private selectedCompetitionSubject = new BehaviorSubject<Competition | null>(null);
  public selectedCompetition$ = this.selectedCompetitionSubject.asObservable();

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/competitions`;
  }

  /**
   * Get all competitions with pagination
   */
  getAllCompetitions(page: number = 0, size: number = 20): Observable<{content: CompetitionSummary[], totalElements: number}> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.baseUrl}`, { params }).pipe(
      map(response => ({
        content: response.content.map((comp: any) => this.mapToCompetitionSummary(comp)),
        totalElements: response.totalElements
      }))
    );
  }

  /**
   * Get competitions by status
   */
  getCompetitionsByStatus(status: CompetitionStatus, page: number = 0, size: number = 20): Observable<{content: CompetitionSummary[], totalElements: number}> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.baseUrl}/status/${status}`, { params }).pipe(
      map(response => ({
        content: response.content.map((comp: any) => this.mapToCompetitionSummary(comp)),
        totalElements: response.totalElements
      }))
    );
  }

  /**
   * Get active competitions
   */
  getActiveCompetitions(): Observable<Competition[]> {
    return this.http.get<any[]>(`${this.baseUrl}/active`).pipe(
      map(competitions => competitions.map(comp => this.mapToCompetition(comp)))
    );
  }

  /**
   * Get competition by ID
   */
  getCompetitionById(id: number): Observable<Competition> {
    return this.http.get<any>(`${this.baseUrl}/${id}`).pipe(
      map(comp => this.mapToCompetition(comp))
    );
  }

  /**
   * Get user's competitions
   */
  getUserCompetitions(userId: number): Observable<Competition[]> {
    return this.http.get<any[]>(`${this.baseUrl}/user/${userId}`).pipe(
      map(competitions => competitions.map(comp => this.mapToCompetition(comp)))
    );
  }

  /**
   * Join a competition
   */
  joinCompetition(competitionId: number): Observable<CompetitionParticipant> {
    return this.http.post<any>(`${this.baseUrl}/${competitionId}/join`, {}).pipe(
      map(participant => this.mapToParticipant(participant))
    );
  }

  /**
   * Leave a competition
   */
  leaveCompetition(competitionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${competitionId}/leave`);
  }

  /**
   * Get competition leaderboard
   */
  getLeaderboard(competitionId: number): Observable<LeaderboardEntry[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${competitionId}/leaderboard`).pipe(
      map(entries => entries.map(entry => this.mapToLeaderboardEntry(entry)))
    );
  }

  /**
   * Get competition participants
   */
  getParticipants(competitionId: number): Observable<CompetitionParticipant[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${competitionId}/participants`).pipe(
      map(participants => participants.map(p => this.mapToParticipant(p)))
    );
  }

  /**
   * Search competitions
   */
  searchCompetitions(query: string, page: number = 0, size: number = 20): Observable<{content: CompetitionSummary[], totalElements: number}> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(`${this.baseUrl}/search`, { params }).pipe(
      map(response => ({
        content: response.content.map((comp: any) => this.mapToCompetitionSummary(comp)),
        totalElements: response.totalElements
      }))
    );
  }

  /**
   * Select a competition
   */
  selectCompetition(competition: Competition | null): void {
    this.selectedCompetitionSubject.next(competition);
  }

  /**
   * Get selected competition
   */
  getSelectedCompetition(): Competition | null {
    return this.selectedCompetitionSubject.value;
  }

  // Mapping functions
  private mapToCompetition(data: any): Competition {
    return {
      ...data,
      startDate: new Date(data.startDate),
      endDate: new Date(data.endDate),
      createdAt: new Date(data.createdAt),
      updatedAt: new Date(data.updatedAt)
    };
  }

  private mapToCompetitionSummary(data: any): CompetitionSummary {
    return {
      id: data.id,
      title: data.title,
      description: data.description,
      type: data.type,
      status: data.status,
      difficulty: data.difficulty,
      startDate: new Date(data.startDate),
      endDate: new Date(data.endDate),
      maxParticipants: data.maxParticipants,
      currentParticipants: data.currentParticipants,
      iconUrl: data.iconUrl,
      isUserParticipating: data.isUserParticipating
    };
  }

  private mapToParticipant(data: any): CompetitionParticipant {
    return {
      ...data,
      joinedDate: new Date(data.joinedDate)
    };
  }

  private mapToLeaderboardEntry(data: any): LeaderboardEntry {
    return {
      position: data.position,
      userId: data.userId,
      userName: data.userName,
      score: data.score,
      bestPerformance: data.bestPerformance,
      performanceUnit: data.performanceUnit,
      isCurrentUser: data.isCurrentUser
    };
  }

  // Formatting helpers
  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  }

  formatDateTime(date: Date): string {
    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  getDaysRemaining(endDate: Date): number {
    const now = new Date();
    const diff = endDate.getTime() - now.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  isCompetitionActive(competition: Competition): boolean {
    const now = new Date();
    return competition.status === CompetitionStatus.ACTIVE &&
           competition.startDate <= now &&
           competition.endDate >= now;
  }

  isCompetitionUpcoming(competition: Competition): boolean {
    const now = new Date();
    return competition.status === CompetitionStatus.UPCOMING &&
           competition.startDate > now;
  }

  isCompetitionEnded(competition: Competition): boolean {
    const now = new Date();
    return competition.status === CompetitionStatus.COMPLETED ||
           competition.endDate < now;
  }

  canJoinCompetition(competition: Competition): boolean {
    if (competition.isUserParticipating) return false;
    if (competition.status !== CompetitionStatus.UPCOMING &&
        competition.status !== CompetitionStatus.ACTIVE) return false;
    if (competition.maxParticipants &&
        competition.currentParticipants >= competition.maxParticipants) return false;
    return true;
  }
}
