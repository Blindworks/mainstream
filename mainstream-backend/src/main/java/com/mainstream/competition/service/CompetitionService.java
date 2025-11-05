package com.mainstream.competition.service;

import com.mainstream.competition.dto.CompetitionDto;
import com.mainstream.competition.dto.CompetitionParticipantDto;
import com.mainstream.competition.dto.CompetitionSummaryDto;
import com.mainstream.competition.dto.LeaderboardEntryDto;
import com.mainstream.competition.entity.Competition;
import com.mainstream.competition.entity.CompetitionParticipant;
import com.mainstream.competition.mapper.CompetitionMapper;
import com.mainstream.competition.repository.CompetitionParticipantRepository;
import com.mainstream.competition.repository.CompetitionRepository;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final CompetitionMapper competitionMapper;

    /**
     * Get all competitions with pagination
     */
    @Transactional(readOnly = true)
    public Page<CompetitionSummaryDto> getAllCompetitions(Long userId, Pageable pageable) {
        log.debug("Fetching all competitions for user: {}", userId);

        Page<Competition> competitions = competitionRepository.findAll(pageable);

        return competitions.map(competition -> {
            CompetitionSummaryDto dto = competitionMapper.toSummaryDto(competition);
            enrichCompetitionDto(dto, competition.getId(), userId);
            return dto;
        });
    }

    /**
     * Get competitions by status
     */
    @Transactional(readOnly = true)
    public Page<CompetitionSummaryDto> getCompetitionsByStatus(
            Competition.CompetitionStatus status,
            Long userId,
            Pageable pageable) {
        log.debug("Fetching competitions with status: {} for user: {}", status, userId);

        Page<Competition> competitions = competitionRepository.findByStatus(status, pageable);

        return competitions.map(competition -> {
            CompetitionSummaryDto dto = competitionMapper.toSummaryDto(competition);
            enrichCompetitionDto(dto, competition.getId(), userId);
            return dto;
        });
    }

    /**
     * Get competition by ID
     */
    @Transactional(readOnly = true)
    public CompetitionDto getCompetitionById(Long competitionId, Long userId) {
        log.debug("Fetching competition {} for user {}", competitionId, userId);

        Competition competition = competitionRepository.findById(competitionId)
            .orElseThrow(() -> new RuntimeException("Competition not found with id: " + competitionId));

        CompetitionDto dto = competitionMapper.toDto(competition);
        enrichCompetitionDto(dto, competitionId, userId);

        return dto;
    }

    /**
     * Get competitions user is participating in
     */
    @Transactional(readOnly = true)
    public List<CompetitionDto> getUserCompetitions(Long userId) {
        log.debug("Fetching competitions for user: {}", userId);

        List<CompetitionParticipant> participants = participantRepository.findByUserIdOrderByJoinedDateDesc(userId);

        return participants.stream()
            .map(participant -> {
                CompetitionDto dto = competitionMapper.toDto(participant.getCompetition());
                enrichCompetitionDto(dto, participant.getCompetition().getId(), userId);
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get active competitions
     */
    @Transactional(readOnly = true)
    public List<CompetitionDto> getActiveCompetitions(Long userId) {
        log.debug("Fetching active competitions for user: {}", userId);

        List<Competition> competitions = competitionRepository.findActiveCompetitions(LocalDateTime.now());

        return competitions.stream()
            .map(competition -> {
                CompetitionDto dto = competitionMapper.toDto(competition);
                enrichCompetitionDto(dto, competition.getId(), userId);
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Join a competition
     */
    public CompetitionParticipantDto joinCompetition(Long competitionId, Long userId) {
        log.info("User {} joining competition {}", userId, competitionId);

        // Check if competition exists
        Competition competition = competitionRepository.findById(competitionId)
            .orElseThrow(() -> new RuntimeException("Competition not found with id: " + competitionId));

        // Check if already participating
        if (participantRepository.existsByCompetitionIdAndUserId(competitionId, userId)) {
            throw new RuntimeException("User is already participating in this competition");
        }

        // Check if competition is full
        if (competition.getMaxParticipants() != null) {
            long currentCount = participantRepository.countByCompetitionId(competitionId);
            if (currentCount >= competition.getMaxParticipants()) {
                throw new RuntimeException("Competition is full");
            }
        }

        // Check if competition is open for registration
        if (competition.getStatus() != Competition.CompetitionStatus.UPCOMING &&
            competition.getStatus() != Competition.CompetitionStatus.ACTIVE) {
            throw new RuntimeException("Competition is not open for registration");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Create participant
        CompetitionParticipant participant = new CompetitionParticipant();
        participant.setCompetition(competition);
        participant.setUser(user);
        participant.setJoinedDate(LocalDateTime.now());
        participant.setStatus(CompetitionParticipant.ParticipantStatus.REGISTERED);

        CompetitionParticipant saved = participantRepository.save(participant);
        log.info("User {} successfully joined competition {}", userId, competitionId);

        return competitionMapper.toParticipantDto(saved);
    }

    /**
     * Leave a competition
     */
    public void leaveCompetition(Long competitionId, Long userId) {
        log.info("User {} leaving competition {}", userId, competitionId);

        CompetitionParticipant participant = participantRepository
            .findByCompetitionIdAndUserId(competitionId, userId)
            .orElseThrow(() -> new RuntimeException("User is not participating in this competition"));

        // Check if competition has started
        Competition competition = participant.getCompetition();
        if (competition.getStartDate().isBefore(LocalDateTime.now())) {
            // If started, mark as withdrawn instead of deleting
            participant.setStatus(CompetitionParticipant.ParticipantStatus.WITHDRAWN);
            participantRepository.save(participant);
        } else {
            // If not started, can delete
            participantRepository.delete(participant);
        }

        log.info("User {} successfully left competition {}", userId, competitionId);
    }

    /**
     * Get competition leaderboard
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(Long competitionId, Long userId) {
        log.debug("Fetching leaderboard for competition {}", competitionId);

        List<CompetitionParticipant> participants = participantRepository.getLeaderboard(competitionId);

        return participants.stream()
            .map(participant -> {
                LeaderboardEntryDto dto = competitionMapper.toLeaderboardEntry(participant);
                dto.setIsCurrentUser(participant.getUser().getId().equals(userId));
                // Set performance unit based on competition type
                // This would be enhanced based on actual competition logic
                dto.setPerformanceUnit("points");
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get all participants for a competition
     */
    @Transactional(readOnly = true)
    public List<CompetitionParticipantDto> getParticipants(Long competitionId) {
        log.debug("Fetching participants for competition {}", competitionId);

        List<CompetitionParticipant> participants = participantRepository
            .findByCompetitionIdOrderByCurrentPositionAsc(competitionId);

        return competitionMapper.toParticipantDtoList(participants);
    }

    /**
     * Search competitions
     */
    @Transactional(readOnly = true)
    public Page<CompetitionSummaryDto> searchCompetitions(String searchTerm, Long userId, Pageable pageable) {
        log.debug("Searching competitions with term: {}", searchTerm);

        Page<Competition> competitions = competitionRepository.searchCompetitions(searchTerm, pageable);

        return competitions.map(competition -> {
            CompetitionSummaryDto dto = competitionMapper.toSummaryDto(competition);
            enrichCompetitionDto(dto, competition.getId(), userId);
            return dto;
        });
    }

    /**
     * Enrich competition DTO with participant count and user participation status
     */
    private void enrichCompetitionDto(CompetitionSummaryDto dto, Long competitionId, Long userId) {
        long participantCount = participantRepository.countByCompetitionId(competitionId);
        dto.setCurrentParticipants((int) participantCount);

        boolean isParticipating = participantRepository.existsByCompetitionIdAndUserId(competitionId, userId);
        dto.setIsUserParticipating(isParticipating);
    }

    /**
     * Enrich full competition DTO
     */
    private void enrichCompetitionDto(CompetitionDto dto, Long competitionId, Long userId) {
        long participantCount = participantRepository.countByCompetitionId(competitionId);
        dto.setCurrentParticipants((int) participantCount);

        boolean isParticipating = participantRepository.existsByCompetitionIdAndUserId(competitionId, userId);
        dto.setIsUserParticipating(isParticipating);
    }
}
