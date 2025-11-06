import {
  Component,
  OnInit,
  signal,
  computed,
  effect,
  ViewChildren,
  QueryList,
  ElementRef,
  ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommunityMapService } from './community-map.service';
import {
  Route,
  RouteWithPosition,
  RouteCompletion,
  CompletionWithUser,
  CompletionUser,
  PathPoint,
  ArrowDefinition,
  RouteStatistics
} from '../../../features/routes/models/route.model';

/**
 * CommunityMapComponent
 *
 * Displays an interactive SVG-based visualization of running routes and community activity.
 *
 * Key Features:
 * - SVG paths representing standard routes from the database
 * - User avatars showing who has completed each route
 * - Direction indicators (forward/reverse arrows)
 * - Completion badges with percentage indicators
 * - Interactive tooltips with detailed completion information
 * - Responsive design adapting to different screen sizes
 *
 * Data Flow:
 * 1. CommunityMapService loads route data, completions, and user info
 * 2. Component calculates marker positions on SVG paths
 * 3. Avatars are grouped by route and positioned on the map
 * 4. Hover interactions show detailed tooltips
 *
 * Technical Notes:
 * - Uses Angular Signals for reactive state management
 * - ViewChildren to access SVG path elements for position calculation
 * - OnPush change detection for performance
 * - Native control flow (@if, @for) for template rendering
 */
@Component({
  selector: 'app-community-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './community-map.component.html',
  styleUrl: './community-map.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CommunityMapComponent implements OnInit {
  // ViewChildren to access SVG path elements
  @ViewChildren('routePath') routePaths!: QueryList<ElementRef<SVGPathElement>>;

  // Signal-based state management
  protected readonly routes = signal<RouteWithPosition[]>([]);
  protected readonly routeCompletions = signal<Map<string, RouteCompletion[]>>(new Map());
  protected readonly users = signal<Map<string, CompletionUser>>(new Map());
  protected readonly statistics = signal<Map<string, RouteStatistics>>(new Map());
  protected readonly loading = signal<boolean>(true);

  // Interaction state
  protected readonly hoveredRoute = signal<Route | null>(null);
  protected readonly hoveredCompletion = signal<CompletionWithUser | null>(null);
  protected readonly tooltipPosition = signal<{ x: number; y: number }>({ x: 0, y: 0 });

  // Constants for SVG rendering
  protected readonly MARKER_POSITION_PERCENTAGE = 0.9; // Place markers at 90% of route path
  protected readonly MAX_AVATARS_DISPLAY = 10; // Maximum number of avatars to show per route
  protected readonly MIN_COMPLETION_PERCENTAGE = 70; // Minimum completion % to display

  constructor(private communityMapService: CommunityMapService) {
    // Effect to calculate positions when paths are available
    effect(() => {
      const pathElements = this.routePaths;
      if (pathElements && pathElements.length > 0) {
        this.calculateMarkerPositions();
      }
    });
  }

  ngOnInit(): void {
    this.loadCommunityData();
  }

  /**
   * Loads community data from the service
   * This includes routes, completions, users, and statistics
   */
  private loadCommunityData(): void {
    this.loading.set(true);

    this.communityMapService.loadCommunityData().subscribe({
      next: (data) => {
        // Convert routes to RouteWithPosition (positions will be calculated in effect)
        const routesWithPosition: RouteWithPosition[] = data.routes.map(route => ({
          ...route,
          markerPosition: { x: 0, y: 0, angle: 0 } // Placeholder, will be calculated
        }));

        this.routes.set(routesWithPosition);
        this.routeCompletions.set(data.completions);
        this.users.set(data.users);
        this.statistics.set(data.statistics);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading community data:', error);
        this.loading.set(false);
      }
    });
  }

  /**
   * Calculates marker positions on SVG paths
   * This is called in an effect when ViewChildren are available
   */
  private calculateMarkerPositions(): void {
    const currentRoutes = this.routes();
    if (currentRoutes.length === 0) return;

    const pathElements = this.routePaths.toArray();
    if (pathElements.length !== currentRoutes.length) return;

    const updatedRoutes = currentRoutes.map((route, index) => {
      const pathElement = pathElements[index]?.nativeElement;
      if (!pathElement) return route;

      const position = this.getPointOnPath(pathElement, this.MARKER_POSITION_PERCENTAGE);

      return {
        ...route,
        markerPosition: position
      };
    });

    this.routes.set(updatedRoutes);
  }

  /**
   * Calculates a point on an SVG path at a given percentage
   * Uses the SVG getPointAtLength API
   *
   * @param pathElement The SVG path element
   * @param percentage Position on path (0-1)
   * @returns PathPoint with x, y coordinates and angle
   */
  private getPointOnPath(pathElement: SVGPathElement, percentage: number): PathPoint {
    const pathLength = pathElement.getTotalLength();
    const targetLength = pathLength * percentage;

    const point = pathElement.getPointAtLength(targetLength);

    // Calculate angle by getting a point slightly ahead
    const deltaLength = Math.min(5, pathLength * 0.01);
    const nextPoint = pathElement.getPointAtLength(Math.min(targetLength + deltaLength, pathLength));

    const angle = Math.atan2(nextPoint.y - point.y, nextPoint.x - point.x) * (180 / Math.PI);

    return {
      x: point.x,
      y: point.y,
      angle
    };
  }

  /**
   * Gets completions for a specific route, filtered and sorted
   * Only includes completions with >= MIN_COMPLETION_PERCENTAGE
   *
   * @param routeId The route ID
   * @returns Array of CompletionWithUser objects
   */
  getRouteCompletions(routeId: string): CompletionWithUser[] {
    const completions = this.routeCompletions().get(routeId) || [];
    const usersMap = this.users();

    // Filter by minimum percentage and map to CompletionWithUser
    const completionsWithUsers = completions
      .filter(c => c.percentage >= this.MIN_COMPLETION_PERCENTAGE)
      .map(completion => {
        const user = usersMap.get(completion.userId);
        if (!user) return null;

        return {
          completion,
          user
        } as CompletionWithUser;
      })
      .filter(c => c !== null) as CompletionWithUser[];

    // Sort by completion date (newest first)
    return completionsWithUsers.sort(
      (a, b) => b.completion.completedAt.getTime() - a.completion.completedAt.getTime()
    );
  }

  /**
   * Gets direction arrows for a route based on completion statistics
   * Returns arrow definitions for rendering polyline markers
   *
   * @param route The route
   * @returns Array of ArrowDefinition objects
   */
  getDirectionArrows(route: Route): ArrowDefinition[] {
    const stats = this.statistics().get(route.id);
    if (!stats) return [];

    const primaryDirection = stats.primaryDirection;
    if (primaryDirection === 'mixed') {
      // Show both directions for mixed routes
      return [
        {
          points: '', // Will be calculated in template
          markerId: 'arrowForward',
          color: 'rgba(34, 197, 94, 0.7)',
          position: 0.3
        },
        {
          points: '',
          markerId: 'arrowReverse',
          color: 'rgba(239, 68, 68, 0.7)',
          position: 0.7
        }
      ];
    }

    // Show primary direction arrows at multiple positions
    const markerId = primaryDirection === 'forward' ? 'arrowForward' : 'arrowReverse';
    const color = primaryDirection === 'forward' ? 'rgba(34, 197, 94, 0.7)' : 'rgba(239, 68, 68, 0.7)';

    return [
      { points: '', markerId, color, position: 0.2 },
      { points: '', markerId, color, position: 0.5 },
      { points: '', markerId, color, position: 0.8 }
    ];
  }

  /**
   * Calculates polyline points for a direction arrow
   * Creates a small arrow shape on the path
   *
   * @param pathElement The SVG path element
   * @param position Position percentage (0-1)
   * @returns Polyline points string
   */
  getArrowPoints(pathElement: SVGPathElement | undefined, position: number): string {
    if (!pathElement) return '';

    const point = this.getPointOnPath(pathElement, position);
    const arrowSize = 8;

    // Calculate arrow points based on angle
    const angleRad = (point.angle - 90) * (Math.PI / 180); // Rotate 90 degrees

    const x1 = point.x + Math.cos(angleRad) * arrowSize;
    const y1 = point.y + Math.sin(angleRad) * arrowSize;

    const x2 = point.x + Math.cos(angleRad + Math.PI / 2) * (arrowSize / 2);
    const y2 = point.y + Math.sin(angleRad + Math.PI / 2) * (arrowSize / 2);

    const x3 = point.x + Math.cos(angleRad - Math.PI / 2) * (arrowSize / 2);
    const y3 = point.y + Math.sin(angleRad - Math.PI / 2) * (arrowSize / 2);

    return `${x1},${y1} ${x2},${y2} ${x3},${y3}`;
  }

  /**
   * Gets the route statistics
   */
  getRouteStats(routeId: string): RouteStatistics | undefined {
    return this.statistics().get(routeId);
  }

  /**
   * Gets the icon for a route based on its properties
   */
  getRouteIcon(route: Route): string {
    return route.icon || '🏃';
  }

  /**
   * Event Handlers
   */

  onRouteBadgeHover(route: Route): void {
    this.hoveredRoute.set(route);
  }

  onRouteBadgeLeave(): void {
    this.hoveredRoute.set(null);
  }

  onAvatarHover(user: CompletionUser, completion: RouteCompletion, event: MouseEvent): void {
    this.hoveredCompletion.set({ user, completion });
    this.tooltipPosition.set({
      x: event.clientX + 15,
      y: event.clientY + 15
    });
  }

  onAvatarLeave(): void {
    this.hoveredCompletion.set(null);
  }

  /**
   * Utility Methods for Template
   */

  /**
   * Formats a date for display
   */
  formatDate(date: Date): string {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Heute';
    if (diffDays === 1) return 'Gestern';
    if (diffDays < 7) return `Vor ${diffDays} Tagen`;

    return date.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  /**
   * Gets direction label in German
   */
  getDirectionLabel(direction: 'forward' | 'reverse'): string {
    return direction === 'forward' ? 'Vorwärts' : 'Rückwärts';
  }

  /**
   * Gets direction icon
   */
  getDirectionIcon(direction: 'forward' | 'reverse'): string {
    return direction === 'forward' ? '↑' : '↓';
  }

  /**
   * Gets the difficulty color for a route
   */
  getDifficultyColor(difficulty?: string): string {
    const colors: { [key: string]: string } = {
      'EASY': '#4caf50',
      'MODERATE': '#ff9800',
      'HARD': '#f44336',
      'EXPERT': '#9c27b0'
    };
    return difficulty ? colors[difficulty] || '#757575' : '#757575';
  }

  /**
   * Tracks routes by ID for @for loop
   */
  trackRoute(_index: number, route: RouteWithPosition): string {
    return route.id;
  }

  /**
   * Tracks completions by user ID for @for loop
   */
  trackCompletion(_index: number, completion: CompletionWithUser): string {
    return `${completion.user.id}-${completion.completion.routeId}`;
  }
}
