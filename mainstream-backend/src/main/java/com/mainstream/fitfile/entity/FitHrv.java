package com.mainstream.fitfile.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store Heart Rate Variability (HRV) data from FIT files.
 * HRV is important for training load and recovery analysis.
 */
@Entity
@Table(name = "fit_hrv")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FitHrv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fit_file_upload_id", nullable = false)
    private FitFileUpload fitFileUpload;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // R-R interval data (milliseconds between heartbeats)
    @Column(name = "time", columnDefinition = "DECIMAL(8,3)")
    private Double time; // seconds

    // Multiple R-R intervals can be stored in a single HRV message
    @ElementCollection
    @CollectionTable(name = "fit_hrv_intervals", 
                    joinColumns = @JoinColumn(name = "fit_hrv_id"))
    @Column(name = "interval_ms")
    private java.util.List<Integer> intervals; // R-R intervals in milliseconds

    // Calculated HRV metrics (if available from device)
    @Column(name = "rmssd")
    private Double rmssd; // Root Mean Square of Successive Differences

    @Column(name = "pnn50")
    private Double pnn50; // Percentage of consecutive R-R intervals differing by >50ms

    @Column(name = "stress_score")
    private Integer stressScore; // Device-calculated stress score

    @Column(name = "hrv_status")
    private String hrvStatus; // Device HRV status

    // Utility methods
    public Double getAverageRRInterval() {
        if (intervals == null || intervals.isEmpty()) {
            return null;
        }
        return intervals.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
    }

    public Integer getHeartRateFromRR() {
        Double avgRR = getAverageRRInterval();
        if (avgRR != null && avgRR > 0) {
            return (int) Math.round(60000.0 / avgRR); // Convert ms to BPM
        }
        return null;
    }

    public boolean hasValidData() {
        return intervals != null && !intervals.isEmpty() && 
               intervals.stream().anyMatch(interval -> interval > 300 && interval < 2000);
    }
}