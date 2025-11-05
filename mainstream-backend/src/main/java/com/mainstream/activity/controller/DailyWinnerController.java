package com.mainstream.activity.controller;

import com.mainstream.activity.dto.DailyWinnerDto;
import com.mainstream.activity.entity.DailyWinner;
import com.mainstream.activity.service.DailyWinnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for daily winners and leaderboards.
 */
@RestController
@RequestMapping("/api/daily-winners")
@RequiredArgsConstructor
@Slf4j
public class DailyWinnerController {

    private final DailyWinnerService dailyWinnerService;

    /**
     * Get winners for today.
     */
    @GetMapping("/today")
    public ResponseEntity<List<DailyWinnerDto>> getTodaysWinners() {
        List<DailyWinner> winners = dailyWinnerService.getWinnersForDate(LocalDate.now());
        List<DailyWinnerDto> dtos = winners.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get winners for a specific date.
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<DailyWinnerDto>> getWinnersByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<DailyWinner> winners = dailyWinnerService.getWinnersForDate(date);
        List<DailyWinnerDto> dtos = winners.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get recent winners (last N days).
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DailyWinnerDto>> getRecentWinners(
            @RequestParam(value = "days", defaultValue = "7") int days) {

        List<DailyWinner> winners = dailyWinnerService.getRecentWinners(days);
        List<DailyWinnerDto> dtos = winners.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Manually trigger daily winner calculation for a date (Admin only).
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> calculateDailyWinners(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();

        try {
            dailyWinnerService.calculateDailyWinners(targetDate);
            return ResponseEntity.ok("Daily winners calculated successfully for date: " + targetDate);
        } catch (Exception e) {
            log.error("Error calculating daily winners", e);
            return ResponseEntity.status(500).body("Error calculating winners: " + e.getMessage());
        }
    }

    /**
     * Convert entity to DTO.
     */
    private DailyWinnerDto toDto(DailyWinner winner) {
        return DailyWinnerDto.builder()
                .id(winner.getId())
                .winnerDate(winner.getWinnerDate())
                .category(winner.getCategory())
                .userId(winner.getUser().getId())
                .userName(winner.getUser().getEmail())
                .userFirstName(winner.getUser().getFirstName())
                .userLastName(winner.getUser().getLastName())
                .activityId(winner.getActivity() != null ? winner.getActivity().getId() : null)
                .achievementValue(winner.getAchievementValue())
                .achievementDescription(winner.getAchievementDescription())
                .createdAt(winner.getCreatedAt())
                .build();
    }
}
