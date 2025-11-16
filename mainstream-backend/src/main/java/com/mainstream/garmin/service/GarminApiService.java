package com.mainstream.garmin.service;

import com.mainstream.garmin.config.GarminProperties;
import com.mainstream.garmin.dto.GarminActivity;
import com.mainstream.garmin.dto.GarminActivityDetails;
import com.mainstream.garmin.dto.GarminTokenResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarminApiService {

    private final GarminProperties garminProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Generates the Garmin OAuth authorization URL
     */
    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(garminProperties.getAuthUrl())
                .queryParam("client_id", garminProperties.getClientId())
                .queryParam("redirect_uri", garminProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "activity_export activity_read")
                .queryParam("approval_prompt", "auto")
                .toUriString();
    }

    /**
     * Exchanges authorization code for access token
     */
    public GarminTokenResponse exchangeToken(String authorizationCode) {
        log.info("Exchanging authorization code for access token");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", garminProperties.getClientId());
        requestBody.add("client_secret", garminProperties.getClientSecret());
        requestBody.add("code", authorizationCode);
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("redirect_uri", garminProperties.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<GarminTokenResponse> response = restTemplate.exchange(
                garminProperties.getTokenUrl(),
                HttpMethod.POST,
                request,
                GarminTokenResponse.class
        );

        log.info("Successfully exchanged token for Garmin user ID: {}",
                response.getBody() != null ? response.getBody().getUserId() : "unknown");

        return response.getBody();
    }

    /**
     * Refreshes an expired access token
     */
    public GarminTokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing Garmin access token");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", garminProperties.getClientId());
        requestBody.add("client_secret", garminProperties.getClientSecret());
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<GarminTokenResponse> response = restTemplate.exchange(
                garminProperties.getTokenUrl(),
                HttpMethod.POST,
                request,
                GarminTokenResponse.class
        );

        log.info("Successfully refreshed access token");

        return response.getBody();
    }

    /**
     * Fetches activities from Garmin API
     */
    public List<GarminActivity> getActivities(String accessToken, LocalDateTime after, Integer limit) {
        log.info("Fetching activities from Garmin since: {}", after);

        String url = UriComponentsBuilder.fromHttpUrl(garminProperties.getApiUrl() + "/activities")
                .queryParam("start", after != null ? after.toEpochSecond(ZoneOffset.UTC) * 1000 : null)
                .queryParam("limit", limit != null ? limit : 30)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<GarminActivity>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<GarminActivity>>() {}
        );

        List<GarminActivity> activities = response.getBody();
        log.info("Fetched {} activities from Garmin", activities != null ? activities.size() : 0);

        return activities;
    }

    /**
     * Fetches a single activity with detailed information including GPS data
     */
    public GarminActivityDetails getActivityDetails(String accessToken, Long activityId) {
        log.info("Fetching detailed activity: {}", activityId);

        String url = garminProperties.getApiUrl() + "/activities/" + activityId + "/details";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GarminActivityDetails> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    GarminActivityDetails.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching activity details for {}: {}", activityId, e.getMessage());
            return null;
        }
    }
}
