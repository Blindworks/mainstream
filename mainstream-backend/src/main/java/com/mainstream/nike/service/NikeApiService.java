package com.mainstream.nike.service;

import com.mainstream.nike.config.NikeProperties;
import com.mainstream.nike.dto.NikeActivitiesResponse;
import com.mainstream.nike.dto.NikeActivity;
import com.mainstream.nike.dto.NikeUserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NikeApiService {

    private final NikeProperties nikeProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Validates Nike access token by fetching user profile
     */
    public NikeUserProfile getUserProfile(String accessToken) {
        log.info("Fetching Nike user profile");

        try {
            String url = nikeProperties.getApiUrl() + "/sport/v3/me";

            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<NikeUserProfile> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    NikeUserProfile.class
            );

            log.info("Successfully fetched Nike user profile");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch Nike user profile: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Invalid Nike access token or API error", e);
        }
    }

    /**
     * Fetches all activities from Nike API with pagination
     */
    public List<NikeActivity> getAllActivities(String accessToken) {
        log.info("Fetching all activities from Nike");

        List<NikeActivity> allActivities = new ArrayList<>();
        String afterId = null;
        Long afterTime = 0L; // Start from epoch 0

        try {
            // First request uses after_time
            NikeActivitiesResponse response = getActivitiesAfterTime(accessToken, afterTime);

            if (response != null && response.getActivities() != null) {
                // Fetch detailed info for each activity
                for (NikeActivitiesResponse.NikeActivitySummary summary : response.getActivities()) {
                    try {
                        NikeActivity detailedActivity = getActivity(accessToken, summary.getId());
                        if (detailedActivity != null) {
                            allActivities.add(detailedActivity);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch activity details for ID {}: {}", summary.getId(), e.getMessage());
                    }
                }

                // Continue pagination with after_id
                if (response.getPaging() != null && response.getPaging().getAfterId() != null) {
                    afterId = response.getPaging().getAfterId();

                    while (afterId != null) {
                        response = getActivitiesAfterId(accessToken, afterId);

                        if (response == null || response.getActivities() == null || response.getActivities().isEmpty()) {
                            break;
                        }

                        for (NikeActivitiesResponse.NikeActivitySummary summary : response.getActivities()) {
                            try {
                                NikeActivity detailedActivity = getActivity(accessToken, summary.getId());
                                if (detailedActivity != null) {
                                    allActivities.add(detailedActivity);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to fetch activity details for ID {}: {}", summary.getId(), e.getMessage());
                            }
                        }

                        afterId = response.getPaging() != null ? response.getPaging().getAfterId() : null;
                    }
                }
            }

            log.info("Fetched {} total activities from Nike", allActivities.size());
            return allActivities;
        } catch (Exception e) {
            log.error("Error fetching Nike activities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Nike activities", e);
        }
    }

    /**
     * Fetches activities after a specific time
     */
    private NikeActivitiesResponse getActivitiesAfterTime(String accessToken, Long afterTimeMs) {
        String url = nikeProperties.getApiUrl() + "/sport/v3/me/activities/after_time/" + afterTimeMs;

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<NikeActivitiesResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    NikeActivitiesResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch activities after time: {} - {}", e.getStatusCode(), e.getMessage());
            throw e;
        }
    }

    /**
     * Fetches activities after a specific activity ID (pagination)
     */
    private NikeActivitiesResponse getActivitiesAfterId(String accessToken, String afterId) {
        String url = nikeProperties.getApiUrl() + "/sport/v3/me/activities/after_id/" + afterId;

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<NikeActivitiesResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    NikeActivitiesResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch activities after ID: {} - {}", e.getStatusCode(), e.getMessage());
            throw e;
        }
    }

    /**
     * Fetches a single activity with all metrics
     */
    public NikeActivity getActivity(String accessToken, String activityId) {
        log.debug("Fetching Nike activity: {}", activityId);

        String url = nikeProperties.getApiUrl() + "/sport/v3/me/activity/" + activityId + "?metrics=ALL";

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<NikeActivity> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    NikeActivity.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch activity {}: {} - {}", activityId, e.getStatusCode(), e.getMessage());
            throw e;
        }
    }

    /**
     * Creates HTTP headers with Bearer authentication
     */
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
