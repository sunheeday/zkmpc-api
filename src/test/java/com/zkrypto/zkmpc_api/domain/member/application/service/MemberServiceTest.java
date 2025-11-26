package com.zkrypto.zkmpc_api.domain.member.application.service;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterRequest;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import com.zkrypto.zkmpc_api.domain.member.domain.service.AuthCodeManager;
import com.zkrypto.zkmpc_api.domain.member.domain.service.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // 💡 추가
import org.mockito.quality.Strictness; // 💡 추가

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 불필요한 Stubbing 오류 방지
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AuthCodeManager authCodeManager;
    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private Group group;
    private final String TEST_MEMBER_ID = "testMemberId";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";


    @BeforeEach
    void setUp() {
        group = mock(Group.class);
        when(group.getGroupId()).thenReturn("testGroupId");

        member = new Member(TEST_MEMBER_ID, TEST_EMAIL);
    }

    private void mockAuthCodeValid() {
        when(authCodeManager.get(anyString())).thenReturn(Optional.of(TEST_AUTH_CODE));
    }


    @Test
    @DisplayName("멤버 등록 성공")
    void registerMember_success() {
        // Given
        MemberRegisterRequest request = new MemberRegisterRequest(TEST_EMAIL, TEST_AUTH_CODE);

        mockAuthCodeValid();
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        memberService.verifyEmailCodeAndRegisterMember(request);

        // Then
        // 💡 memberRepository의 findByEmail 호출 검증 추가 (누락된 Service 로직 가정)
        verify(memberRepository, times(1)).findByEmail(request.getEmail());
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(authCodeManager, times(1)).remove(eq(TEST_EMAIL));
    }

    @Test
    @DisplayName("멤버 등록 실패 - 이미 존재하는 이메일")
    void registerMember_fail_emailAlreadyExists() {
        // Given
        MemberRegisterRequest request = new MemberRegisterRequest(TEST_EMAIL, TEST_AUTH_CODE);

        mockAuthCodeValid();
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.verifyEmailCodeAndRegisterMember(request);
        });
        //
        assertThat(exception.getMessage()).contains("이미 가입된 이메일 주소입니다.");
        verify(memberRepository, times(1)).findByEmail(request.getEmail());
        verify(memberRepository, never()).save(any(Member.class));
    }


    @Test
    @DisplayName("그룹 설정 성공")
    void setGroup_success() {
        // Given
        // @BeforeEach에서 그룹 설정을 제거했으므로, 이 member 객체는 아직 그룹이 없음.
        when(memberRepository.findByMemberId(eq(TEST_MEMBER_ID))).thenReturn(Optional.of(member));

        // When
        memberService.setGroup(TEST_MEMBER_ID, group);

        // Then
        // 💡 그룹 설정이 성공했는지 검증
        assertThat(member.getGroup()).isEqualTo(group);
        verify(memberRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("그룹 설정 실패 - 멤버를 찾을 수 없음")
    void setGroup_fail_memberNotFound() {
        String nonExistentId = "nonExistentMemberId";
        // Given
        when(memberRepository.findByMemberId(eq(nonExistentId))).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.setGroup(nonExistentId, group);
        });
        // 💡 Assertion 메시지를 정확하게 검증하도록 수정
        assertThat(exception.getMessage()).contains("존재하지 않는 멤버 ID입니다: " + nonExistentId);
        verify(memberRepository, never()).save(any(Member.class));
    }
}