package com.jeondoksi.jeondoksi.domain.boss.repository;

import com.jeondoksi.jeondoksi.domain.boss.entity.Boss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BossRepository extends JpaRepository<Boss, Long> {
    @Query("SELECT b FROM Boss b WHERE b.isActive = true")
    List<Boss> findAllActive();
}
