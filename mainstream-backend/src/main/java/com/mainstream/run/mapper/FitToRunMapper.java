package com.mainstream.run.mapper;

import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.run.dto.GpsPointDto;
import com.mainstream.run.dto.RunDto;
import com.mainstream.run.entity.Run;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Component
public interface FitToRunMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "fitFile", qualifiedByName = "generateTitle")
    @Mapping(target = "description", source = "fitFile", qualifiedByName = "generateDescription")
    @Mapping(target = "startTime", source = "fitFile.activityStartTime")
    @Mapping(target = "endTime", source = "fitFile.activityEndTime")
    @Mapping(target = "durationSeconds", source = "fitFile.totalTimerTime")
    @Mapping(target = "distanceMeters", source = "fitFile.totalDistance")
    @Mapping(target = "averagePaceSecondsPerKm", source = "fitFile", qualifiedByName = "calculatePaceFromSpeed")
    @Mapping(target = "maxSpeedKmh", source = "fitFile", qualifiedByName = "convertMaxSpeedToKmh")
    @Mapping(target = "averageSpeedKmh", source = "fitFile", qualifiedByName = "convertAvgSpeedToKmh")
    @Mapping(target = "caloriesBurned", source = "fitFile.totalCalories")
    @Mapping(target = "elevationGainMeters", source = "fitFile.totalAscent")
    @Mapping(target = "elevationLossMeters", source = "fitFile.totalDescent")
    @Mapping(target = "runType", source = "fitFile", qualifiedByName = "determineRunType")
    @Mapping(target = "status", constant = "COMPLETED")
    @Mapping(target = "weatherCondition", source = "fitFile.weatherCondition")
    @Mapping(target = "temperatureCelsius", source = "fitFile.avgTemperature")
    @Mapping(target = "isPublic", constant = "true")
    @Mapping(target = "routeId", ignore = true)
    @Mapping(target = "gpsPoints", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Run fitFileToRun(FitFileUpload fitFile);

    @Mapping(target = "id", source = "run.id")
    @Mapping(target = "userId", source = "run.userId")
    @Mapping(target = "title", source = "run.title")
    @Mapping(target = "description", source = "run.description")
    @Mapping(target = "startTime", source = "run.startTime")
    @Mapping(target = "endTime", source = "run.endTime")
    @Mapping(target = "durationSeconds", source = "run.durationSeconds")
    @Mapping(target = "distanceMeters", source = "run.distanceMeters")
    @Mapping(target = "averagePaceSecondsPerKm", source = "run.averagePaceSecondsPerKm")
    @Mapping(target = "averageSpeedKmh", source = "run.averageSpeedKmh")
    @Mapping(target = "maxSpeedKmh", source = "run.maxSpeedKmh")
    @Mapping(target = "caloriesBurned", source = "run.caloriesBurned")
    @Mapping(target = "elevationGainMeters", source = "run.elevationGainMeters")
    @Mapping(target = "elevationLossMeters", source = "run.elevationLossMeters")
    @Mapping(target = "runType", source = "run.runType")
    @Mapping(target = "status", source = "run.status")
    @Mapping(target = "weatherCondition", source = "run.weatherCondition")
    @Mapping(target = "temperatureCelsius", source = "run.temperatureCelsius")
    @Mapping(target = "isPublic", source = "run.isPublic")
    @Mapping(target = "createdAt", source = "run.createdAt")
    @Mapping(target = "updatedAt", source = "run.updatedAt")
    @Mapping(target = "formattedDuration", source = "run", qualifiedByName = "formatDuration")
    @Mapping(target = "distanceKm", source = "run", qualifiedByName = "convertDistanceToKm")
    @Mapping(target = "formattedPace", source = "run", qualifiedByName = "formatPace")
    @Mapping(target = "averageHeartRate", source = "fitFile.avgHeartRate")
    @Mapping(target = "maxHeartRate", source = "fitFile.maxHeartRate")
    @Mapping(target = "averageCadence", source = "fitFile.avgCadence")
    @Mapping(target = "maxCadence", source = "fitFile.maxCadence")
    @Mapping(target = "totalSteps", source = "fitFile.totalSteps")
    @Mapping(target = "averageStrideLength", source = "fitFile.avgStrideLength")
    @Mapping(target = "averageRunningPower", source = "fitFile.avgRunningPower")
    @Mapping(target = "maxRunningPower", source = "fitFile.maxRunningPower")
    @Mapping(target = "averageVerticalOscillation", source = "fitFile.avgVerticalOscillation")
    @Mapping(target = "averageGroundContactTime", source = "fitFile.avgGroundContactTime")
    @Mapping(target = "averageGroundContactBalance", source = "fitFile.avgGroundContactBalance")
    @Mapping(target = "trainingStressScore", source = "fitFile.trainingStressScore")
    @Mapping(target = "hrZone1Time", source = "fitFile.hrZone1Time")
    @Mapping(target = "hrZone2Time", source = "fitFile.hrZone2Time")
    @Mapping(target = "hrZone3Time", source = "fitFile.hrZone3Time")
    @Mapping(target = "hrZone4Time", source = "fitFile.hrZone4Time")
    @Mapping(target = "hrZone5Time", source = "fitFile.hrZone5Time")
    @Mapping(target = "deviceManufacturer", source = "fitFile.fitManufacturer")
    @Mapping(target = "deviceProduct", source = "fitFile.fitProduct")
    @Mapping(target = "deviceSerial", source = "fitFile.fitDeviceSerial")
    @Mapping(target = "dataSource", constant = "FIT")
    @Mapping(target = "fitFileUploadId", source = "fitFile.id")
    @Mapping(target = "gpsPoints", source = "fitFile.trackPoints", qualifiedByName = "mapTrackPointsToGpsPoints")
    RunDto toRunDto(Run run, FitFileUpload fitFile);

    @Mapping(target = "latitude", source = "positionLat")
    @Mapping(target = "longitude", source = "positionLong")
    @Mapping(target = "altitude", source = "enhancedAltitude")
    @Mapping(target = "speedKmh", source = "trackPoint", qualifiedByName = "calculateSpeedKmh")
    @Mapping(target = "gpsFixType", source = "gpsFixType")
    GpsPointDto trackPointToGpsPointDto(FitTrackPoint trackPoint);

    // Named methods for custom mappings
    @Named("generateTitle")
    default String generateTitle(FitFileUpload fitFile) {
        if (fitFile.getActivityStartTime() != null) {
            String sport = fitFile.getSport() != null ? fitFile.getSport() : "Run";
            String date = fitFile.getActivityStartTime().toLocalDate().toString();
            return String.format("%s - %s", sport, date);
        }
        return "Training Session";
    }

    @Named("generateDescription")
    default String generateDescription(FitFileUpload fitFile) {
        StringBuilder desc = new StringBuilder();
        if (fitFile.getDistanceKm() != null) {
            desc.append(String.format("Distance: %.2f km", fitFile.getDistanceKm()));
        }
        if (fitFile.getFormattedDuration() != null) {
            if (desc.length() > 0) desc.append(" | ");
            desc.append(String.format("Duration: %s", fitFile.getFormattedDuration()));
        }
        if (fitFile.getAvgPaceMinPerKm() != null) {
            if (desc.length() > 0) desc.append(" | ");
            desc.append(String.format("Avg Pace: %.2f min/km", fitFile.getAvgPaceMinPerKm()));
        }
        return desc.length() > 0 ? desc.toString() : "Imported from FIT file";
    }

    @Named("calculatePaceFromSpeed")
    default Double calculatePaceFromSpeed(FitFileUpload fitFile) {
        System.out.println("DEBUG: calculatePaceFromSpeed called with avgSpeed: " + fitFile.getAvgSpeed());
        if (fitFile.getAvgSpeed() != null && fitFile.getAvgSpeed().doubleValue() > 0) {
            double speedKmh = fitFile.getAvgSpeed().doubleValue() * 3.6;
            double paceMinPerKm = 60.0 / speedKmh;
            double paceSecondsPerKm = paceMinPerKm * 60;
            System.out.println("DEBUG: Calculated pace: " + paceSecondsPerKm + " seconds/km");
            return paceSecondsPerKm; // seconds per km
        }
        System.out.println("DEBUG: No valid avgSpeed, returning null");
        return null;
    }

    @Named("convertMaxSpeedToKmh")
    default BigDecimal convertMaxSpeedToKmh(FitFileUpload fitFile) {
        if (fitFile.getMaxSpeed() != null) {
            return BigDecimal.valueOf(fitFile.getMaxSpeed().doubleValue() * 3.6);
        }

        // Calculate from track points if missing
        if (fitFile.getTrackPoints() != null && !fitFile.getTrackPoints().isEmpty()) {
            Double maxSpeed = fitFile.getTrackPoints().stream()
                .map(tp -> tp.getEnhancedSpeed() != null ? tp.getEnhancedSpeed() : tp.getSpeed())
                .filter(speed -> speed != null && speed > 0)
                .max(Double::compareTo)
                .orElse(null);

            if (maxSpeed != null) {
                return BigDecimal.valueOf(maxSpeed * 3.6);
            }
        }

        return null;
    }

    @Named("convertAvgSpeedToKmh")
    default BigDecimal convertAvgSpeedToKmh(FitFileUpload fitFile) {
        if (fitFile.getAvgSpeed() != null) {
            return BigDecimal.valueOf(fitFile.getAvgSpeed().doubleValue() * 3.6);
        }

        // Calculate from track points if missing
        if (fitFile.getTrackPoints() != null && !fitFile.getTrackPoints().isEmpty()) {
            Double avgSpeed = fitFile.getTrackPoints().stream()
                .map(tp -> tp.getEnhancedSpeed() != null ? tp.getEnhancedSpeed() : tp.getSpeed())
                .filter(speed -> speed != null && speed > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

            if (avgSpeed > 0) {
                return BigDecimal.valueOf(avgSpeed * 3.6);
            }
        }

        return null;
    }

    @Named("determineRunType")
    default String determineRunType(FitFileUpload fitFile) {
        if (fitFile.getSubSport() != null) {
            switch (fitFile.getSubSport().toUpperCase()) {
                case "TREADMILL":
                    return "TREADMILL";
                case "TRAIL":
                    return "TRAIL";
                case "TRACK":
                    return "TRACK";
                default:
                    return "OUTDOOR";
            }
        }
        return "OUTDOOR";
    }

    @Named("formatDuration")
    default String formatDuration(Run run) {
        return run.getFormattedDuration();
    }

    @Named("convertDistanceToKm")
    default Double convertDistanceToKm(Run run) {
        return run.getDistanceKm();
    }

    @Named("formatPace")
    default String formatPace(Run run) {
        if (run.getAveragePaceSecondsPerKm() != null) {
            int totalSeconds = run.getAveragePaceSecondsPerKm().intValue();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%d:%02d min/km", minutes, seconds);
        }
        return null;
    }

    @Named("calculateSpeedKmh")
    default Double calculateSpeedKmh(FitTrackPoint trackPoint) {
        Double speed = trackPoint.getEnhancedSpeed() != null ? 
            trackPoint.getEnhancedSpeed() : trackPoint.getSpeed();
        return speed != null ? speed * 3.6 : null;
    }

    @Named("mapTrackPointsToGpsPoints")
    default List<GpsPointDto> mapTrackPointsToGpsPoints(List<FitTrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            return Collections.emptyList();
        }
        
        return trackPoints.stream()
            .filter(tp -> tp.hasValidGpsPosition())
            .sorted(Comparator.comparing(FitTrackPoint::getTimestamp))
            .limit(1000) // Limit GPS points for performance
            .map(this::trackPointToGpsPointDto)
            .collect(Collectors.toList());
    }

    @Named("formatFitDuration")
    default String formatFitDuration(FitFileUpload fitFile) {
        return fitFile.getFormattedDuration();
    }

    @Named("convertFitDistanceToKm")
    default Double convertFitDistanceToKm(FitFileUpload fitFile) {
        return fitFile.getDistanceKm();
    }

    @Named("formatFitPace")
    default String formatFitPace(FitFileUpload fitFile) {
        Double pace = fitFile.getAvgPaceMinPerKm();
        if (pace != null) {
            int minutes = pace.intValue();
            int seconds = (int) ((pace - minutes) * 60);
            return String.format("%d:%02d min/km", minutes, seconds);
        }
        return null;
    }

    // Direct mapping from FitFileUpload to RunDto
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "title", source = "fitFile", qualifiedByName = "generateTitle")
    @Mapping(target = "description", source = "fitFile", qualifiedByName = "generateDescription")
    @Mapping(target = "startTime", source = "activityStartTime")
    @Mapping(target = "endTime", source = "activityEndTime")
    @Mapping(target = "durationSeconds", source = "totalTimerTime")
    @Mapping(target = "formattedDuration", source = "fitFile", qualifiedByName = "formatFitDuration")
    @Mapping(target = "distanceMeters", source = "totalDistance")
    @Mapping(target = "distanceKm", source = "fitFile", qualifiedByName = "convertFitDistanceToKm")
    @Mapping(target = "averagePaceSecondsPerKm", source = "fitFile", qualifiedByName = "calculatePaceFromSpeed")
    @Mapping(target = "formattedPace", source = "fitFile", qualifiedByName = "formatFitPace")
    @Mapping(target = "averageSpeedKmh", source = "fitFile", qualifiedByName = "convertAvgSpeedToKmh")
    @Mapping(target = "maxSpeedKmh", source = "fitFile", qualifiedByName = "convertMaxSpeedToKmh")
    @Mapping(target = "caloriesBurned", source = "totalCalories")
    @Mapping(target = "averageHeartRate", source = "avgHeartRate")
    @Mapping(target = "maxHeartRate", source = "maxHeartRate")
    @Mapping(target = "averageCadence", source = "avgCadence")
    @Mapping(target = "maxCadence", source = "maxCadence")
    @Mapping(target = "totalSteps", source = "totalSteps")
    @Mapping(target = "averageStrideLength", source = "avgStrideLength")
    @Mapping(target = "elevationGainMeters", source = "totalAscent")
    @Mapping(target = "elevationLossMeters", source = "totalDescent")
    @Mapping(target = "runType", source = "fitFile", qualifiedByName = "determineRunType")
    @Mapping(target = "status", constant = "COMPLETED")
    @Mapping(target = "weatherCondition", source = "weatherCondition")
    @Mapping(target = "temperatureCelsius", source = "avgTemperature")
    @Mapping(target = "isPublic", constant = "true")
    @Mapping(target = "averageRunningPower", source = "avgRunningPower")
    @Mapping(target = "maxRunningPower", source = "maxRunningPower")
    @Mapping(target = "averageVerticalOscillation", source = "avgVerticalOscillation")
    @Mapping(target = "averageGroundContactTime", source = "avgGroundContactTime")
    @Mapping(target = "averageGroundContactBalance", source = "avgGroundContactBalance")
    @Mapping(target = "trainingStressScore", source = "trainingStressScore")
    @Mapping(target = "hrZone1Time", source = "hrZone1Time")
    @Mapping(target = "hrZone2Time", source = "hrZone2Time")
    @Mapping(target = "hrZone3Time", source = "hrZone3Time")
    @Mapping(target = "hrZone4Time", source = "hrZone4Time")
    @Mapping(target = "hrZone5Time", source = "hrZone5Time")
    @Mapping(target = "deviceManufacturer", source = "fitManufacturer")
    @Mapping(target = "deviceProduct", source = "fitProduct")
    @Mapping(target = "deviceSerial", source = "fitDeviceSerial")
    @Mapping(target = "dataSource", constant = "FIT")
    @Mapping(target = "fitFileUploadId", source = "id")
    @Mapping(target = "gpsPoints", source = "trackPoints", qualifiedByName = "mapTrackPointsToGpsPoints")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    RunDto fitFileToRunDto(FitFileUpload fitFile);
}