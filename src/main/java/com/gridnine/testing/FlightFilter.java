package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FlightFilter {

    /**
     * List of exclusion commands
     */
    private final static String[] EXCLUSIONS = {
        "DEPARTURE_BEFORE_CURRENT_TIME",
        "ARRIVAL_BEFORE_DEPARTURE",
        "INTERSECTING_SEGMENTS",
        "SAME_TIME_SEGMENTS",
        "SET_GROUND_TIME/**"
    };

    /**
     * List of filter commands
     */
    private final static String[] FILTERS = {
        "DEPARTURE_AT/dd-MM-yyyy",
        "DEPARTURE_BEFORE/dd-MM-yyyy",
        "DEPARTURE_AFTER/dd-MM-yyyy",
        "ARRIVAL_AT/dd-MM-yyyy",
        "ARRIVAL_BEFORE/dd-MM-yyyy",
        "ARRIVAL_AFTER/dd-MM-yyyy",
        "NUMBER_OF_SEGMENTS/**"
    };

    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /**
     * Method for excluding impossible flights or those with higher than needed ground time
     */
    static List<Flight> excludeFlights(List<Flight> flights,String... rules) {
        List<Flight> filteredFlights = flights;
        for (String rule : rules) {
            filteredFlights = exclude(filteredFlights, rule);
        }
        return filteredFlights;
    }

    private static List<Flight> exclude(List<Flight> flights, String rule) {
        List<Flight> filteredFlights = new ArrayList<>();
        rule = rule.toUpperCase();
        switch (rule) {
            case ("DEPARTURE_BEFORE_CURRENT_TIME") -> {
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    for (Segment flightSegment : flightSegments) {
                        if (!flightSegment.getDepartureDate().isBefore(LocalDateTime.now())) {
                            filteredFlights.add(flight);
                            break;
                        }
                    }
                }
            }
            case ("ARRIVAL_BEFORE_DEPARTURE") -> {
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    for (Segment flightSegment : flightSegments) {
                        if (!flightSegment.getArrivalDate().isBefore(flightSegment.getDepartureDate())) {
                            filteredFlights.add(flight);
                            break;
                        }
                    }
                }
            }
            case ("INTERSECTING_SEGMENTS") -> {
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    boolean hasIntersectingSegments = false;
                    for (int i = 0; i < flightSegments.size() - 1; i++) {
                        if (flightSegments.get(i).getArrivalDate().isAfter(flightSegments.get(i + 1).getDepartureDate())) {
                            hasIntersectingSegments = true;
                            break;
                        }
                    }
                    if (!hasIntersectingSegments) filteredFlights.add(flight);
                }
            }
            case ("SAME_TIME_ARRIVAL") -> {
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    boolean sameTime = false;
                    for (Segment flightSegment : flightSegments) {
                        sameTime = flightSegment.getDepartureDate().isEqual(flightSegment.getArrivalDate());
                    }
                    if (!sameTime) filteredFlights.add(flight);
                }
            }
        }

        if (rule.startsWith("SET_GROUND_TIME/")) {
            for (Flight flight : flights) {
                List<Segment> flightSegments = flight.getSegments();
                long setGroundTime = Long.parseLong(rule.substring(16));
                long groundTime = 0L;
                for (int i = 0; i < flightSegments.size() - 1; i++) {
                    groundTime = groundTime + ChronoUnit
                            .MINUTES
                            .between(flightSegments.get(i).getArrivalDate(), flightSegments.get(i + 1).getDepartureDate());
                }
                if (groundTime < setGroundTime) {
                    filteredFlights.add(flight);
                }
            }
        }

        return filteredFlights;
    }

    /**
     * Method for filtering flights based on their time or number of segments
     */
    static List<Flight> filter(List<Flight> flights, String... rules) {
        List<Flight> filteredFlights = flights;
        for (String rule : rules) {
            filteredFlights = filterFlights(filteredFlights, rule);
        }

        return filteredFlights;
    }

    private static List<Flight> filterFlights(List<Flight> flights, String rule) {
        List<Flight> filteredFlights = new ArrayList<>();
        rule = rule.toUpperCase();
        int slashIndex = rule.lastIndexOf("/");
        switch (rule.substring(0, rule.lastIndexOf("/"))) {
            case ("DEPARTURE_AT") -> {
                LocalDateTime setDepartureDate = LocalDateTime.parse(rule.substring(slashIndex + 1), dateTimeFormatter);
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    LocalDateTime departureDate = flightSegments.get(0).getDepartureDate();
                    if (departureDate.isEqual(setDepartureDate))
                        filteredFlights.add(flight);
                }
            }
            case ("DEPARTURE_BEFORE") -> {
                LocalDateTime setDepartureDate = LocalDateTime.parse(rule.substring(slashIndex + 1), dateTimeFormatter);
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    LocalDateTime departureDate = flightSegments.get(0).getDepartureDate();
                    if (departureDate.isBefore(setDepartureDate))
                        filteredFlights.add(flight);
                }
            }
            case ("DEPARTURE_AFTER") -> {
                LocalDateTime setDepartureDate = LocalDateTime.parse(rule.substring(slashIndex + 1), dateTimeFormatter);
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    LocalDateTime departureDate = flightSegments.get(0).getDepartureDate();
                    if (departureDate.isAfter(setDepartureDate))
                        filteredFlights.add(flight);
                }
            }
            case ("ARRIVAL_AT") -> {
                LocalDateTime setArrivalDate = LocalDateTime.parse(rule.substring(slashIndex + 1), dateTimeFormatter);
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    LocalDateTime arrivalDate = flightSegments.get(flightSegments.size() - 1).getArrivalDate();
                    if (arrivalDate.isEqual(setArrivalDate))
                        filteredFlights.add(flight);
                }
            }
            case ("ARRIVAL_BEFORE") -> {
                LocalDateTime setArrivalDate = LocalDateTime.parse(rule.substring(slashIndex + 1), dateTimeFormatter);
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    LocalDateTime arrivalDate = flightSegments.get(flightSegments.size() - 1).getArrivalDate();
                    if (arrivalDate.isBefore(setArrivalDate))
                        filteredFlights.add(flight);
                }
            }
            case ("ARRIVAL_AFTER") -> {
                LocalDateTime setArrivalDate = LocalDateTime.parse(rule.substring(slashIndex + 1), dateTimeFormatter);
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    LocalDateTime arrivalDate = flightSegments.get(flightSegments.size() - 1).getArrivalDate();
                    if (arrivalDate.isAfter(setArrivalDate))
                        filteredFlights.add(flight);
                }
            }
            case ("NUMBER_OF_SEGMENTS") -> {
                int numberOfSegments = Integer.parseInt(rule.substring(slashIndex + 1));
                for (Flight flight : flights) {
                    List<Segment> flightSegments = flight.getSegments();
                    if (flightSegments.size() == numberOfSegments)
                        filteredFlights.add(flight);
                }
            }
        }

        return filteredFlights;
    }

}
