import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { Trophy, UserTrophy, TrophyWithProgress } from '../models/trophy.model';
import { ApiService } from '../../../shared/services/api.service';

@Injectable({
  providedIn: 'root'
})
export class TrophyService {
  private baseUrl: string;
  private selectedTrophySubject = new BehaviorSubject<Trophy | null>(null);
  public selectedTrophy$ = this.selectedTrophySubject.asObservable();

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) {
    this.baseUrl = `${this.apiService.apiUrl}/api/trophies`;
  }

  /**
   * Get all available trophies
   */
  getAllTrophies(): Observable<Trophy[]> {
    return this.http.get<any[]>(`${this.baseUrl}`).pipe(
      map(trophies => trophies.map(trophy => this.mapToTrophy(trophy)))
    );
  }

  /**
   * Get user's earned trophies
   */
  getUserTrophies(): Observable<UserTrophy[]> {
    return this.http.get<any[]>(`${this.baseUrl}/my`).pipe(
      map(userTrophies => userTrophies.map(ut => this.mapToUserTrophy(ut)))
    );
  }

  /**
   * Get trophies earned for a specific activity/run
   */
  getTrophiesForActivity(activityId: number): Observable<UserTrophy[]> {
    return this.http.get<any[]>(`${this.baseUrl}/activity/${activityId}`).pipe(
      map(userTrophies => userTrophies.map(ut => this.mapToUserTrophy(ut)))
    );
  }

  /**
   * Get all trophies with user's progress
   */
  getTrophiesWithProgress(): Observable<TrophyWithProgress[]> {
    return new Observable(observer => {
      Promise.all([
        this.getAllTrophies().toPromise(),
        this.getUserTrophies().toPromise()
      ]).then(([allTrophies, userTrophies]) => {
        const trophiesWithProgress = allTrophies?.map(trophy => {
          const userTrophy = userTrophies?.find(ut => ut.trophy.id === trophy.id);
          return {
            ...trophy,
            isEarned: !!userTrophy,
            earnedAt: userTrophy?.earnedAt
          } as TrophyWithProgress;
        }) || [];

        observer.next(trophiesWithProgress);
        observer.complete();
      }).catch(error => {
        observer.error(error);
      });
    });
  }

  /**
   * Initialize default trophies (admin)
   */
  initializeTrophies(): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/initialize`, {});
  }

  /**
   * Get today's trophy - a trophy that is only available on this specific day
   */
  getTodaysTrophy(): Observable<Trophy | null> {
    return this.http.get<any>(`${this.baseUrl}/daily/today`).pipe(
      map(trophy => trophy ? this.mapToTrophy(trophy) : null)
    );
  }

  /**
   * Get users who have earned today's trophy
   */
  getTodaysTrophyWinners(): Observable<UserTrophy[]> {
    return this.http.get<any[]>(`${this.baseUrl}/daily/today/winners`).pipe(
      map(userTrophies => userTrophies.map(ut => this.mapToUserTrophy(ut)))
    );
  }

  /**
   * Get weekly trophies - trophies earned this week
   */
  getWeeklyTrophies(): Observable<UserTrophy[]> {
    return this.http.get<any[]>(`${this.baseUrl}/weekly`).pipe(
      map(userTrophies => userTrophies.map(ut => this.mapToUserTrophy(ut)))
    );
  }

  /**
   * Get weekly trophy stats grouped by trophy
   */
  getWeeklyTrophyStats(): Observable<{ trophy: Trophy; count: number; winners: UserTrophy[] }[]> {
    return this.getWeeklyTrophies().pipe(
      map(userTrophies => {
        const grouped = new Map<number, { trophy: Trophy; count: number; winners: UserTrophy[] }>();

        userTrophies.forEach(ut => {
          const existing = grouped.get(ut.trophy.id);
          if (existing) {
            existing.count++;
            existing.winners.push(ut);
          } else {
            grouped.set(ut.trophy.id, {
              trophy: ut.trophy,
              count: 1,
              winners: [ut]
            });
          }
        });

        // Sort by count descending
        return Array.from(grouped.values()).sort((a, b) => b.count - a.count);
      })
    );
  }

  /**
   * Select a trophy
   */
  selectTrophy(trophy: Trophy | null): void {
    this.selectedTrophySubject.next(trophy);
  }

  /**
   * Get selected trophy
   */
  getSelectedTrophy(): Trophy | null {
    return this.selectedTrophySubject.value;
  }

  // Mapping functions
  private mapToTrophy(data: any): Trophy {
    return {
      ...data,
      createdAt: new Date(data.createdAt),
      updatedAt: new Date(data.updatedAt)
    };
  }

  private mapToUserTrophy(data: any): UserTrophy {
    return {
      id: data.id,
      userId: data.userId,
      userName: data.userName,
      trophy: this.mapToTrophy(data.trophy),
      activityId: data.activityId,
      earnedAt: new Date(data.earnedAt),
      metadata: data.metadata
    };
  }

  // Helper functions
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

  getTrophyTypeName(type: string): string {
    const typeNames: { [key: string]: string } = {
      'DISTANCE_MILESTONE': 'Distanz-Meilenstein',
      'STREAK': 'Serie',
      'ROUTE_COMPLETION': 'Streckenabschluss',
      'CONSISTENCY': 'Beständigkeit',
      'TIME_BASED': 'Zeitbasiert',
      'EXPLORER': 'Entdecker',
        'LOCATION_BASED': 'Standortbasiert',
      'SPECIAL': 'Spezial'
    };
    return typeNames[type] || type;
  }

  getTrophyCategoryName(category: string): string {
    const categoryNames: { [key: string]: string } = {
      'BEGINNER': 'Anfänger',
      'INTERMEDIATE': 'Fortgeschritten',
      'ADVANCED': 'Erweitert',
      'ELITE': 'Elite',
      'SPECIAL': 'Spezial'
    };
    return categoryNames[category] || category;
  }

  getCategoryColor(category: string): string {
    const colors: { [key: string]: string } = {
      'BEGINNER': '#4caf50',
      'INTERMEDIATE': '#2196f3',
      'ADVANCED': '#ff9800',
      'ELITE': '#9c27b0',
      'SPECIAL': '#f44336'
    };
    return colors[category] || '#757575';
  }
}
