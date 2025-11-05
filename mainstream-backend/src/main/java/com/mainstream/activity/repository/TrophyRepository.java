package com.mainstream.activity.repository;

import com.mainstream.activity.entity.Trophy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrophyRepository extends JpaRepository<Trophy, Long> {

    Optional<Trophy> findByCode(String code);

    List<Trophy> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Trophy> findByTypeAndIsActiveTrue(Trophy.TrophyType type);
}
