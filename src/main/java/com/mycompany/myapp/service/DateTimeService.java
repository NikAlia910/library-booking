package com.mycompany.myapp.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Service for providing current date/time, allowing for testing with mocked dates.
 */
@Service
public class DateTimeService {

    private LocalDate mockedCurrentDate;

    /**
     * Get the current date. Returns mocked date if set, otherwise actual current date.
     *
     * @return the current date
     */
    public LocalDate getCurrentDate() {
        return mockedCurrentDate != null ? mockedCurrentDate : LocalDate.now();
    }

    /**
     * Set a mocked current date for testing purposes.
     *
     * @param mockedDate the date to use as "current" date
     */
    public void setMockedCurrentDate(LocalDate mockedDate) {
        this.mockedCurrentDate = mockedDate;
    }

    /**
     * Clear the mocked date and return to using actual current date.
     */
    public void clearMockedCurrentDate() {
        this.mockedCurrentDate = null;
    }
}
