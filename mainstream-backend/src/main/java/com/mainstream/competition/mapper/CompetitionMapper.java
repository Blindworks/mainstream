package com.mainstream.competition.mapper;

import com.mainstream.competition.dto.CompetitionDto;
import com.mainstream.competition.dto.CompetitionParticipantDto;
import com.mainstream.competition.dto.CompetitionSummaryDto;
import com.mainstream.competition.dto.LeaderboardEntryDto;
import com.mainstream.competition.entity.Competition;
import com.mainstream.competition.entity.CompetitionParticipant;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CompetitionMapper {

    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    @Mapping(target = "currentParticipants", ignore = true)
    @Mapping(target = "isUserParticipating", ignore = true)
    CompetitionDto toDto(Competition competition);

    @Mapping(target = "currentParticipants", ignore = true)
    @Mapping(target = "isUserParticipating", ignore = true)
    CompetitionSummaryDto toSummaryDto(Competition competition);

    List<CompetitionDto> toDtoList(List<Competition> competitions);

    List<CompetitionSummaryDto> toSummaryDtoList(List<Competition> competitions);

    @Mapping(source = "competition.id", target = "competitionId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    CompetitionParticipantDto toParticipantDto(CompetitionParticipant participant);

    List<CompetitionParticipantDto> toParticipantDtoList(List<CompetitionParticipant> participants);

    @Mapping(source = "participant.currentPosition", target = "position")
    @Mapping(source = "participant.user.id", target = "userId")
    @Mapping(source = "participant.user.fullName", target = "userName")
    @Mapping(source = "participant.currentScore", target = "score")
    @Mapping(target = "performanceUnit", ignore = true)
    @Mapping(target = "isCurrentUser", ignore = true)
    LeaderboardEntryDto toLeaderboardEntry(CompetitionParticipant participant);

    List<LeaderboardEntryDto> toLeaderboardEntryList(List<CompetitionParticipant> participants);
}
