package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.ReservationTestSamples.*;
import static com.mycompany.myapp.domain.ResourceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reservation.class);
        Reservation reservation1 = getReservationSample1();
        Reservation reservation2 = new Reservation();
        assertThat(reservation1).isNotEqualTo(reservation2);

        reservation2.setId(reservation1.getId());
        assertThat(reservation1).isEqualTo(reservation2);

        reservation2 = getReservationSample2();
        assertThat(reservation1).isNotEqualTo(reservation2);
    }

    @Test
    void resourceTest() {
        Reservation reservation = getReservationRandomSampleGenerator();
        Resource resourceBack = getResourceRandomSampleGenerator();

        reservation.setResource(resourceBack);
        assertThat(reservation.getResource()).isEqualTo(resourceBack);

        reservation.resource(null);
        assertThat(reservation.getResource()).isNull();
    }
}
