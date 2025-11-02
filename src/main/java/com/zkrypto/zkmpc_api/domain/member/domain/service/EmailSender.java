package com.zkrypto.zkmpc_api.domain.member.domain.service;

public interface EmailSender {
    /**
     * 특정 이메일 주소로 인증 코드를 발송합니다.
     * @param toEmail 수신자 이메일 주소
     * @param code 발송할 6자리 인증 코드
     */
    void sendVerificationCode(String toEmail, String code);
}
