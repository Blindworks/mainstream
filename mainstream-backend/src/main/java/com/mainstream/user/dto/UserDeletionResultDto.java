package com.mainstream.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for GDPR-compliant user account deletion result.
 * Provides detailed information about what data was deleted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletionResultDto {

    private Long userId;
    private String email;
    private LocalDateTime deletionTimestamp;
    private boolean success;
    private String message;

    // Counts of deleted entities
    private int runsDeleted;
    private int userActivitiesDeleted;
    private int userTrophiesDeleted;
    private int fitFileUploadsDeleted;
    private int dailyWinnersDeleted;
    private int competitionParticipantsDeleted;
    private int competitionsReassigned;
    private int routesAnonymized;
    private int subscriptionsDeleted;
    private int ordersDeleted;
    private int paymentsDeleted;
    private int passwordResetTokensDeleted;

    // File storage cleanup
    private int avatarFilesDeleted;
    private int fitFilesDeleted;

    // Third-party integrations disconnected
    private boolean stravaDisconnected;
    private boolean nikeDisconnected;
    private boolean garminDisconnected;

    // Audit trail
    private String auditLogId;

    // Summary statistics
    public Map<String, Integer> getDeletedEntitiesSummary() {
        return Map.ofEntries(
            Map.entry("runs", runsDeleted),
            Map.entry("userActivities", userActivitiesDeleted),
            Map.entry("userTrophies", userTrophiesDeleted),
            Map.entry("fitFileUploads", fitFileUploadsDeleted),
            Map.entry("dailyWinners", dailyWinnersDeleted),
            Map.entry("competitionParticipants", competitionParticipantsDeleted),
            Map.entry("competitionsReassigned", competitionsReassigned),
            Map.entry("routesAnonymized", routesAnonymized),
            Map.entry("subscriptions", subscriptionsDeleted),
            Map.entry("orders", ordersDeleted),
            Map.entry("payments", paymentsDeleted),
            Map.entry("passwordResetTokens", passwordResetTokensDeleted),
            Map.entry("avatarFiles", avatarFilesDeleted),
            Map.entry("fitFiles", fitFilesDeleted)
        );
    }
}
