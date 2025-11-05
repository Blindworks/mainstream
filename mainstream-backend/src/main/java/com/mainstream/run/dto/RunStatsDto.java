package com.mainstream.run.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunStatsDto {
    
    private Integer totalRuns;
    private Double totalDistance; // km
    private Integer totalDuration; // seconds
    private Double averagePace; // seconds per km
    private Double bestPace; // seconds per km (fastest)
    private Double longestRun; // km
    private String formattedTotalDuration;
    private String formattedAveragePace;
    private String formattedBestPace;
    
    // Calculated fields
    public String getFormattedTotalDuration() {
        if (totalDuration == null) return "00:00:00";
        
        int hours = totalDuration / 3600;
        int minutes = (totalDuration % 3600) / 60;
        int seconds = totalDuration % 60;
        
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
    
    public String getFormattedAveragePace() {
        if (averagePace == null) return "0:00 min/km";
        
        int totalSeconds = averagePace.intValue();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        
        return String.format("%d:%02d min/km", minutes, seconds);
    }
    
    public String getFormattedBestPace() {
        if (bestPace == null) return "0:00 min/km";
        
        int totalSeconds = bestPace.intValue();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        
        return String.format("%d:%02d min/km", minutes, seconds);
    }
}