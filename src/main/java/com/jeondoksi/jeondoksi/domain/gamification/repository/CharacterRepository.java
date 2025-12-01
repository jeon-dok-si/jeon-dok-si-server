package com.jeondoksi.jeondoksi.domain.gamification.repository;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findAllByUser(User user);
}
