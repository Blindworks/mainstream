package com.mainstream.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audit log entity for GDPR-compliant account deletion tracking.
 * Stores anonymized information about deleted accounts for compliance purposes.
 */
@Entity
@Table(name = "account_deletion_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AccountDeletionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deleted_user_id", nullable = false)
    private Long deletedUserId;

    @Column(name = "email_hash", nullable = false, length = 64)
    private String emailHash; // SHA-256 hash of email for reference without storing PII

    @Column(name = "deletion_reason", length = 255)
    private String deletionReason;

    @Column(name = "requested_by")
    private Long requestedBy; // Admin user ID if admin-initiated, or same as deletedUserId if self-service

    @Enumerated(EnumType.STRING)
    @Column(name = "deletion_type", nullable = false)
    private DeletionType deletionType;

    @Column(name = "deleted_entities_summary", columnDefinition = "TEXT")
    private String deletedEntitiesSummary; // JSON string with counts

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreatedDate
    @Column(name = "deleted_at", nullable = false, updatable = false)
    private LocalDateTime deletedAt;

    @Column(name = "retention_period_days")
    @Builder.Default
    private Integer retentionPeriodDays = 2555; // 7 years for GDPR compliance

    @Column(name = "legal_basis", length = 100)
    private String legalBasis; // e.g., "Art. 17 GDPR - Right to erasure"

    @Column(name = "data_categories_deleted", columnDefinition = "TEXT")
    private String dataCategoriesDeleted; // JSON list of data categories

    @Column(name = "third_party_notifications", columnDefinition = "TEXT")
    private String thirdPartyNotifications; // JSON: which third parties were notified

    public enum DeletionType {
        SELF_SERVICE,      // User requested own deletion
        ADMIN_INITIATED,   // Admin deleted the account
        AUTOMATED,         // System automated deletion (e.g., after inactivity)
        LEGAL_REQUEST      // Legal/regulatory request
    }
}
