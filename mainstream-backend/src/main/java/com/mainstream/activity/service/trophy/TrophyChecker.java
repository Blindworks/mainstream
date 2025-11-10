package com.mainstream.activity.service.trophy;

import com.mainstream.activity.entity.Trophy;
import com.mainstream.activity.entity.UserActivity;
import com.mainstream.user.entity.User;

/**
 * Interface for trophy criteria checkers.
 * Each trophy type has its own checker implementation.
 */
public interface TrophyChecker {

    /**
     * Check if trophy criteria is met for a user's activity.
     *
     * @param user The user to check
     * @param activity The activity that triggered the check (can be null for some trophy types)
     * @param trophy The trophy definition with criteria configuration
     * @return true if trophy should be awarded
     */
    boolean checkCriteria(User user, UserActivity activity, Trophy trophy);

    /**
     * Calculate progress towards trophy achievement.
     *
     * @param user The user to check
     * @param trophy The trophy definition
     * @return Progress information (current value, target value)
     */
    TrophyProgress calculateProgress(User user, Trophy trophy);

    /**
     * Check if this checker supports the given trophy type.
     *
     * @param type The trophy type
     * @return true if this checker can handle the trophy type
     */
    boolean supports(Trophy.TrophyType type);
}
