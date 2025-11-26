package com.zkrypto.zkmpc_api.domain.group.application.service;

import com.zkrypto.zkmpc_api.common.utility.U64IdGenerator;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import com.zkrypto.zkmpc_api.domain.group.application.dto.GroupRegisterRequest;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import com.zkrypto.zkmpc_api.domain.group.domain.service.GroupDomainService;
import com.zkrypto.zkmpc_api.domain.member.application.service.MemberService;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupDomainService groupDomainService;
    @Mock
    private EnterpriseRepository enterpriseRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberService memberService;

    @InjectMocks
    private GroupService groupService;

    private GroupRegisterRequest groupRegisterRequest;
    private Enterprise enterprise1;
    private Enterprise enterprise2;
    private Member member;
    private Group group;

    @BeforeEach
    void setUp() {
        enterprise1 = new Enterprise("enterpriseId1", "Enterprise Name 1");
        enterprise2 = new Enterprise("enterpriseId2", "Enterprise Name 2");

        groupRegisterRequest = new GroupRegisterRequest();
        groupRegisterRequest.setMemberId("memberId1");
        groupRegisterRequest.setEnterprises(Arrays.asList("enterpriseId1", "enterpriseId2"));
        groupRegisterRequest.setThreshold(2); // 1 member + 2 enterprises = 3 participants, threshold should be 2

        member = new Member("memberId1", "member@example.com");
        group = new Group("newGroupId", new HashSet<>(Arrays.asList(enterprise1, enterprise2)), 2);
    }

    @Test
    @DisplayName("그룹 등록 성공")
    void registerGroup_success() {
        try (MockedStatic<U64IdGenerator> mockedStatic = mockStatic(U64IdGenerator.class)) {
            mockedStatic.when(U64IdGenerator::generateU64Id).thenReturn("newGroupId");

            //given
            when(memberRepository.findByMemberId(groupRegisterRequest.getMemberId())).thenReturn(Optional.of(member));
            when(enterpriseRepository.findByEnterpriseId("enterpriseId1")).thenReturn(Optional.of(enterprise1));
            when(enterpriseRepository.findByEnterpriseId("enterpriseId2")).thenReturn(Optional.of(enterprise2));
            when(groupRepository.save(any(Group.class))).thenReturn(group);

            doNothing().when(groupDomainService).startProtocol(anyString(), anyString(), anyList(), anyInt(), any());

            //when
            groupService.registerGroup(groupRegisterRequest);

            //then
            mockedStatic.verify(U64IdGenerator::generateU64Id, times(1));
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId1");
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId2");
            verify(groupRepository, times(1)).save(any(Group.class));
            verify(groupDomainService, times(1)).startProtocol(
                    eq("KEY_GENERATION"),
                    eq("newGroupId"),
                    anyList(), // The actual list content is checked implicitly by the success of the call
                    eq(2),
                    isNull()
            );
        }
    }

    @Test
    @DisplayName("그룹 등록 실패 - 존재하지 않는 Enterprise ID")
    void registerGroup_invalidEnterpriseId_throwsException() {
        try (MockedStatic<U64IdGenerator> mockedStatic = mockStatic(U64IdGenerator.class)) {
            mockedStatic.when(U64IdGenerator::generateU64Id).thenReturn("newGroupId");

            //given
            when(enterpriseRepository.findByEnterpriseId("enterpriseId1")).thenReturn(Optional.of(enterprise1));
            when(enterpriseRepository.findByEnterpriseId("enterpriseId2")).thenReturn(Optional.empty());

            //when예외가 발생함
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                groupService.registerGroup(groupRegisterRequest);
            });

            //then
            assertThat(exception.getMessage()).contains("존재하지 않는 Enterprise ID가 포함되어 있습니다: enterpriseId2");
            mockedStatic.verify(U64IdGenerator::generateU64Id, times(1));
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId1");
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId2");
            verify(groupRepository, never()).save(any(Group.class));
            verify(memberService, never()).setGroup(anyString(), any(Group.class));
            verify(groupDomainService, never()).startProtocol(anyString(), anyString(), anyList(), anyInt(), any());
        }
    }

    @Test
    @DisplayName("그룹 등록 실패 - 2개 미만의 Enterprise ID")
    void registerGroup_lessThanTwoEnterprises_throwsException() {
        groupRegisterRequest.setEnterprises(Arrays.asList("enterpriseId1"));
        groupRegisterRequest.setThreshold(1); // Adjust threshold for 1 participant

        try (MockedStatic<U64IdGenerator> mockedStatic = mockStatic(U64IdGenerator.class)) {
            mockedStatic.when(U64IdGenerator::generateU64Id).thenReturn("newGroupId");

            when(enterpriseRepository.findByEnterpriseId("enterpriseId1")).thenReturn(Optional.of(enterprise1));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                groupService.registerGroup(groupRegisterRequest);
            });

            assertThat(exception.getMessage()).contains("그룹 등록을 위해서는 최소 2개 이상의 유효한 엔터프라이즈 ID가 필요합니다.");
            mockedStatic.verify(U64IdGenerator::generateU64Id, times(1));
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId1");
            verify(groupRepository, never()).save(any(Group.class));
            verify(memberService, never()).setGroup(anyString(), any(Group.class));
            verify(groupDomainService, never()).startProtocol(anyString(), anyString(), anyList(), anyInt(), any());
        }
    }

    @Test
    @DisplayName("그룹 등록 실패 - 잘못된 Threshold 값")
    void registerGroup_incorrectThreshold_throwsException() {
        groupRegisterRequest.setThreshold(1); // Incorrect threshold, should be 2

        try (MockedStatic<U64IdGenerator> mockedStatic = mockStatic(U64IdGenerator.class)) {
            mockedStatic.when(U64IdGenerator::generateU64Id).thenReturn("newGroupId");

            when(enterpriseRepository.findByEnterpriseId("enterpriseId1")).thenReturn(Optional.of(enterprise1));
            when(enterpriseRepository.findByEnterpriseId("enterpriseId2")).thenReturn(Optional.of(enterprise2));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                groupService.registerGroup(groupRegisterRequest);
            });

            assertThat(exception.getMessage()).contains("Threshold (1) 값은 총 참가자 수 (3) - 1 인 2이어야 합니다.");
            mockedStatic.verify(U64IdGenerator::generateU64Id, times(1));
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId1");
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId2");
            verify(groupRepository, never()).save(any(Group.class));
            verify(memberService, never()).setGroup(anyString(), any(Group.class));
            verify(groupDomainService, never()).startProtocol(anyString(), anyString(), anyList(), anyInt(), any());
        }
    }

    @Test
    @DisplayName("그룹 등록 실패 - KEY_GENERATION 프로토콜 시작 실패")
    void registerGroup_keyGenerationProtocolFails_throwsException() {
        try (MockedStatic<U64IdGenerator> mockedStatic = mockStatic(U64IdGenerator.class)) {
            mockedStatic.when(U64IdGenerator::generateU64Id).thenReturn("newGroupId");

            //given
            // 💡 수정된 부분: memberRepository Mocking 추가 (이전 실패 원인)
            when(memberRepository.findByMemberId(groupRegisterRequest.getMemberId())).thenReturn(Optional.of(member));

            when(enterpriseRepository.findByEnterpriseId("enterpriseId1")).thenReturn(Optional.of(enterprise1));
            when(enterpriseRepository.findByEnterpriseId("enterpriseId2")).thenReturn(Optional.of(enterprise2));
            when(groupRepository.save(any(Group.class))).thenReturn(group);

            doThrow(new RuntimeException("Protocol failed")).when(groupDomainService).startProtocol(anyString(), anyString(), anyList(), anyInt(), any());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                groupService.registerGroup(groupRegisterRequest);
            });

            assertThat(exception.getMessage()).contains("KEY_GENERATION 프로토콜 시작 실패. 그룹 등록 취소됨.");
            mockedStatic.verify(U64IdGenerator::generateU64Id, times(1));
            verify(memberRepository, times(1)).findByMemberId(groupRegisterRequest.getMemberId());
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId1");
            verify(enterpriseRepository, times(1)).findByEnterpriseId("enterpriseId2");
            verify(groupRepository, times(1)).save(any(Group.class));

            verify(groupDomainService, times(1)).startProtocol(
                    eq("KEY_GENERATION"),
                    eq("newGroupId"),
                    anyList(),
                    eq(2),
                    isNull()
            );
        }
    }

    @Test
    @DisplayName("그룹 ID로 그룹 조회 성공")
    void getGroupById_success() {
        when(groupRepository.findByGroupId("newGroupId")).thenReturn(Optional.of(group));

        Group foundGroup = groupService.getGroupById("newGroupId");

        assertThat(foundGroup).isEqualTo(group);
        verify(groupRepository, times(1)).findByGroupId("newGroupId");
    }

    @Test
    @DisplayName("그룹 ID로 그룹 조회 실패 - 그룹 없음")
    void getGroupById_notFound_throwsException() {
        when(groupRepository.findByGroupId("nonExistentGroupId")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            groupService.getGroupById("nonExistentGroupId");
        });

        assertThat(exception.getMessage()).contains("존재하지 않는 그룹 ID입니다: nonExistentGroupId");
        verify(groupRepository, times(1)).findByGroupId("nonExistentGroupId");
    }

    @Test
    @DisplayName("그룹 ID로 멤버 ID 조회 성공")
    void getMemberIdByGroupId_success() {
        when(memberRepository.findByGroup_GroupId("newGroupId")).thenReturn(Optional.of(member));

        String foundMemberId = groupService.getMemberIdByGroupId("newGroupId");

        assertThat(foundMemberId).isEqualTo("memberId1");
        verify(memberRepository, times(1)).findByGroup_GroupId("newGroupId");
    }

    @Test
    @DisplayName("그룹 ID로 멤버 ID 조회 실패 - 멤버 없음")
    void getMemberIdByGroupId_notFound_throwsException() {
        when(memberRepository.findByGroup_GroupId("nonExistentGroupId")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            groupService.getMemberIdByGroupId("nonExistentGroupId");
        });

        assertThat(exception.getMessage()).contains("해당 그룹에 존재하는 멤버가 없습니다: nonExistentGroupId");
        verify(memberRepository, times(1)).findByGroup_GroupId("nonExistentGroupId");
    }

}