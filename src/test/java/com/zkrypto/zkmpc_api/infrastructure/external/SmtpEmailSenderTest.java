package com.zkrypto.zkmpc_api.infrastructure.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
public class SmtpEmailSenderTest {

        @Autowired
        private SmtpEmailSender smtpEmailSender;

        private final String YOUR_EMAIL_TO_RECEIVE = "k2mseungh2@gmail.com";
        private final String TEST_CODE = "999999";

        @Test
        @DisplayName("실제 SMTP 서버를 이용한 이메일 발송 테스트")
        void sendVerificationCode_manualTest() {
            // Given
            System.out.println(">>> 발송 테스트 시작. 이메일: " + YOUR_EMAIL_TO_RECEIVE);

            // When & Then
            assertDoesNotThrow(() -> {
                smtpEmailSender.sendVerificationCode(YOUR_EMAIL_TO_RECEIVE, TEST_CODE);
            }, "이메일 발송이 실패했습니다. application.yml의 SMTP 설정을 확인하세요.");

            System.out.println(">>> 이메일 발송 성공! " + YOUR_EMAIL_TO_RECEIVE + " 에서 확인하세요.");
            System.out.println(">>> 테스트 코드는: " + TEST_CODE);
        }
}
