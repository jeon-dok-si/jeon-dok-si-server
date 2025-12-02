package com.jeondoksi.jeondoksi.domain.user.service;

import com.jeondoksi.jeondoksi.domain.user.dto.LoginRequest;
import com.jeondoksi.jeondoksi.domain.user.dto.LoginResponse;
import com.jeondoksi.jeondoksi.domain.user.dto.SignupRequest;
import com.jeondoksi.jeondoksi.domain.user.dto.UserResponse;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import com.jeondoksi.jeondoksi.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final com.jeondoksi.jeondoksi.domain.gamification.service.CharacterService characterService;
    private final com.jeondoksi.jeondoksi.domain.gamification.repository.CharacterRepository characterRepository;

    // 회원가입
    @Transactional
    public Long signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        User savedUser = userRepository.save(user);
        characterService.grantBasicCharacter(savedUser);

        return savedUser.getUserId();
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());
        return new LoginResponse(token);
    }

    // 내 정보 조회
    public UserResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 장착한 캐릭터 조회
        com.jeondoksi.jeondoksi.domain.gamification.entity.Character equippedCharacter = characterRepository
                .findByUserAndIsEquippedTrue(user)
                .orElse(null);

        return UserResponse.from(user, equippedCharacter);
    }
}
