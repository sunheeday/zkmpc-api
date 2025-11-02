package com.zkrypto.zkmpc_api.domain.member.application.service;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterRequest;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.infrastructure.persistence.JpaMemberRepository;
import com.zkrypto.zkmpc_api.domain.member.domain.service.AuthCodeManager;
import com.zkrypto.zkmpc_api.domain.member.domain.service.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private JpaMemberRepository jpaMemberRepository;
    @Mock
    private AuthCodeManager authCodeManager;
    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private Group group;

    @BeforeEach
    void setUp() {
        group = mock(Group.class);
        when(group.getGroupId()).thenReturn("testGroupId");
        member = new Member("testMemberId", "test@example.com", "0x123abc", null);
    }

    @Test
    @DisplayName("멤버 등록 성공")
    void registerMember_success() {
        // Given
        MemberRegisterRequest request = new MemberRegisterRequest("test@example.com", "0x123abc");
        when(jpaMemberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jpaMemberRepository.findByAddress(anyString())).thenReturn(Optional.empty());
        when(jpaMemberRepository.save(any(Member.class))).thenReturn(member);
        doNothing().when(emailSender).sendAuthCode(anyString(), anyString());
        doNothing().when(authCodeManager).saveAuthCode(anyString(), anyString());

        // When
        memberService.registerMember(request);

        // Then
        verify(jpaMemberRepository, times(1)).findByEmail(request.getEmail());
        verify(jpaMemberRepository, times(1)).findByAddress(request.getAddress());
        verify(jpaMemberRepository, times(1)).save(any(Member.class));
        verify(emailSender, times(1)).sendAuthCode(eq(request.getEmail()), anyString());
        verify(authCodeManager, times(1)).saveAuthCode(eq(request.getEmail()), anyString());
    }

    @Test
    @DisplayName("멤버 등록 실패 - 이미 존재하는 이메일")
    void registerMember_fail_emailAlreadyExists() {
        // Given
        MemberRegisterRequest request = new MemberRegisterRequest("test@example.com", "0x123abc");
        when(jpaMemberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.registerMember(request);
        });
        assertThat(exception.getMessage()).contains("이미 존재하는 이메일입니다.");
        verify(jpaMemberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("멤버 등록 실패 - 이미 존재하는 주소")
    void registerMember_fail_addressAlreadyExists() {
        // Given
        MemberRegisterRequest request = new MemberRegisterRequest("test@example.com", "0x123abc");
        when(jpaMemberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jpaMemberRepository.findByAddress(anyString())).thenReturn(Optional.of(member));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.registerMember(request);
        });
        assertThat(exception.getMessage()).contains("이미 존재하는 지갑 주소입니다.");
        verify(jpaMemberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("그룹 설정 성공")
    void setGroup_success() {
        // Given
        when(jpaMemberRepository.findByMemberId(anyString())).thenReturn(Optional.of(member));
        when(jpaMemberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.setGroup("testMemberId", group);

        // Then
        assertThat(member.getGroup()).isEqualTo(group);
        verify(jpaMemberRepository, times(1)).findByMemberId("testMemberId");
        verify(jpaMemberRepository, times(1)).save(member);
    }

    @Test
    @DisplayName("그룹 설정 실패 - 멤버를 찾을 수 없음")
    void setGroup_fail_memberNotFound() {
        // Given
        when(jpaMemberRepository.findByMemberId(anyString())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.setGroup("nonExistentMemberId", group);
        });
        assertThat(exception.getMessage()).contains("존재하지 않는 멤버 ID입니다.");
        verify(jpaMemberRepository, never()).save(any(Member.class));
    }
}
