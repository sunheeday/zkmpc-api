package com.zkrypto.zkmpc_api.domain.enterprise.application.service;

import com.zkrypto.zkmpc_api.domain.enterprise.application.dto.EnterpriseRegisterRequest;
import com.zkrypto.zkmpc_api.domain.enterprise.application.dto.EnterpriseResponse;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @InjectMocks
    private EnterpriseService enterpriseService;

    private EnterpriseRegisterRequest registerRequest;
    private Enterprise enterprise;

    @BeforeEach
    void setUp() {
        registerRequest = new EnterpriseRegisterRequest();
        registerRequest.setEnterpriseId("testEnterpriseId");
        registerRequest.setName("Test Enterprise Name");
        enterprise = new Enterprise("testEnterpriseId", "Test Enterprise Name");
    }

    @Test
    @DisplayName("새로운 기업을 성공적으로 등록한다")
    void registerEnterprise_success() {
        // Given
        when(enterpriseRepository.existsByEnterpriseId(registerRequest.getEnterpriseId())).thenReturn(false);
        when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);

        // When
        enterpriseService.registerEnterprise(registerRequest);

        // Then
        verify(enterpriseRepository, times(1)).existsByEnterpriseId(registerRequest.getEnterpriseId());
        verify(enterpriseRepository, times(1)).save(any(Enterprise.class));
    }

    @Test
    @DisplayName("이미 존재하는 기업 ID로 등록 시 예외를 발생시킨다")
    void registerEnterprise_duplicateId_throwsException() {
        // Given
        when(enterpriseRepository.existsByEnterpriseId(registerRequest.getEnterpriseId())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enterpriseService.registerEnterprise(registerRequest);
        });

        assertThat(exception.getMessage()).contains("이미 등록된 ID입니다");
        verify(enterpriseRepository, times(1)).existsByEnterpriseId(registerRequest.getEnterpriseId());
        verify(enterpriseRepository, never()).save(any(Enterprise.class));
    }

    @Test
    @DisplayName("등록된 기업이 없을 때 빈 리스트를 반환한다")
    void getAllEnterprises_noEnterprises_returnsEmptyList() {
        // Given
        when(enterpriseRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<EnterpriseResponse> result = enterpriseService.getAllEnterprises();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(enterpriseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("등록된 기업들이 있을 때 모든 기업을 반환한다")
    void getAllEnterprises_withEnterprises_returnsAll() {
        // Given
        Enterprise enterprise1 = new Enterprise("id1", "Name1");
        Enterprise enterprise2 = new Enterprise("id2", "Name2");
        List<Enterprise> enterprises = Arrays.asList(enterprise1, enterprise2);
        when(enterpriseRepository.findAll()).thenReturn(enterprises);

        // When
        List<EnterpriseResponse> result = enterpriseService.getAllEnterprises();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEnterpriseId()).isEqualTo("id1");
        assertThat(result.get(1).getEnterpriseName()).isEqualTo("Name2");
        verify(enterpriseRepository, times(1)).findAll();
    }
}
