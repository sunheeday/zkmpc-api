package com.zkrypto.zkmpc_api.domain.member.domain.service;
import java.util.Optional;

public interface AuthCodeManager {
    /**
     * 특정 이메일과 코드를 저장하고 만료 시간을 설정합니다.
     * @param email 사용자 이메일
     * @param code 발송된 인증 코드
     */
    void save(String email, String code);

    /**
     * 특정 이메일로 저장된 인증 코드를 조회합니다.
     * @param email 사용자 이메일
     * @return 저장된 코드 (없거나 만료 시 Optional.empty())
     */
    Optional<String> get(String email);

    /**
     * 인증 완료 후 코드를 저장소에서 제거합니다.
     * @param email 사용자 이메일
     */
    void remove(String email);

    /**
     * 6자리 인증 코드를 생성합니다.
     * @return 6자리 String 코드
     */
    String generateCode();
}
