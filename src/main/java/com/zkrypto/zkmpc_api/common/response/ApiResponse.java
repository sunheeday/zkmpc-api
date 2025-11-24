package com.zkrypto.zkmpc_api.common.response;


import jnr.ffi.annotations.SaveError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private final String message;
    private final T data;

    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드 (데이터 포함)
     * 응답 명세: { "message": "Success", "data": ... }
     * @param data 실제 응답 데이터
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("Success", data);
    }

    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드
     * 응답 명세: { "message": "Success", "data": null }
     * @return ApiResponse 객체
     */
    public static ApiResponse<Void> success(Void data) {
        return new ApiResponse<>("Success", null);
    }

    // -- 에러 처리 로직 --

    /**
     * 실패 응답을 생성하는 정적 팩토리 메서드
     * @param message 에러 메시지
     * @param data 에러 상세 정보 (필요한 경우)
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    /**
     * 실패 응답을 생성하는 정적 팩토리 메서드 (데이터 없음)
     * @param message 에러 메시지
     * @return ApiResponse 객체
     */
    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(message, null);
    }
}
