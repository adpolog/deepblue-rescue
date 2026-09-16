package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.mapper.RescueCaseMapper;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.impl.RescueCaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper;

    @InjectMocks
    private RescueCaseServiceImpl service;


@Test
void shouldFindRescueCaseByCode() {
    RescueCase rescueCase = new RescueCase();

    RescueCaseResponse response = new RescueCaseResponse(
            1L,
            "RES-999",
            LocalDate.now(),
            "Santa Marta",
            RescueStatus.ADMITTED,
            "CEN-001",
            "ANI-001"
    );

    when(
            repository.findByCaseCode("RES-001")
    ).thenReturn(
            Optional.of(rescueCase)
    );

    when(
            mapper.toResponse(rescueCase)
    ).thenReturn(response);

    RescueCaseResponse result =
            service.findByCode("RES-001");

    assertThat(result)
            .isEqualTo(response);

    verify(repository)
            .findByCaseCode("RES-001");

    verify(mapper)
            .toResponse(rescueCase);
}
}