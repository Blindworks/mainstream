# Email Service Komponente

Diese Dokumentation beschreibt die E-Mail-Sende-Komponente im MainStream Backend.

## Übersicht

Die E-Mail-Service-Komponente bietet eine vollständige Lösung zum Versenden von E-Mails mit folgenden Features:

- Einfache Text-E-Mails
- HTML-E-Mails
- Template-basierte E-Mails
- E-Mails mit Anhängen
- CC und BCC Unterstützung
- Vordefinierte Templates (Willkommen, Passwort-Reset)

## Architektur

```
com.mainstream.email/
├── config/
│   └── EmailConfig.java          # E-Mail-Konfiguration
├── controller/
│   └── EmailController.java      # REST-Endpunkte
├── dto/
│   ├── EmailRequestDto.java      # E-Mail-Anfrage DTO
│   ├── EmailAttachmentDto.java   # Anhang DTO
│   └── EmailResponseDto.java     # Antwort DTO
└── service/
    ├── EmailService.java          # Service Interface
    ├── EmailServiceImpl.java      # Service Implementierung
    └── EmailTemplateService.java  # Template-Verarbeitung
```

## Konfiguration

### application.properties

```properties
# E-Mail-Server Konfiguration
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.default-encoding=UTF-8
mainstream.mail.from=${MAIL_FROM:noreply@mainstream.com}
```

### Umgebungsvariablen

Setze folgende Umgebungsvariablen für die Produktionsumgebung:

```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export MAIL_FROM=noreply@mainstream.com
```

### Gmail Konfiguration

Für Gmail:
1. Gehe zu deinem Google Account
2. Aktiviere 2-Faktor-Authentifizierung
3. Erstelle ein App-Passwort unter "Sicherheit" → "App-Passwörter"
4. Verwende dieses App-Passwort als `MAIL_PASSWORD`

## Verwendung

### 1. Einfache Text-E-Mail

```java
@Autowired
private EmailService emailService;

EmailResponseDto response = emailService.sendSimpleEmail(
    "user@example.com",
    "Test Subject",
    "This is a test email body"
);
```

### 2. HTML-E-Mail

```java
String htmlBody = "<h1>Hello</h1><p>This is an HTML email</p>";
EmailResponseDto response = emailService.sendHtmlEmail(
    "user@example.com",
    "Test Subject",
    htmlBody
);
```

### 3. E-Mail mit allen Optionen

```java
EmailRequestDto request = EmailRequestDto.builder()
    .to(List.of("user1@example.com", "user2@example.com"))
    .cc(List.of("cc@example.com"))
    .bcc(List.of("bcc@example.com"))
    .subject("Important Email")
    .body("<h1>Hello</h1><p>Content here</p>")
    .isHtml(true)
    .build();

EmailResponseDto response = emailService.sendEmail(request);
```

### 4. Template-basierte E-Mail

```java
Map<String, Object> variables = new HashMap<>();
variables.put("username", "John Doe");
variables.put("loginUrl", "http://localhost:4200/login");

EmailResponseDto response = emailService.sendTemplatedEmail(
    "user@example.com",
    "Willkommen bei MainStream!",
    "welcome",
    variables
);
```

## REST API Endpunkte

### Einfache E-Mail senden

```bash
POST /api/email/send/simple
Content-Type: application/x-www-form-urlencoded

to=user@example.com&subject=Test&body=Hello World
```

### HTML E-Mail senden

```bash
POST /api/email/send/html
Content-Type: application/x-www-form-urlencoded

to=user@example.com&subject=Test&htmlBody=<h1>Hello</h1>
```

### Vollständige E-Mail senden

```bash
POST /api/email/send
Content-Type: application/json

{
  "to": ["user@example.com"],
  "cc": ["cc@example.com"],
  "subject": "Test Email",
  "body": "<h1>Hello</h1>",
  "isHtml": true
}
```

### Willkommens-E-Mail senden

```bash
POST /api/email/send/welcome
Content-Type: application/x-www-form-urlencoded

to=user@example.com&username=John Doe
```

### Passwort-Reset E-Mail senden

```bash
POST /api/email/send/password-reset
Content-Type: application/x-www-form-urlencoded

to=user@example.com&username=John Doe&resetToken=abc123xyz
```

### Template-E-Mail senden

```bash
POST /api/email/send/template?to=user@example.com&subject=Test&templateName=welcome
Content-Type: application/json

{
  "username": "John Doe",
  "loginUrl": "http://localhost:4200/login"
}
```

### Health Check

```bash
GET /api/email/health
```

## E-Mail Templates

Templates befinden sich in `src/main/resources/templates/email/`.

### Verfügbare Templates

1. **welcome.html** - Willkommens-E-Mail
   - Variablen: `username`, `loginUrl`

2. **password-reset.html** - Passwort-Reset E-Mail
   - Variablen: `username`, `resetUrl`, `expirationHours`

### Eigene Templates erstellen

1. Erstelle eine neue HTML-Datei in `src/main/resources/templates/email/`
2. Verwende `{{variableName}}` als Platzhalter
3. Rufe `emailService.sendTemplatedEmail()` mit dem Template-Namen auf

Beispiel `custom-template.html`:
```html
<!DOCTYPE html>
<html>
<body>
    <h1>Hallo {{name}}</h1>
    <p>Deine Nachricht: {{message}}</p>
</body>
</html>
```

## Fehlerbehandlung

Die Service-Methoden geben immer ein `EmailResponseDto` zurück:

```java
EmailResponseDto response = emailService.sendEmail(request);

if (response.isSuccess()) {
    // E-Mail erfolgreich gesendet
    System.out.println("Gesendet am: " + response.getSentAt());
} else {
    // Fehler beim Senden
    System.out.println("Fehler: " + response.getMessage());
    System.out.println("Details: " + response.getErrorDetails());
}
```

## Testing

Für Tests kann ein Test-SMTP-Server wie [MailHog](https://github.com/mailhog/MailHog) oder [FakeSMTP](http://nilhcem.com/FakeSMTP/) verwendet werden.

Konfiguration für MailHog:
```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
```

## Best Practices

1. **Asynchrones Senden**: Für bessere Performance sollten E-Mails asynchron gesendet werden:
   ```java
   @Async
   public CompletableFuture<EmailResponseDto> sendEmailAsync(EmailRequestDto request) {
       return CompletableFuture.completedFuture(sendEmail(request));
   }
   ```

2. **Rate Limiting**: Implementiere Rate Limiting, um Spam zu verhindern

3. **Queue-System**: Für hohe Last verwende eine Message Queue (RabbitMQ, Kafka)

4. **Monitoring**: Überwache E-Mail-Versand-Erfolgsraten und Fehler

5. **Validierung**: Validiere E-Mail-Adressen vor dem Senden

## Sicherheit

- Verwende niemals Klartext-Passwörter in application.properties
- Verwende Umgebungsvariablen oder ein Secret-Management-System
- Aktiviere STARTTLS für sichere Verbindungen
- Implementiere Input-Validierung gegen E-Mail-Injection

## Troubleshooting

### E-Mails werden nicht gesendet

1. Überprüfe SMTP-Server-Konfiguration
2. Überprüfe Firewall-Einstellungen
3. Überprüfe E-Mail-Credentials
4. Aktiviere Debug-Logging: `logging.level.org.springframework.mail=DEBUG`

### Gmail blockiert E-Mails

1. Aktiviere "Weniger sichere Apps" oder verwende App-Passwörter
2. Überprüfe, ob 2FA aktiviert ist
3. Überprüfe Gmail-Sicherheitseinstellungen

## Weitere Features

Mögliche Erweiterungen:

- [ ] Asynchrone E-Mail-Versendung mit @Async
- [ ] E-Mail-Queue mit Retry-Mechanismus
- [ ] E-Mail-Versand-Historie in Datenbank
- [ ] Thymeleaf-Integration für komplexe Templates
- [ ] Inline-Bilder in E-Mails
- [ ] Bulk-E-Mail-Versand
- [ ] E-Mail-Tracking (Öffnungsrate, Klicks)
