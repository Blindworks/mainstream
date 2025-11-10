package com.mainstream.activity.service.trophy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents progress towards earning a trophy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrophyProgress {

    /**
     * Current progress value (e.g., 5000 meters, 5 days, 3 routes)
     */
    private long currentValue;

    /**
     * Target value to achieve the trophy (e.g., 10000 meters, 7 days, 5 routes)
     */
    private long targetValue;

    /**
     * Progress percentage (0-100)
     */
    public int getPercentage() {
        if (targetValue == 0) {
            return 0;
        }
        return (int) Math.min(100, (currentValue * 100) / targetValue);
    }

    /**
     * Check if target is reached
     */
    public boolean isComplete() {
        return currentValue >= targetValue;
    }
}
