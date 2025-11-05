package com.mainstream.activity.repository;

import com.mainstream.activity.entity.RouteTrackPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteTrackPointRepository extends JpaRepository<RouteTrackPoint, Long> {
}
