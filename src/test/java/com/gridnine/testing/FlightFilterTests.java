package com.gridnine.testing;


import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static com.gridnine.testing.FlightBuilder.createFlight;
import static org.junit.Assert.assertEquals;


public class FlightFilterTests {

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final static LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MINUTES);
    private final static LocalDateTime dec23 = LocalDateTime.parse("23-12-2023 21:00", dateTimeFormatter);
    private final static LocalDateTime dec22 = dec23.minusDays(1);
    private final static LocalDateTime dec24 = dec23.plusDays(1);

    @Test
    public void shouldCorrectlyExcludeFlightsDepartingBeforeCurrentTime() {
        List<Flight> flights = FlightBuilder.createFlights();

        List<Flight> expected = Arrays.asList(
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7))
        );

        List<Flight> actual = FlightFilter.excludeFlights(flights, "DEPARTURE_BEFORE_CURRENT_TIME");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyExcludeFlightsArrivingBeforeDeparting() {
        List<Flight> flights = FlightBuilder.createFlights();

        List<Flight> expected = Arrays.asList(
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7))
        );

        List<Flight> actual = FlightFilter.excludeFlights(flights, "ARRIVAL_BEFORE_DEPARTURE");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyExcludeFlightsWithMoreThanTwoHoursOfGroundTime() {
        List<Flight> flights = FlightBuilder.createFlights();

        List<Flight> expected = Arrays.asList(
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6))
        );

        List<Flight> actual = FlightFilter.excludeFlights(flights, "SET_GROUND_TIME/120");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyExcludeFlightsWithIntersectingSegments() {
        List<Flight> flights = FlightBuilder.createFlightsForAdditionalExclusions();

        List<Flight> expected = Arrays.asList(
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                createFlight(threeDaysFromNow, threeDaysFromNow),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(6))
        );

        List<Flight> actual = FlightFilter.excludeFlights(flights, "INTERSECTING_SEGMENTS");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyExcludeFlightsWithSameTimeSegments() {
        List<Flight> flights = FlightBuilder.createFlightsForAdditionalExclusions();

        List<Flight> expected = Arrays.asList(
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(1), threeDaysFromNow.plusHours(3)),
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5),
                threeDaysFromNow.plusHours(4), threeDaysFromNow.plusHours(6))
        );

        List<Flight> actual = FlightFilter.excludeFlights(flights, "SAME_TIME_ARRIVAL");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyFilterFlightsArrivingBeforeSetDateTime() {
        List<Flight> flights = FlightBuilder.createFlightsForFiltering();

        List<Flight> expected = Arrays.asList(
                createFlight(dec22, dec22.plusHours(1),
                        dec22.plusHours(3), dec22.plusHours(5),
                        dec22.plusHours(7), dec22.plusHours(9)),
                createFlight(dec22.plusHours(7), dec22.plusHours(9)),
                createFlight(dec23.minusHours(21), dec23.minusHours(18),
                        dec23.minusHours(15), dec23.minusHours(12)),
                createFlight(dec23.minusHours(7), dec23.minusHours(5)),
                createFlight(dec23, dec23.plusHours(4))
        );

        List<Flight> actual = FlightFilter.filter(flights, "ARRIVAL_BEFORE/24-12-2023 06:00");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyFilterFlightsArrivingAfterSetDateTime() {
        List<Flight> flights = FlightBuilder.createFlightsForFiltering();

        List<Flight> expected = Arrays.asList(
                createFlight(dec23.minusHours(21), dec23.minusHours(18),
                        dec23.minusHours(15), dec23.minusHours(12)),
                createFlight(dec23.minusHours(7), dec23.minusHours(5)),
                createFlight(dec23, dec23.plusHours(4)),
                createFlight(dec24.minusHours(3), dec24.minusHours(1),
                        dec24, dec24.plusHours(2))
        );

        List<Flight> actual = FlightFilter.filter(flights, "ARRIVAL_AFTER/23-12-2023 07:00");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyFilterFlightsDepartingBeforeSetDateTime() {
        List<Flight> flights = FlightBuilder.createFlightsForFiltering();

        List<Flight> expected = Arrays.asList(
                createFlight(dec22, dec22.plusHours(1),
                        dec22.plusHours(3), dec22.plusHours(5),
                        dec22.plusHours(7), dec22.plusHours(9)),
                createFlight(dec22.plusHours(7), dec22.plusHours(9)),
                createFlight(dec23.minusHours(21), dec23.minusHours(18),
                        dec23.minusHours(15), dec23.minusHours(12)),
                createFlight(dec23.minusHours(7), dec23.minusHours(5))
        );

        List<Flight> actual = FlightFilter.filter(flights, "DEPARTURE_BEFORE/23-12-2023 20:00");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyFilterFlightsDepartingAfterSetDateTime() {
        List<Flight> flights = FlightBuilder.createFlightsForFiltering();

        List<Flight> expected = Arrays.asList(
                createFlight(dec22.plusHours(7), dec22.plusHours(9)),
                createFlight(dec23.minusHours(7), dec23.minusHours(5)),
                createFlight(dec23, dec23.plusHours(4)),
                createFlight(dec24.minusHours(3), dec24.minusHours(1),
                        dec24, dec24.plusHours(2))
        );

        List<Flight> actual = FlightFilter.filter(flights, "DEPARTURE_AFTER/23-12-2023 01:00");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyReturnNoFlightsDueToConflictingFilters() {
        List<Flight> flights = FlightBuilder.createFlightsForFiltering();

        List<Flight> expected = List.of();

        List<Flight> actual = FlightFilter.filter(flights,
                "ARRIVAL_BEFORE/22-12-2023 21:00",
                "DEPARTURE_AFTER/23-12-2023 01:00");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyExcludeDespiteLowerCaseCommand() {
        List<Flight> flights = FlightBuilder.createFlights();

        List<Flight> expected = Arrays.asList(
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7))
        );

        List<Flight> actual = FlightFilter.excludeFlights(flights, "arrival_before_departure");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyFilterDespiteLowerCaseCommand() {
        List<Flight> flights = FlightBuilder.createFlightsForFiltering();

        List<Flight> expected = Arrays.asList(
                createFlight(dec23.minusHours(21), dec23.minusHours(18),
                        dec23.minusHours(15), dec23.minusHours(12)),
                createFlight(dec23.minusHours(7), dec23.minusHours(5)),
                createFlight(dec23, dec23.plusHours(4)),
                createFlight(dec24.minusHours(3), dec24.minusHours(1),
                        dec24, dec24.plusHours(2))
        );

        List<Flight> actual = FlightFilter.filter(flights, "arrival_after/23-12-2023 07:00");

        assertEquals(expected, actual);
    }

}
