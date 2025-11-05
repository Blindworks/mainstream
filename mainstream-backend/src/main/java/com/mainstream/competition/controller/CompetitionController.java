package com.mainstream.competition.controller;

import com.mainstream.competition.dto.CompetitionDto;
import com.mainstream.competition.dto.CompetitionParticipantDto;
import com.mainstream.competition.dto.CompetitionSummaryDto;
import com.mainstream.competition.dto.LeaderboardEntryDto;
import com.mainstream.competition.entity.Competition;
import com.mainstream.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    /**
     * Get all competitions
     */
    @GetMapping
    public ResponseEntity<Page<CompetitionSummaryDto>> getAllCompetitions(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {

        log.info("Fetching all competitions for user: {}", userId);

        Page<CompetitionSummaryDto> competitions = competitionService.getAllCompetitions(userId, pageable);
        return ResponseEntity.ok(competitions);
    }

    /**
     * Get competitions by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<CompetitionSummaryDto>> getCompetitionsByStatus(
            @PathVariable Competition.CompetitionStatus status,
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {

        log.info("Fetching competitions with status {} for user: {}", status, userId);

        Page<CompetitionSummaryDto> competitions = competitionService.getCompetitionsByStatus(status, userId, pageable);
        return ResponseEntity.ok(competitions);
    }

    /**
     * Get active competitions
     */
    @GetMapping("/active")
    public ResponseEntity<List<CompetitionDto>> getActiveCompetitions(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetching active competitions for user: {}", userId);

        List<CompetitionDto> competitions = competitionService.getActiveCompetitions(userId);
        return ResponseEntity.ok(competitions);
    }

    /**
     * Get competition by ID
     */
    @GetMapping("/{competitionId}")
    public ResponseEntity<CompetitionDto> getCompetitionById(
            @PathVariable Long competitionId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetching competition {} for user: {}", competitionId, userId);

        CompetitionDto competition = competitionService.getCompetitionById(competitionId, userId);
        return ResponseEntity.ok(competition);
    }

    /**
     * Get competitions user is participating in
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CompetitionDto>> getUserCompetitions(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long requestingUserId) {

        log.info("Fetching competitions for user: {}", userId);

        // For now, allow users to only see their own competitions
        // Could be extended to allow viewing others' public competitions
        if (!userId.equals(requestingUserId)) {
            return ResponseEntity.status(403).build();
        }

        List<CompetitionDto> competitions = competitionService.getUserCompetitions(userId);
        return ResponseEntity.ok(competitions);
    }

    /**
     * Join a competition
     */
    @PostMapping("/{competitionId}/join")
    public ResponseEntity<CompetitionParticipantDto> joinCompetition(
            @PathVariable Long competitionId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("User {} joining competition {}", userId, competitionId);

        try {
            CompetitionParticipantDto participant = competitionService.joinCompetition(competitionId, userId);
            return ResponseEntity.status(201).body(participant);
        } catch (RuntimeException e) {
            log.error("Error joining competition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Leave a competition
     */
    @DeleteMapping("/{competitionId}/leave")
    public ResponseEntity<Void> leaveCompetition(
            @PathVariable Long competitionId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("User {} leaving competition {}", userId, competitionId);

        try {
            competitionService.leaveCompetition(competitionId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error leaving competition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get competition leaderboard
     */
    @GetMapping("/{competitionId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(
            @PathVariable Long competitionId,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetching leaderboard for competition {}", competitionId);

        List<LeaderboardEntryDto> leaderboard = competitionService.getLeaderboard(competitionId, userId);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Get all participants for a competition
     */
    @GetMapping("/{competitionId}/participants")
    public ResponseEntity<List<CompetitionParticipantDto>> getParticipants(
            @PathVariable Long competitionId) {

        log.info("Fetching participants for competition {}", competitionId);

        List<CompetitionParticipantDto> participants = competitionService.getParticipants(competitionId);
        return ResponseEntity.ok(participants);
    }

    /**
     * Search competitions
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CompetitionSummaryDto>> searchCompetitions(
            @RequestParam String query,
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching competitions with query: {}", query);

        Page<CompetitionSummaryDto> competitions = competitionService.searchCompetitions(query, userId, pageable);
        return ResponseEntity.ok(competitions);
    }
}
