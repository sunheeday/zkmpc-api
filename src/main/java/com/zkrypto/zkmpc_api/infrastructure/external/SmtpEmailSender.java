package com.zkrypto.zkmpc_api.infrastructure.external;

import com.zkrypto.zkmpc_api.domain.member.domain.service.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final String FROM_ADDRESS;

    public SmtpEmailSender(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.FROM_ADDRESS = fromAddress;
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(FROM_ADDRESS); // 발신자 설정
        message.setTo(toEmail);       // 수신자 설정
        message.setSubject("[zkmpc-wallet] 이메일 인증 코드가 도착했습니다.");
        message.setText("안녕하세요. 사용자 인증 위한 인증 코드는 다음과 같습니다:\n\n" +
                "인증 코드: " + code + "\n\n" +
                "본 코드는 5분간 유효합니다."
        );

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}