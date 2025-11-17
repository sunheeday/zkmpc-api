package com.zkrypto.zkmpc_api.infrastructure.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "smtp.from-address=k2mseungh2@gmail.com"
})
class SmtpEmailSenderIntegrationTest {

    // MailHog 컨테이너 설정: SMTP 포트(1025)와 HTTP UI 포트(8025) 노출
    @Container
    private static final GenericContainer<?> MAILHOG_CONTAINER = new GenericContainer<>("mailhog/mailhog")
            .withExposedPorts(1025, 8025);

    @Autowired
    private SmtpEmailSender smtpEmailSender;

    private final String TO_EMAIL = "recipient@example.com";
    private final String CODE = "543210";

    // Spring Mail Sender가 MailHog의 동적 포트를 사용하도록 설정
    @DynamicPropertySource
    static void mailHogProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", MAILHOG_CONTAINER::getHost);
        registry.add("spring.mail.port", () -> MAILHOG_CONTAINER.getMappedPort(1025).toString());
    }

    @Test
    @DisplayName("인증 코드 이메일 발송 성공 및 MailHog 수신 확인")
    void sendVerificationCode_and_verify_success() throws IOException, InterruptedException {
        // 1. When: 실제 이메일 발송 로직 호출
        smtpEmailSender.sendVerificationCode(TO_EMAIL, CODE);

        // 2. Then: MailHog의 API를 호출하여 이메일 수신 여부와 내용 검증
        HttpClient client = HttpClient.newHttpClient();

        // MailHog의 HTTP API 포트(8025)를 사용하여 최근 수신된 메시지를 조회
        String mailhogApiUrl = String.format("http://%s:%d/api/v2/messages",
                MAILHOG_CONTAINER.getHost(),
                MAILHOG_CONTAINER.getMappedPort(8025));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mailhogApiUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // A. 수신 확인
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(TO_EMAIL); // 수신자 이메일이 포함되어야 함

        // B. 내용 검증 (MailHog JSON 응답에서 텍스트 확인)
        assertThat(response.body()).contains("인증 코드는 다음과 같습니다:");
        assertThat(response.body()).contains("인증 코드: " + CODE);
    }
}