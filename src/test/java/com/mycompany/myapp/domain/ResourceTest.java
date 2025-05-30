package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.ReservationTestSamples.*;
import static com.mycompany.myapp.domain.ResourceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ResourceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Resource.class);
        Resource resource1 = getResourceSample1();
        Resource resource2 = new Resource();
        assertThat(resource1).isNotEqualTo(resource2);

        resource2.setId(resource1.getId());
        assertThat(resource1).isEqualTo(resource2);

        resource2 = getResourceSample2();
        assertThat(resource1).isNotEqualTo(resource2);
    }

    @Test
    void reservationTest() {
        Resource resource = getResourceRandomSampleGenerator();
        Reservation reservationBack = getReservationRandomSampleGenerator();

        resource.addReservation(reservationBack);
        assertThat(resource.getReservations()).containsOnly(reservationBack);
        assertThat(reservationBack.getResource()).isEqualTo(resource);

        resource.removeReservation(reservationBack);
        assertThat(resource.getReservations()).doesNotContain(reservationBack);
        assertThat(reservationBack.getResource()).isNull();

        resource.reservations(new HashSet<>(Set.of(reservationBack)));
        assertThat(resource.getReservations()).containsOnly(reservationBack);
        assertThat(reservationBack.getResource()).isEqualTo(resource);

        resource.setReservations(new HashSet<>());
        assertThat(resource.getReservations()).doesNotContain(reservationBack);
        assertThat(reservationBack.getResource()).isNull();
    }
}
