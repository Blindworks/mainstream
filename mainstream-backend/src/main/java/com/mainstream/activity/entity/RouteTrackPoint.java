package com.mainstream.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * GPS trackpoint from a predefined route.
 * Used for route matching against user activities.
 */
@Entity
@Table(name = "route_track_points", indexes = {
    @Index(name = "idx_route_sequence", columnList = "route_id,sequence_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteTrackPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private PredefinedRoute route;

    /**
     * Order of this point in the route
     */
    @Column(nullable = false)
    private Integer sequenceNumber;

    /**
     * Latitude in decimal degrees
     */
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    /**
     * Longitude in decimal degrees
     */
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    /**
     * Elevation in meters (optional)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal elevation;

    /**
     * Distance from route start in meters
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal distanceFromStartMeters;
}
