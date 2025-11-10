import { TrophyType, CheckScope } from './trophy.model';

/**
 * Configuration information for each trophy type.
 * Helps admins understand what parameters are available when creating trophies.
 */
export interface TrophyTypeConfig {
  type: TrophyType;
  name: string;
  description: string;
  configParameters: ConfigParameter[];
  examples: string[];
}

export interface ConfigParameter {
  name: string;
  type: 'number' | 'string' | 'array' | 'select';
  required: boolean;
  description: string;
  defaultValue?: any;
  options?: { value: any; label: string }[];
}

/**
 * Configuration metadata for all trophy types.
 * Used by admin UI to generate dynamic trophy creation forms.
 */
export const TROPHY_TYPE_CONFIGS: TrophyTypeConfig[] = [
  {
    type: TrophyType.DISTANCE_MILESTONE,
    name: 'Distanz-Meilenstein',
    description: 'Belohnt für das Erreichen von Distanz-Zielen (einzeln oder total)',
    configParameters: [
      {
        name: 'distanceMeters',
        type: 'number',
        required: true,
        description: 'Distanz in Metern (z.B. 10000 für 10km)'
      },
      {
        name: 'scope',
        type: 'select',
        required: true,
        description: 'Scope: Einzelner Lauf oder Gesamt-Distanz',
        defaultValue: 'TOTAL',
        options: [
          { value: 'SINGLE_ACTIVITY', label: 'Einzelner Lauf' },
          { value: 'TOTAL', label: 'Gesamt-Distanz' }
        ]
      }
    ],
    examples: [
      '{"distanceMeters": 10000, "scope": "SINGLE_ACTIVITY"} - 10km in einem Lauf',
      '{"distanceMeters": 100000, "scope": "TOTAL"} - 100km insgesamt'
    ]
  },
  {
    type: TrophyType.STREAK,
    name: 'Serie',
    description: 'Belohnt für aufeinanderfolgende Tage mit Aktivitäten',
    configParameters: [
      {
        name: 'consecutiveDays',
        type: 'number',
        required: true,
        description: 'Anzahl aufeinanderfolgender Tage'
      },
      {
        name: 'minimumDistancePerDay',
        type: 'number',
        required: false,
        description: 'Optional: Minimum-Distanz pro Tag in Metern'
      }
    ],
    examples: [
      '{"consecutiveDays": 7} - 7 Tage in Folge',
      '{"consecutiveDays": 14, "minimumDistancePerDay": 1000} - 14 Tage je mind. 1km'
    ]
  },
  {
    type: TrophyType.TIME_BASED,
    name: 'Zeitbasiert',
    description: 'Belohnt für Aktivitäten zu bestimmten Tageszeiten',
    configParameters: [
      {
        name: 'startHour',
        type: 'number',
        required: true,
        description: 'Start-Stunde (0-23)'
      },
      {
        name: 'endHour',
        type: 'number',
        required: true,
        description: 'End-Stunde (0-23)'
      },
      {
        name: 'requiredCount',
        type: 'number',
        required: true,
        description: 'Benötigte Anzahl Aktivitäten'
      },
      {
        name: 'daysOfWeek',
        type: 'array',
        required: false,
        description: 'Optional: Wochentage (1=Montag, 7=Sonntag)'
      },
      {
        name: 'minimumDistance',
        type: 'number',
        required: false,
        description: 'Optional: Minimum-Distanz in Metern'
      }
    ],
    examples: [
      '{"startHour": 5, "endHour": 7, "requiredCount": 10} - 10x vor 7 Uhr',
      '{"startHour": 0, "endHour": 24, "requiredCount": 15, "daysOfWeek": [6,7]} - 15x am Wochenende'
    ]
  },
  {
    type: TrophyType.CONSISTENCY,
    name: 'Beständigkeit',
    description: 'Belohnt für regelmäßiges Training über mehrere Wochen',
    configParameters: [
      {
        name: 'minActivitiesPerWeek',
        type: 'number',
        required: true,
        description: 'Minimum Aktivitäten pro Woche'
      },
      {
        name: 'numberOfWeeks',
        type: 'number',
        required: true,
        description: 'Anzahl aufeinanderfolgender Wochen'
      },
      {
        name: 'minDistancePerActivity',
        type: 'number',
        required: false,
        description: 'Optional: Minimum-Distanz pro Aktivität in Metern'
      }
    ],
    examples: [
      '{"minActivitiesPerWeek": 3, "numberOfWeeks": 4} - 3x/Woche über 4 Wochen',
      '{"minActivitiesPerWeek": 5, "numberOfWeeks": 8, "minDistancePerActivity": 2000} - 5x/Woche je 2km über 8 Wochen'
    ]
  },
  {
    type: TrophyType.EXPLORER,
    name: 'Entdecker',
    description: 'Belohnt für das Erkunden verschiedener Gebiete',
    configParameters: [
      {
        name: 'uniqueAreasCount',
        type: 'number',
        required: true,
        description: 'Anzahl verschiedener Gebiete'
      },
      {
        name: 'gridSizeMeters',
        type: 'number',
        required: false,
        description: 'Optional: Grid-Größe in Metern (z.B. 1000 für 1km×1km)'
      },
      {
        name: 'radiusMeters',
        type: 'number',
        required: false,
        description: 'Optional: Radius für Gebiets-Clustering'
      },
      {
        name: 'minDistancePerArea',
        type: 'number',
        required: false,
        description: 'Optional: Minimum-Distanz pro Gebiet'
      }
    ],
    examples: [
      '{"uniqueAreasCount": 10, "gridSizeMeters": 1000} - 10 verschiedene 1km-Gebiete',
      '{"uniqueAreasCount": 5, "radiusMeters": 5000} - 5 Gebiete mit 5km Radius'
    ]
  },
  {
    type: TrophyType.ROUTE_COMPLETION,
    name: 'Streckenabschluss',
    description: 'Belohnt für das Absolvieren vordefinierter Routen',
    configParameters: [
      {
        name: 'routeId',
        type: 'number',
        required: false,
        description: 'Spezifische Routen-ID (für einzelne Route)'
      },
      {
        name: 'uniqueRoutesCount',
        type: 'number',
        required: false,
        description: 'Anzahl verschiedener Routen (Alternative zu routeId)'
      },
      {
        name: 'minMatchPercentage',
        type: 'number',
        required: false,
        description: 'Optional: Minimum Match-Prozentsatz (Standard: 80)',
        defaultValue: 80
      }
    ],
    examples: [
      '{"routeId": 123, "minMatchPercentage": 90} - Spezifische Route mit 90% Match',
      '{"uniqueRoutesCount": 5, "minMatchPercentage": 80} - 5 verschiedene Routen'
    ]
  },
  {
    type: TrophyType.LOCATION_BASED,
    name: 'Standortbasiert',
    description: 'Belohnt für GPS-Position an bestimmten Orten',
    configParameters: [
      {
        name: 'latitude',
        type: 'number',
        required: true,
        description: 'Breitengrad des Ortes'
      },
      {
        name: 'longitude',
        type: 'number',
        required: true,
        description: 'Längengrad des Ortes'
      },
      {
        name: 'collectionRadiusMeters',
        type: 'number',
        required: true,
        description: 'Radius in Metern für Sammlung'
      },
      {
        name: 'locationName',
        type: 'string',
        required: false,
        description: 'Optional: Name des Ortes'
      }
    ],
    examples: [
      '{"latitude": 52.5200, "longitude": 13.4050, "collectionRadiusMeters": 100, "locationName": "Brandenburger Tor"}'
    ]
  },
  {
    type: TrophyType.SPECIAL,
    name: 'Spezial',
    description: 'Belohnt für besondere Ereignisse und Leistungen',
    configParameters: [
      {
        name: 'specialType',
        type: 'select',
        required: true,
        description: 'Art der Spezial-Trophäe',
        options: [
          { value: 'BIRTHDAY_RUN', label: 'Geburtstags-Lauf' },
          { value: 'DATE_BASED', label: 'Datums-basiert' },
          { value: 'PERFORMANCE', label: 'Performance' },
          { value: 'FIRST_ACTIVITY', label: 'Erste Aktivität' }
        ]
      },
      {
        name: 'month',
        type: 'number',
        required: false,
        description: 'Monat (1-12) für DATE_BASED'
      },
      {
        name: 'day',
        type: 'number',
        required: false,
        description: 'Tag (1-31) für DATE_BASED'
      },
      {
        name: 'distanceMeters',
        type: 'number',
        required: false,
        description: 'Distanz in Metern für PERFORMANCE'
      },
      {
        name: 'maxDurationSeconds',
        type: 'number',
        required: false,
        description: 'Max. Dauer in Sekunden für PERFORMANCE'
      }
    ],
    examples: [
      '{"specialType": "BIRTHDAY_RUN"} - Lauf am Geburtstag',
      '{"specialType": "DATE_BASED", "month": 1, "day": 1} - Neujahrs-Lauf',
      '{"specialType": "PERFORMANCE", "distanceMeters": 10000, "maxDurationSeconds": 2700} - 10km unter 45min'
    ]
  }
];

/**
 * Get configuration for a specific trophy type
 */
export function getTrophyTypeConfig(type: TrophyType): TrophyTypeConfig | undefined {
  return TROPHY_TYPE_CONFIGS.find(config => config.type === type);
}

/**
 * Trophy type labels for display
 */
export const TROPHY_TYPE_LABELS: Record<TrophyType, string> = {
  [TrophyType.DISTANCE_MILESTONE]: 'Distanz-Meilenstein',
  [TrophyType.STREAK]: 'Serie',
  [TrophyType.ROUTE_COMPLETION]: 'Streckenabschluss',
  [TrophyType.CONSISTENCY]: 'Beständigkeit',
  [TrophyType.TIME_BASED]: 'Zeitbasiert',
  [TrophyType.EXPLORER]: 'Entdecker',
  [TrophyType.LOCATION_BASED]: 'Standortbasiert',
  [TrophyType.SPECIAL]: 'Spezial'
};
