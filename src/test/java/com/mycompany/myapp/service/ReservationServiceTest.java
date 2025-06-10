package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.enumeration.ResourceType;
import com.mycompany.myapp.repository.ReservationRepository;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.service.dto.ResourceDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import com.mycompany.myapp.service.mapper.ReservationMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        reservationDTO.setEndTime(Instant.now().plusSeconds(10800)); // 3 hours from now (1 hour duration)

        Instant currentTime = Instant.now();
        when(dateTimeService.getCurrentInstant()).thenReturn(currentTime);
        when(reservationRepository.countActiveReservationsByUserId(1L, currentTime)).thenReturn(5L);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reservationService.save(reservationDTO));

        assertThat(exception.getMessage()).contains("Maximum reservation limit of 5 active reservations reached");
    }

    @Test
    void testMeetingRoomTimeLimitValidation() {
        // Test that meeting room 2-hour limit is enforced
        UserDTO user = new UserDTO();
        user.setId(1L);

        ResourceDTO meetingRoom = new ResourceDTO();
        meetingRoom.setId(1L);
        meetingRoom.setResourceType(ResourceType.MEETING_ROOM);

        Instant now = Instant.now();
        Instant startTime = now.plus(2, ChronoUnit.HOURS);
        Instant endTime = now.plus(5, ChronoUnit.HOURS); // 3 hours total duration - should fail

        ReservationDTO reservation = new ReservationDTO();
        reservation.setUser(user);
        reservation.setResource(meetingRoom);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setReservationDate(startTime);
        reservation.setReservationId("TEST-123");

        // The validation will fail early due to duration constraint, so we don't need extensive mocking

        // This should throw an exception because 3 hours > 2 hours limit
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reservationService.save(reservation));

        assertTrue(exception.getMessage().contains("Maximum reservation duration is 2 hours"));
    }
}
