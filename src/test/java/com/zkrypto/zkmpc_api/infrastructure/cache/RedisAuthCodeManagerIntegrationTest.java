package com.zkrypto.zkmpc_api.infrastructure.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
// RedisConfig 등을 import하여 실제 Bean을 사용하도록 설정 (필요한 경우)
@Import(RedisAuthCodeManager.class)
class RedisAuthCodeManagerIntegrationTest {

    // 6379 포트로 Redis 컨테이너 생성
    @Container
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private RedisAuthCodeManager redisAuthCodeManager;

    private final String TEST_EMAIL = "integration_test@example.com";
    private final String TEST_CODE = "987654";

    // 런타임에 Spring 설정을 컨테이너 포트로 덮어쓰기
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Test
    @DisplayName("인증 코드 저장 및 조회 성공 (실제 Redis 동작 검증)")
    void saveAndGet_success() throws InterruptedException {
        // Given
        // save 로직은 내부적으로 TTL(Time To Live)을 Duration.ofMinutes(5)로 가정합니다.
        redisAuthCodeManager.save(TEST_EMAIL, TEST_CODE);

        // When: 실제 Redis에서 값을 조회
        Optional<String> result = redisAuthCodeManager.get(TEST_EMAIL);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TEST_CODE);
    }

    @Test
    @DisplayName("인증 코드 조회 실패 (삭제 후)")
    void remove_success() {
        // Given
        redisAuthCodeManager.save(TEST_EMAIL, TEST_CODE);

        // When
        redisAuthCodeManager.remove(TEST_EMAIL);
        Optional<String> result = redisAuthCodeManager.get(TEST_EMAIL);

        // Then
        assertThat(result).isEmpty();
    }
}