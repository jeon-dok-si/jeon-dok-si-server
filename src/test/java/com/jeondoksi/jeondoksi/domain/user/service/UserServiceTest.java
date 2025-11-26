package com.jeondoksi.jeondoksi.domain.user.service;

import com.jeondoksi.jeondoksi.domain.user.dto.LoginRequest;
import com.jeondoksi.jeondoksi.domain.user.dto.LoginResponse;
import com.jeondoksi.jeondoksi.domain.user.dto.SignupRequest;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import com.jeondoksi.jeondoksi.global.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "password");
        ReflectionTestUtils.setField(request, "nickname", "nickname");

        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        User user = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .nickname(request.getNickname())
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.save(any())).willReturn(user);

        // when
        Long userId = userService.signup(request);

        // then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_duplicate_email() {
        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "test@example.com");

        given(userRepository.existsByEmail(any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_DUPLICATION);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "password");

        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("nickname")
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(jwtUtil.generateToken(any(), any())).willReturn("accessToken");

        // when
        LoginResponse response = userService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
    }
}
