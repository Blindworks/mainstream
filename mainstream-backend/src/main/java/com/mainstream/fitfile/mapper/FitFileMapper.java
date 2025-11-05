package com.mainstream.fitfile.mapper;

import com.mainstream.fitfile.dto.FitFileUploadDto;
import com.mainstream.fitfile.dto.FitFileUploadResponseDto;
import com.mainstream.fitfile.entity.FitFileUpload;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FitFileMapper {

    @Mapping(target = "distanceKm", expression = "java(fitFileUpload.getDistanceKm())")
    @Mapping(target = "avgPaceMinPerKm", expression = "java(fitFileUpload.getAvgPaceMinPerKm())")
    @Mapping(target = "avgSpeedKmh", expression = "java(fitFileUpload.getAvgSpeedKmh())")
    @Mapping(target = "maxSpeedKmh", expression = "java(fitFileUpload.getMaxSpeedKmh())")
    @Mapping(target = "formattedDuration", expression = "java(fitFileUpload.getFormattedDuration())")
    @Mapping(target = "isProcessed", expression = "java(fitFileUpload.isProcessed())")
    @Mapping(target = "isFailed", expression = "java(fitFileUpload.isFailed())")
    FitFileUploadDto toDto(FitFileUpload fitFileUpload);

    List<FitFileUploadDto> toDtoList(List<FitFileUpload> fitFileUploads);

    @Mapping(target = "id", source = "fitFileUpload.id")
    @Mapping(target = "originalFilename", source = "fitFileUpload.originalFilename")
    @Mapping(target = "fileSize", source = "fitFileUpload.fileSize")
    @Mapping(target = "processingStatus", source = "fitFileUpload.processingStatus")
    @Mapping(target = "errorMessage", source = "fitFileUpload.errorMessage")
    FitFileUploadResponseDto toResponseDto(FitFileUpload fitFileUpload, String message);

    @Mapping(target = "message", source = "message")
    FitFileUploadResponseDto toResponseDto(FitFileUpload fitFileUpload, String message, String errorMessage);
}