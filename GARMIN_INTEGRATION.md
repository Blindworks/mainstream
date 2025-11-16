# Garmin Connect Integration

This document describes the Garmin Connect integration for the Mainstream running application.

## Overview

The Garmin Connect integration allows users to:
- Connect their Garmin Connect account via OAuth 2.0
- Automatically sync running activities from Garmin
- Import GPS data, elevation, pace, and other metrics
- Disconnect their Garmin account when desired

## Setup Requirements

### 1. Garmin Developer Program Registration

To use this integration, you must register for the Garmin Connect Developer Program:

1. Visit https://developer.garmin.com/gc-developer-program/
2. Apply for the Garmin Connect Developer Program
3. Wait for approval (may take several business days)
4. Once approved, create a new application in the developer portal
5. Configure your OAuth 2.0 settings:
   - Redirect URI: `http://localhost:4200/garmin/callback` (development)
   - Request appropriate scopes: `activity_export`, `activity_read`
6. Note your Client ID and Client Secret

### 2. Configuration

Set the following environment variables or update `application.properties`:

```properties
# Garmin Connect OAuth Configuration
mainstream.garmin.client-id=${GARMIN_CLIENT_ID:your-client-id-here}
mainstream.garmin.client-secret=${GARMIN_CLIENT_SECRET:your-client-secret-here}
mainstream.garmin.redirect-uri=http://localhost:4200/garmin/callback
mainstream.garmin.api-url=https://connect.garmin.com/proxy
mainstream.garmin.auth-url=https://connect.garmin.com/oauthConfirm
mainstream.garmin.token-url=https://connect.garmin.com/oauth2/token
```

**Note:** The API URLs may need to be adjusted based on the Garmin Developer Program documentation you receive upon approval.

## Architecture

### Backend (Java/Spring Boot)

```
com.mainstream.garmin/
├── config/
│   └── GarminProperties.java       # Configuration properties
├── dto/
│   ├── GarminTokenResponse.java    # OAuth token response
│   ├── GarminUser.java             # Garmin user profile
│   ├── GarminActivity.java         # Activity summary
│   └── GarminActivityDetails.java  # Detailed activity with GPS
├── service/
│   ├── GarminApiService.java       # HTTP calls to Garmin API
│   └── GarminSyncService.java      # Business logic & data sync
└── controller/
    └── GarminController.java       # REST API endpoints
```

### Frontend (Angular)

```
app/features/garmin/
├── services/
│   └── garmin.service.ts           # Angular HTTP service
└── components/
    └── garmin-connection.component.ts  # UI component

app/pages/garmin/
└── garmin-callback.component.ts    # OAuth callback handler
```

### Database Schema

**Users Table** (additional columns):
- `garmin_user_id` (VARCHAR 100) - Garmin user identifier
- `garmin_access_token` (VARCHAR 500) - OAuth access token
- `garmin_refresh_token` (VARCHAR 500) - OAuth refresh token
- `garmin_token_expires_at` (DATETIME) - Token expiration time
- `garmin_connected_at` (DATETIME) - Connection timestamp

**Runs Table** (additional column):
- `garmin_activity_id` (BIGINT) - Link to original Garmin activity

## API Endpoints

All endpoints require `X-User-Id` header for authentication.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/garmin/auth-url` | GET | Get OAuth authorization URL |
| `/api/garmin/connect?code={code}` | POST | Connect Garmin account |
| `/api/garmin/disconnect` | DELETE | Disconnect Garmin account |
| `/api/garmin/sync?since={datetime}` | POST | Sync activities |
| `/api/garmin/status` | GET | Get connection status |
| `/api/garmin/runs/{runId}/backfill-gps` | POST | Backfill GPS for a run |
| `/api/garmin/backfill-all-gps` | POST | Backfill all GPS data |

## OAuth 2.0 Flow

1. User clicks "Connect to Garmin" in profile
2. Frontend requests auth URL from `/api/garmin/auth-url`
3. Opens popup to Garmin OAuth authorization page
4. User authorizes the app on Garmin Connect
5. Garmin redirects to `/garmin/callback?code={CODE}`
6. Frontend captures code and sends to `/api/garmin/connect`
7. Backend exchanges code for access/refresh tokens
8. Tokens stored encrypted in database
9. Connection established

## Data Mapping

| Garmin Field | Run Field | Conversion |
|--------------|-----------|------------|
| `activityName` | `title` | Direct |
| `description` | `description` | Direct |
| `startTimeLocal` | `startTime` | ISO DateTime parse |
| `movingDuration` | `durationSeconds` | Double → Integer |
| `distance` | `distanceMeters` | Direct (meters) |
| `averageSpeed` | `averageSpeedKmh` | m/s × 3.6 |
| `maxSpeed` | `maxSpeedKmh` | m/s × 3.6 |
| `elevationGain` | `elevationGainMeters` | Direct (meters) |
| `calories` | `caloriesBurned` | Double → Integer |
| `activityId` | `garminActivityId` | Direct |
| `activityType.typeKey` | Filter | Only "running" activities |

## Activity Filtering

Only activities with `activityType.typeKey` containing "running" are synced:
- `running`
- `trail_running`
- `treadmill_running`
- etc.

## GPS Data Import

GPS points are extracted from the activity's `geoPolylineDTO`:
- Latitude and longitude coordinates
- Altitude data
- Timestamps (milliseconds from epoch)
- Distance from start
- Limited to 1000 points per activity for performance

## Token Management

- Access tokens expire (configurable, typically 3 months)
- Automatic refresh 5 minutes before expiration
- New refresh token stored with each refresh
- Token validation on every API call

## Sync Process

1. Check token validity, refresh if needed
2. Fetch activities from Garmin API
3. Filter to running activities only
4. Check for duplicates (by `garminActivityId`)
5. Convert to Run entity
6. Fetch GPS polyline data
7. Create GPS points
8. Trigger route matching and trophy checking
9. Return sync summary

## Troubleshooting

### Common Issues

1. **"User is not connected to Garmin"**
   - User hasn't completed OAuth flow
   - Connection was disconnected
   - Solution: Re-connect via profile page

2. **"Failed to connect to Garmin"**
   - Invalid authorization code
   - Network issues
   - Check backend logs for details

3. **No activities syncing**
   - Verify activity type is "running"
   - Check date range (default: last 30 days)
   - Activities may already be synced

4. **Missing GPS data**
   - Activity may not have GPS (treadmill)
   - Garmin privacy settings
   - Use backfill endpoint to retry

### Developer Console

Monitor backend logs for:
```
Connecting Garmin for user ID: {id}
Syncing Garmin activities for user ID: {id}
Fetched {n} activities from Garmin
Synced activity: {name} (Garmin ID: {id})
```

## Security Considerations

- Client secret stored securely (environment variable)
- Access tokens encrypted in database
- User-specific authorization
- Token refresh handled server-side
- No client-side token exposure

## Future Enhancements

1. Webhook support for real-time sync (Garmin Push API)
2. Additional activity types (cycling, swimming)
3. Heart rate data import
4. Training load metrics
5. Two-way sync (optional)

## Related Files

- **Strava Integration**: Similar implementation at `STRAVA_INTEGRATION.md`
- **Nike Integration**: Basic implementation in `com.mainstream.nike`
- **Route Matching**: `com.mainstream.activity.service.UserActivityService`
- **Trophy Checking**: Integrated with activity processing

## Testing

1. Start backend: `./mvnw spring-boot:run`
2. Start frontend: `ng serve`
3. Navigate to Profile > Third-party Integrations
4. Click "Connect to Garmin"
5. Authorize in popup
6. Click "Sync Activities"
7. Check runs list for imported activities

## Support

For issues specific to Garmin API:
- Garmin Developer Support: https://developer.garmin.com/support/
- API Documentation (available after program approval)

For application issues:
- Check backend logs
- Review network requests in browser dev tools
- Verify environment configuration
