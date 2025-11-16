import { Component, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ViewportScroller } from '@angular/common';

interface FaqItem {
  question: string;
  answer: string;
  category: string;
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.scss'
})
export class FaqComponent {
  private location = inject(Location);
  private viewportScroller = inject(ViewportScroller);

  expandedIndex: number | null = null;
  selectedCategory: string = 'all';

  categories = [
    { id: 'all', name: 'Alle Fragen', icon: '📋' },
    { id: 'account', name: 'Konto & Registrierung', icon: '👤' },
    { id: 'features', name: 'Funktionen', icon: '⚙️' },
    { id: 'competitions', name: 'Wettkämpfe', icon: '🏆' },
    { id: 'integrations', name: 'Integrationen', icon: '🔗' },
    { id: 'subscriptions', name: 'Abonnements', icon: '💳' },
    { id: 'privacy', name: 'Datenschutz', icon: '🔒' }
  ];

  faqItems: FaqItem[] = [
    // Account & Registration
    {
      category: 'account',
      question: 'Wie erstelle ich ein Konto bei MainStream?',
      answer: 'Klicken Sie auf "Registrieren" in der oberen Navigation. Geben Sie Ihre E-Mail-Adresse, einen Benutzernamen und ein sicheres Passwort ein. Nach der Bestätigung Ihrer E-Mail-Adresse können Sie sofort loslegen.'
    },
    {
      category: 'account',
      question: 'Kann ich meinen Benutzernamen ändern?',
      answer: 'Ja, Sie können Ihren Benutzernamen in Ihren Profileinstellungen ändern. Gehen Sie zu "Profil" und klicken Sie auf "Bearbeiten". Beachten Sie, dass der neue Benutzername verfügbar sein muss.'
    },
    {
      category: 'account',
      question: 'Was mache ich, wenn ich mein Passwort vergessen habe?',
      answer: 'Klicken Sie auf der Login-Seite auf "Passwort vergessen". Geben Sie Ihre registrierte E-Mail-Adresse ein und Sie erhalten einen Link zum Zurücksetzen Ihres Passworts. Der Link ist 24 Stunden gültig.'
    },
    {
      category: 'account',
      question: 'Wie lösche ich mein Konto?',
      answer: 'Sie können Ihr Konto in den Profileinstellungen unter "Konto löschen" entfernen. Bitte beachten Sie, dass alle Ihre Daten, einschließlich Läufe, Trophäen und Wettkampf-Teilnahmen, unwiderruflich gelöscht werden.'
    },

    // Features
    {
      category: 'features',
      question: 'Welche Funktionen bietet MainStream?',
      answer: 'MainStream bietet Ihnen: Verfolgung Ihrer Laufaktivitäten, Teilnahme an Community-Wettkämpfen, Sammeln von Trophäen und Auszeichnungen, Ansicht von Community-Routen, persönliche Statistiken (täglich, monatlich, jährlich) und Integration mit Strava und Garmin.'
    },
    {
      category: 'features',
      question: 'Wie kann ich meine Läufe einsehen?',
      answer: 'Navigieren Sie zu "Läufe" im Hauptmenü. Dort sehen Sie eine Liste aller Ihrer synchronisierten Läufe mit Details wie Datum, Distanz, Dauer und Tempo. Sie können nach Zeitraum filtern und detaillierte Ansichten öffnen.'
    },
    {
      category: 'features',
      question: 'Was sind Trophäen und wie verdiene ich sie?',
      answer: 'Trophäen sind Auszeichnungen für besondere Leistungen. Sie können Trophäen verdienen für: bestimmte Distanzen (z.B. erster 10K-Lauf), Streak-Ziele (aufeinanderfolgende Trainingstage), Wettkampf-Siege und persönliche Bestleistungen. Besuchen Sie die Trophäen-Seite, um alle verfügbaren Auszeichnungen zu sehen.'
    },
    {
      category: 'features',
      question: 'Wie funktioniert die Community-Karte?',
      answer: 'Die Community-Karte zeigt in Echtzeit, wo MainStream-Mitglieder aktiv sind. Sie sehen anonymisierte Aktivitätspunkte und können populäre Laufgebiete entdecken. Die Karte aktualisiert sich automatisch mit neuen Daten.'
    },

    // Competitions
    {
      category: 'competitions',
      question: 'Wie nehme ich an einem Wettkampf teil?',
      answer: 'Gehen Sie zur Wettkampf-Seite und wählen Sie einen aktiven Wettkampf aus. Klicken Sie auf "Teilnehmen" und bestätigen Sie Ihre Anmeldung. Ihre Läufe während des Wettkampfzeitraums werden automatisch gezählt.'
    },
    {
      category: 'competitions',
      question: 'Welche Arten von Wettkämpfen gibt es?',
      answer: 'Es gibt verschiedene Wettkampftypen: Distanz-Wettkämpfe (wer läuft am weitesten), Zeit-Wettkämpfe (wer läuft am längsten), Häufigkeits-Wettkämpfe (wer läuft am öftesten) und Team-Wettkämpfe (Gruppenleistung). Jeder Wettkampf hat spezifische Regeln und Zeiträume.'
    },
    {
      category: 'competitions',
      question: 'Kann ich mehrere Wettkämpfe gleichzeitig bestreiten?',
      answer: 'Ja, Sie können an mehreren Wettkämpfen gleichzeitig teilnehmen. Ihre Läufe werden für alle aktiven Wettkämpfe gezählt, an denen Sie teilnehmen. Behalten Sie Ihre Fortschritte auf der Wettkampf-Seite im Blick.'
    },
    {
      category: 'competitions',
      question: 'Wie werden die Gewinner ermittelt?',
      answer: 'Die Platzierungen werden automatisch basierend auf den Wettkampfkriterien berechnet. Bei Distanz-Wettkämpfen zählt die Gesamtdistanz, bei Zeit-Wettkämpfen die Gesamtzeit usw. Die Ergebnisse werden in Echtzeit aktualisiert.'
    },

    // Integrations
    {
      category: 'integrations',
      question: 'Wie verbinde ich mein Strava-Konto?',
      answer: 'Gehen Sie zu Ihren Profileinstellungen und klicken Sie auf "Mit Strava verbinden". Sie werden zu Strava weitergeleitet, um die Berechtigung zu erteilen. Nach erfolgreicher Verbindung werden Ihre Läufe automatisch synchronisiert.'
    },
    {
      category: 'integrations',
      question: 'Welche Daten werden von Strava übernommen?',
      answer: 'Wir synchronisieren Ihre Laufaktivitäten (Datum, Distanz, Dauer, Tempo, Route), Ihr öffentliches Profil (Name, Profilbild) und Aktivitätsstatistiken. Wir haben keinen Zugriff auf Ihre privaten Strava-Daten oder andere Aktivitätstypen.'
    },
    {
      category: 'integrations',
      question: 'Unterstützt MainStream andere Fitness-Apps?',
      answer: 'Ja! Neben Strava unterstützen wir auch Garmin Connect. Weitere Integrationen wie Nike Run Club sind in Planung. Besuchen Sie regelmäßig unsere Update-Seite für neue Funktionen.'
    },
    {
      category: 'integrations',
      question: 'Kann ich die Verbindung zu Strava/Garmin trennen?',
      answer: 'Ja, Sie können jederzeit die Verbindung in Ihren Profileinstellungen trennen. Ihre bereits synchronisierten Daten bleiben erhalten, aber es werden keine neuen Daten mehr abgerufen. Sie können die Verbindung jederzeit wieder herstellen.'
    },

    // Subscriptions
    {
      category: 'subscriptions',
      question: 'Welche Premium-Funktionen gibt es?',
      answer: 'Premium-Mitglieder erhalten: erweiterte Statistiken und Analysen, unbegrenzte Wettkampf-Teilnahmen, exklusive Trophäen, Zugang zu Premium-Routen, priorisierter Support und werbefreies Erlebnis.'
    },
    {
      category: 'subscriptions',
      question: 'Wie viel kostet ein Premium-Abonnement?',
      answer: 'Wir bieten verschiedene Abonnement-Optionen: Monatlich, jährlich und lebenslang. Die aktuellen Preise finden Sie auf unserer Abonnement-Seite. Jährliche Abonnements bieten erhebliche Ersparnisse gegenüber monatlichen.'
    },
    {
      category: 'subscriptions',
      question: 'Kann ich mein Abonnement kündigen?',
      answer: 'Ja, Sie können Ihr Abonnement jederzeit in Ihren Kontoeinstellungen kündigen. Das Abonnement bleibt bis zum Ende der bezahlten Periode aktiv. Sie verlieren danach den Zugang zu Premium-Funktionen, behalten aber Ihre Basis-Funktionen.'
    },
    {
      category: 'subscriptions',
      question: 'Gibt es eine Testphase?',
      answer: 'Ja, neue Benutzer können MainStream Premium 7 Tage lang kostenlos testen. Während der Testphase haben Sie vollen Zugang zu allen Premium-Funktionen. Die Testphase endet automatisch ohne Verpflichtung.'
    },

    // Privacy
    {
      category: 'privacy',
      question: 'Wie schützt MainStream meine Daten?',
      answer: 'Wir verwenden modernste Verschlüsselungstechnologien für alle Datenübertragungen. Ihre persönlichen Daten werden auf sicheren Servern in Deutschland gespeichert und entsprechen der DSGVO. Wir verkaufen Ihre Daten niemals an Dritte.'
    },
    {
      category: 'privacy',
      question: 'Wer kann meine Laufdaten sehen?',
      answer: 'Standardmäßig sind Ihre Laufdaten privat und nur für Sie sichtbar. Bei Wettkämpfen werden aggregierte Statistiken (Distanz, Zeit) mit anderen Teilnehmern geteilt. Sie können Ihre Datenschutzeinstellungen jederzeit in Ihrem Profil anpassen.'
    },
    {
      category: 'privacy',
      question: 'Kann ich meine Daten exportieren?',
      answer: 'Ja, gemäß DSGVO können Sie eine Kopie aller Ihrer gespeicherten Daten anfordern. Gehen Sie zu Profileinstellungen > Datenschutz > "Daten exportieren". Sie erhalten eine E-Mail mit einem Download-Link innerhalb von 48 Stunden.'
    },
    {
      category: 'privacy',
      question: 'Verwendet MainStream Cookies?',
      answer: 'Wir verwenden nur notwendige und funktionale Cookies, um die Website zu betreiben und Ihre Einstellungen zu speichern. Wir verwenden keine Tracking-Cookies von Drittanbietern. Weitere Details finden Sie in unserer Datenschutzerklärung.'
    }
  ];

  get filteredFaqs(): FaqItem[] {
    if (this.selectedCategory === 'all') {
      return this.faqItems;
    }
    return this.faqItems.filter(item => item.category === this.selectedCategory);
  }

  toggleQuestion(index: number): void {
    this.expandedIndex = this.expandedIndex === index ? null : index;
  }

  selectCategory(categoryId: string): void {
    this.selectedCategory = categoryId;
    this.expandedIndex = null;
  }

  goBack(): void {
    this.location.back();
  }

  scrollToTop(): void {
    this.viewportScroller.scrollToPosition([0, 0]);
  }
}
