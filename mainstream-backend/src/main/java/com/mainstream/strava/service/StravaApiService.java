package com.mainstream.strava.service;

import com.mainstream.strava.config.StravaProperties;
import com.mainstream.strava.dto.StravaActivity;
import com.mainstream.strava.dto.StravaStream;
import com.mainstream.strava.dto.StravaTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StravaApiService {

    private final StravaProperties stravaProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Generates the Strava OAuth authorization URL
     */
    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(stravaProperties.getAuthUrl())
                .queryParam("client_id", stravaProperties.getClientId())
                .queryParam("redirect_uri", stravaProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "read,activity:read_all,activity:read")
                .queryParam("approval_prompt", "auto")
                .toUriString();
    }

    /**
     * Exchanges authorization code for access token
     */
    public StravaTokenResponse exchangeToken(String authorizationCode) {
        log.info("Exchanging authorization code for access token");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", stravaProperties.getClientId());
        requestBody.add("client_secret", stravaProperties.getClientSecret());
        requestBody.add("code", authorizationCode);
        requestBody.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<StravaTokenResponse> response = restTemplate.exchange(
                stravaProperties.getTokenUrl(),
                HttpMethod.POST,
                request,
                StravaTokenResponse.class
        );

        log.info("Successfully exchanged token for athlete ID: {}",
                response.getBody() != null && response.getBody().getAthlete() != null ?
                        response.getBody().getAthlete().getId() : "unknown");

        return response.getBody();
    }

    /**
     * Refreshes an expired access token
     */
    public StravaTokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing Strava access token");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", stravaProperties.getClientId());
        requestBody.add("client_secret", stravaProperties.getClientSecret());
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<StravaTokenResponse> response = restTemplate.exchange(
                stravaProperties.getTokenUrl(),
                HttpMethod.POST,
                request,
                StravaTokenResponse.class
        );

        log.info("Successfully refreshed access token");

        return response.getBody();
    }

    /**
     * Fetches activities from Strava API
     */
    public List<StravaActivity> getActivities(String accessToken, LocalDateTime after, Integer perPage) {
        log.info("Fetching activities from Strava since: {}", after);

        String url = UriComponentsBuilder.fromHttpUrl(stravaProperties.getApiUrl() + "/athlete/activities")
                .queryParam("after", after != null ? after.toEpochSecond(ZoneOffset.UTC) : null)
                .queryParam("per_page", perPage != null ? perPage : 30)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<StravaActivity>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<StravaActivity>>() {}
        );

        List<StravaActivity> activities = response.getBody();
        log.info("Fetched {} activities from Strava", activities != null ? activities.size() : 0);

        return activities;
    }

    /**
     * Fetches a single activity with detailed information
     */
    public StravaActivity getActivity(String accessToken, Long activityId) {
        log.info("Fetching detailed activity: {}", activityId);

        String url = stravaProperties.getApiUrl() + "/activities/" + activityId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<StravaActivity> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                StravaActivity.class
        );

        return response.getBody();
    }

    /**
     * Fetches activity streams (GPS data, altitude, time, etc.)
     * Stream types: latlng, altitude, time, distance, velocity_smooth, heartrate, cadence, watts, temp, moving, grade_smooth
     */
    public List<StravaStream> getActivityStreams(String accessToken, Long activityId) {
        log.info("Fetching activity streams for activity: {}", activityId);

        String url = UriComponentsBuilder.fromHttpUrl(stravaProperties.getApiUrl() + "/activities/" + activityId + "/streams")
                .queryParam("keys", "latlng,altitude,time,distance")
                .queryParam("key_by_type", "true")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<StravaStream>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<StravaStream>>() {}
            );

            List<StravaStream> streams = response.getBody();
            log.info("Fetched {} stream types for activity {}", streams != null ? streams.size() : 0, activityId);
            return streams;
        } catch (Exception e) {
            log.error("Error fetching activity streams for activity {}: {}", activityId, e.getMessage());
            return List.of();
        }
    }
}
