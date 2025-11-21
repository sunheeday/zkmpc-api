package com.zkrypto.zkmpc_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF (Cross-Site Request Forgery) 보호 비활성화
                .csrf(csrf -> csrf.disable())

                // 2. HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable())

                // 3. 폼 기반 로그인 비활성화
                .formLogin(form -> form.disable())

                // 4. 모든 요청에 대해 접근 허용
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/**").permitAll() // 모든 경로 (/**) 접근 허용
                                .anyRequest().permitAll() // 혹시 모를 다른 요청도 모두 허용
                );

        return http.build();
    }
}