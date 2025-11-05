package com.mainstream.activity.repository;

import com.mainstream.activity.entity.PredefinedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredefinedRouteRepository extends JpaRepository<PredefinedRoute, Long> {

    List<PredefinedRoute> findByIsActiveTrue();

    Optional<PredefinedRoute> findByName(String name);

    boolean existsByName(String name);
}
