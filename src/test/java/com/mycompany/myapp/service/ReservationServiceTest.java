package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.repository.ReservationRepository;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import com.mycompany.myapp.service.mapper.ReservationMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private DateTimeService dateTimeService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void testMaxReservationValidation_ShouldFailWhen5ActiveReservations() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setUser(userDTO);
        reservationDTO.setStartTime(Instant.now().plusSeconds(7200)); // 2 hours from now
        reservationDTO.setEndTime(Instant.now().plusSeconds(10800)); // 3 hours from now

        Instant currentTime = Instant.now();
        when(dateTimeService.getCurrentInstant()).thenReturn(currentTime);
        when(reservationRepository.countActiveReservationsByUserId(1L, currentTime)).thenReturn(5L);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reservationService.save(reservationDTO));

        assertThat(exception.getMessage()).contains("Maximum reservation limit of 5 active reservations reached");
    }
}
