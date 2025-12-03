package com.jeondoksi.jeondoksi.domain.guild.repository;

import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GuildRepository extends JpaRepository<Guild, Long> {
    boolean existsByName(String name);

    Optional<Guild> findByJoinCode(String joinCode);

    @Query("SELECT g FROM Guild g WHERE g.isPrivate = false")
    Page<Guild> findAllByIsPrivateFalse(Pageable pageable);

    @Query("SELECT g FROM Guild g WHERE g.name LIKE %:keyword% AND g.isPrivate = false")
    Page<Guild> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
