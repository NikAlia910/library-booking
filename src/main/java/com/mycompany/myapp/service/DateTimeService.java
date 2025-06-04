package com.mycompany.myapp.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

/**
 * Service for providing current date/time, allowing for testing with mocked dates.
 */
@Service
public class DateTimeService {

    private LocalDate mockedCurrentDate;
    private LocalDateTime mockedCurrentDateTime;

    /**
     * Get the current date. Returns mocked date if set, otherwise actual current date.
     *
     * @return the current date
     */
    public LocalDate getCurrentDate() {
        if (mockedCurrentDateTime != null) {
            return mockedCurrentDateTime.toLocalDate();
        }
        return mockedCurrentDate != null ? mockedCurrentDate : LocalDate.now();
    }

    /**
     * Get the current instant. Returns mocked instant if set, otherwise actual current instant.
     *
     * @return the current instant
     */
    public Instant getCurrentInstant() {
        if (mockedCurrentDateTime != null) {
            return mockedCurrentDateTime.toInstant(ZoneOffset.UTC);
        }
        return mockedCurrentDate != null ? mockedCurrentDate.atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.now();
    }

    /**
     * Set a mocked current date for testing purposes.
     *
     * @param mockedDate the date to use as "current" date
     */
    public void setMockedCurrentDate(LocalDate mockedDate) {
        this.mockedCurrentDate = mockedDate;
        this.mockedCurrentDateTime = null; // Clear datetime if date is set
    }

    /**
     * Set a mocked current date and time for testing purposes.
     *
     * @param mockedDateTime the date and time to use as "current" date/time
     */
    public void setMockedCurrentDateTime(LocalDateTime mockedDateTime) {
        this.mockedCurrentDateTime = mockedDateTime;
        this.mockedCurrentDate = null; // Clear date if datetime is set
    }

    /**
     * Clear the mocked date and return to using actual current date.
     */
    public void clearMockedCurrentDate() {
        this.mockedCurrentDate = null;
        this.mockedCurrentDateTime = null;
    }
}
