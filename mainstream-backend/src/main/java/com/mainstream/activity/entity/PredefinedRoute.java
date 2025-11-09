package com.mainstream.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a predefined running route loaded from GPX file.
 * Used as reference for matching user activities.
 */
@Entity
@Table(name = "predefined_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PredefinedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * URL or path to the route image
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * Original GPX filename
     */
    @Column(nullable = false)
    private String originalFilename;

    /**
     * Total distance of the route in meters
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceMeters;

    /**
     * Elevation gain in meters
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal elevationGainMeters;

    /**
     * Elevation loss in meters
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal elevationLossMeters;

    /**
     * Starting point latitude
     */
    @Column(precision = 10, scale = 7)
    private BigDecimal startLatitude;

    /**
     * Starting point longitude
     */
    @Column(precision = 10, scale = 7)
    private BigDecimal startLongitude;

    /**
     * Whether this route is active and should be used for matching
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * GPS trackpoints from the GPX file
     */
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    private List<RouteTrackPoint> trackPoints = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Add a track point to this route
     */
    public void addTrackPoint(RouteTrackPoint trackPoint) {
        trackPoints.add(trackPoint);
        trackPoint.setRoute(this);
    }
}
