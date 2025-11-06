import { Injectable } from '@angular/core';
import { Observable, of, forkJoin } from 'rxjs';
import { map, delay } from 'rxjs/operators';
import {
  Route,
  RouteCompletion,
  CompletionUser,
  CommunityData,
  RouteStatistics
} from '../../../features/routes/models/route.model';

/**
 * Service for managing community map data
 *
 * This service orchestrates data from multiple sources:
 * - RouteService: Provides standard routes with SVG path data
 * - RouteCompletionService: Provides user completions of routes
 * - UserService: Provides user information
 *
 * For development, this service provides mock data.
 * In production, inject the actual services and replace mock implementations.
 */
@Injectable({
  providedIn: 'root'
})
export class CommunityMapService {
  // Mock data flags - set to false when real services are available
  private readonly USE_MOCK_DATA = true;

  constructor(
    // TODO: Inject real services when available
    // private routeService: RouteService,
    // private routeCompletionService: RouteCompletionService,
    // private userService: UserService
  ) {}

  /**
   * Loads all community data needed for the map visualization
   * Combines routes, completions, and user data
   */
  loadCommunityData(): Observable<CommunityData> {
    if (this.USE_MOCK_DATA) {
      return this.loadMockCommunityData();
    }

    // Production implementation (when services are available):
    // return forkJoin({
    //   routes: this.routeService.getStandardRoutes(),
    //   completions: this.routeCompletionService.getAllCompletions(),
    //   users: this.userService.getUsers()
    // }).pipe(
    //   map(({ routes, completions, users }) => {
    //     return this.processCommunityData(routes, completions, users);
    //   })
    // );

    return of({
      routes: [],
      completions: new Map(),
      users: new Map(),
      statistics: new Map()
    });
  }

  /**
   * Gets statistics for a specific route
   */
  getRouteStatistics(routeId: string, completions: RouteCompletion[]): RouteStatistics {
    const routeCompletions = completions.filter(c => c.routeId === routeId);

    const forwardCount = routeCompletions.filter(c => c.direction === 'forward').length;
    const reverseCount = routeCompletions.filter(c => c.direction === 'reverse').length;
    const fullCompletions = routeCompletions.filter(c => c.percentage === 100).length;

    const totalPercentage = routeCompletions.reduce((sum, c) => sum + c.percentage, 0);
    const averageCompletion = routeCompletions.length > 0 ? totalPercentage / routeCompletions.length : 0;

    const uniqueUsers = new Set(routeCompletions.map(c => c.userId)).size;

    let primaryDirection: 'forward' | 'reverse' | 'mixed' = 'mixed';
    if (forwardCount > reverseCount * 2) primaryDirection = 'forward';
    else if (reverseCount > forwardCount * 2) primaryDirection = 'reverse';

    return {
      routeId,
      totalCompletions: routeCompletions.length,
      averageCompletion,
      forwardCount,
      reverseCount,
      fullCompletions,
      primaryDirection,
      uniqueUsers
    };
  }

  /**
   * Processes raw data into the CommunityData structure
   */
  private processCommunityData(
    routes: Route[],
    completions: RouteCompletion[],
    users: CompletionUser[]
  ): CommunityData {
    // Create maps for efficient lookup
    const completionsMap = new Map<string, RouteCompletion[]>();
    const usersMap = new Map<string, CompletionUser>();
    const statisticsMap = new Map<string, RouteStatistics>();

    // Group completions by route
    completions.forEach(completion => {
      const routeCompletions = completionsMap.get(completion.routeId) || [];
      routeCompletions.push(completion);
      completionsMap.set(completion.routeId, routeCompletions);
    });

    // Create user map
    users.forEach(user => {
      usersMap.set(user.id, user);
    });

    // Calculate statistics for each route
    routes.forEach(route => {
      const routeCompletions = completionsMap.get(route.id) || [];
      const stats = this.getRouteStatistics(route.id, routeCompletions);
      statisticsMap.set(route.id, stats);
    });

    return {
      routes,
      completions: completionsMap,
      users: usersMap,
      statistics: statisticsMap
    };
  }

  /**
   * Loads mock community data for development
   */
  private loadMockCommunityData(): Observable<CommunityData> {
    const routes = this.generateMockRoutes();
    const users = this.generateMockUsers(25);
    const completions = this.generateMockCompletions(routes, users);

    const data = this.processCommunityData(routes, completions, users);

    // Simulate network delay
    return of(data).pipe(delay(500));
  }

  /**
   * Generates mock routes with SVG path data
   * These represent standard running routes in Frankfurt along the Main river
   */
  private generateMockRoutes(): Route[] {
    return [
      {
        id: 'route-1',
        name: '10km Vollrunde Main',
        distance: 10.2,
        description: 'Komplette Mainufer-Runde, beide Seiten',
        difficulty: 'MODERATE',
        category: 'RIVERSIDE',
        icon: '🌊',
        isActive: true,
        // SVG path representing a loop along the Main river
        pathData: 'M 150 200 Q 200 150 300 180 Q 400 220 500 200 Q 600 180 650 220 Q 700 260 680 350 Q 660 440 600 480 Q 500 520 400 500 Q 300 480 220 450 Q 150 420 120 350 Q 100 280 150 200 Z'
      },
      {
        id: 'route-2',
        name: '5km Ostpark Loop',
        distance: 5.3,
        description: 'Schnelle Runde durch den Ostpark',
        difficulty: 'EASY',
        category: 'PARK',
        icon: '🌳',
        isActive: true,
        // SVG path representing a smaller loop in the east
        pathData: 'M 700 300 Q 750 280 800 300 Q 850 340 840 400 Q 820 450 770 460 Q 720 450 700 400 Q 690 350 700 300 Z'
      },
      {
        id: 'route-3',
        name: '7km Nordmainufer',
        distance: 7.1,
        description: 'Nordseite des Mainufers, Hin und Zurück',
        difficulty: 'EASY',
        category: 'RIVERSIDE',
        icon: '⬆️',
        isActive: true,
        // SVG path representing north side of Main
        pathData: 'M 180 240 Q 280 200 400 210 Q 520 220 640 240 L 640 260 Q 520 240 400 230 Q 280 220 180 260 Z'
      },
      {
        id: 'route-4',
        name: '12km Stadtwald Trail',
        distance: 12.5,
        description: 'Anspruchsvolle Trail-Strecke durch den Stadtwald',
        difficulty: 'HARD',
        category: 'TRAIL',
        icon: '🏔️',
        isActive: true,
        // SVG path representing a trail route
        pathData: 'M 250 500 Q 280 450 320 460 Q 360 480 380 520 Q 400 560 440 580 Q 490 600 520 570 Q 540 540 560 500 Q 580 450 540 410 Q 500 380 460 400 L 420 430 Q 380 410 340 390 Q 300 380 260 420 Q 230 460 250 500 Z'
      }
    ];
  }

  /**
   * Generates mock users
   */
  private generateMockUsers(count: number): CompletionUser[] {
    const firstNames = ['Max', 'Anna', 'Tim', 'Sarah', 'Jan', 'Lisa', 'Tom', 'Emma', 'Lukas', 'Sophie',
                        'Felix', 'Laura', 'Ben', 'Marie', 'Paul', 'Julia', 'Leon', 'Lea', 'Finn', 'Hannah',
                        'Nico', 'Lena', 'David', 'Mia', 'Jonas'];

    const lastNames = ['Müller', 'Schmidt', 'Weber', 'Wagner', 'Becker', 'Schulz', 'Hoffmann', 'Koch',
                       'Richter', 'Klein', 'Wolf', 'Schröder', 'Neumann', 'Schwarz', 'Zimmermann'];

    const avatarColors = ['e91e63', '9c27b0', '673ab7', '3f51b5', '2196f3', '03a9f4', '00bcd4',
                          '009688', '4caf50', '8bc34a', 'cddc39', 'ffeb3b', 'ffc107', 'ff9800', 'ff5722'];

    return Array.from({ length: count }, (_, i) => {
      const firstName = firstNames[i % firstNames.length];
      const lastName = lastNames[Math.floor(i / firstNames.length) % lastNames.length];
      const color = avatarColors[i % avatarColors.length];

      return {
        id: `user-${i + 1}`,
        name: `${firstName} ${lastName.charAt(0)}.`,
        firstName,
        lastName,
        avatarUrl: `https://ui-avatars.com/api/?name=${firstName}+${lastName}&background=${color}&color=fff&size=128`
      };
    });
  }

  /**
   * Generates mock route completions
   * Creates realistic completion data with varying percentages and directions
   */
  private generateMockCompletions(routes: Route[], users: CompletionUser[]): RouteCompletion[] {
    const completions: RouteCompletion[] = [];
    const now = new Date();

    routes.forEach(route => {
      // Each route has 5-15 completions
      const completionCount = 5 + Math.floor(Math.random() * 10);

      // Randomly select users for this route
      const shuffledUsers = [...users].sort(() => Math.random() - 0.5);
      const selectedUsers = shuffledUsers.slice(0, completionCount);

      selectedUsers.forEach((user, index) => {
        // Generate completion date (within last 30 days)
        const daysAgo = Math.floor(Math.random() * 30);
        const completedAt = new Date(now);
        completedAt.setDate(completedAt.getDate() - daysAgo);

        // Most completions are high percentage (70-100%)
        const percentageRoll = Math.random();
        let percentage: number;
        if (percentageRoll < 0.6) {
          percentage = 100; // 60% are perfect matches
        } else if (percentageRoll < 0.85) {
          percentage = 90 + Math.floor(Math.random() * 10); // 25% are 90-99%
        } else {
          percentage = 70 + Math.floor(Math.random() * 20); // 15% are 70-89%
        }

        // Direction based on route statistics
        // Route 1 and 3: more forward
        // Route 2 and 4: more mixed
        let direction: 'forward' | 'reverse';
        if (route.id === 'route-1' || route.id === 'route-3') {
          direction = Math.random() < 0.7 ? 'forward' : 'reverse';
        } else {
          direction = Math.random() < 0.5 ? 'forward' : 'reverse';
        }

        // Match accuracy correlates with percentage
        const matchAccuracy = Math.max(70, percentage - Math.floor(Math.random() * 10));

        completions.push({
          id: `completion-${route.id}-${user.id}`,
          userId: user.id,
          routeId: route.id,
          completedAt,
          percentage,
          direction,
          matchAccuracy
        });
      });
    });

    // Sort by completion date (newest first)
    return completions.sort((a, b) => b.completedAt.getTime() - a.completedAt.getTime());
  }
}
