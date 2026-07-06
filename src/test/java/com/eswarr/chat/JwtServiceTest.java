package com.eswarr.chat;

import com.eswarr.chat.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
            "test-secret-key-minimum-32-characters-long-for-hmac");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    void generateToken_producesValidToken() {
        String token = jwtService.generateToken("Alice");
        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void extractUserName_returnsCorrectName() {
        String token = jwtService.generateToken("Bob");
        assertThat(jwtService.extractUserName(token)).isEqualTo("Bob");
    }

    @Test
    void extractUserId_returnsNonNullId() {
        String token = jwtService.generateToken("Charlie");
        assertThat(jwtService.extractUserId(token)).isNotBlank();
    }

    @Test
    void differentUsers_getDifferentUserIds() {
        String token1 = jwtService.generateToken("Dave");
        String token2 = jwtService.generateToken("Dave"); // same name, different session
        assertThat(jwtService.extractUserId(token1)).isNotEqualTo(jwtService.extractUserId(token2));
    }

    @Test
    void isValid_invalidToken_returnsFalse() {
        assertThat(jwtService.isValid("not.a.valid.token")).isFalse();
    }

    @Test
    void isValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken("Eve");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isValid(tampered)).isFalse();
    }
}
