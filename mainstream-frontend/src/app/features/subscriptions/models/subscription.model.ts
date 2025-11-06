export interface SubscriptionPlan {
  id: string;
  name: string;
  price: number;
  currency: string;
  interval: 'month' | 'year';
  features: string[];
  highlighted?: boolean;
  buttonText?: string;
  popular?: boolean;
}

export interface UserSubscription {
  id: string;
  userId: string;
  planId: string;
  status: 'active' | 'inactive' | 'cancelled' | 'expired';
  startDate: Date;
  endDate?: Date;
  autoRenew: boolean;
}

export const SUBSCRIPTION_PLANS: SubscriptionPlan[] = [
  {
    id: 'free',
    name: 'Free',
    price: 0,
    currency: '€',
    interval: 'month',
    features: [
      'Grundlegende Laufstatistiken',
      'Bis zu 10 Läufe pro Monat',
      'Streckenvisualisierung',
      'Teilnahme an öffentlichen Wettbewerben',
      'Community-Zugang'
    ],
    buttonText: 'Kostenlos starten',
    highlighted: false
  },
  {
    id: 'premium',
    name: 'Premium',
    price: 9.99,
    currency: '€',
    interval: 'month',
    features: [
      'Alle Free-Features',
      'Unbegrenzte Läufe',
      'Erweiterte Analysen & Statistiken',
      'Persönliche Bestleistungen-Tracking',
      'Herz-Frequenz-Zonen-Analyse',
      'Höhenprofil & Pace-Diagramme',
      'Erweiterte Strava-Integration',
      'Exklusive Premium-Wettbewerbe',
      'Individuelle Trainingspläne',
      'Keine Werbung',
      'Prioritäts-Support'
    ],
    buttonText: 'Premium werden',
    highlighted: true,
    popular: true
  }
];
