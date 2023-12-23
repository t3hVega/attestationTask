package com.gridnine.testing;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Flight> standardFlightList = FlightBuilder.createFlights();

        System.out.println("Setting up flightlist for cumulative exclusion");
        standardFlightList.forEach(System.out::println);
        System.out.println("Excluding flights at departing before current time/arriving before departing/" +
                "staying grounded for more than two hours...");
        List<Flight> cumulativeExclusionList = FlightFilter.excludeFlights(standardFlightList,
                "DEPARTURE_BEFORE_CURRENT_TIME",
                "ARRIVAL_BEFORE_DEPARTURE",
                "SET_GROUND_TIME/120");
        cumulativeExclusionList.forEach(System.out::println);
        System.out.println();

        System.out.println("Setting up additional flightlist for cumulative exclusion");
        standardFlightList = FlightBuilder.createFlightsForAdditionalExclusions();
        standardFlightList.forEach(System.out::println);
        System.out.println("Excluding all flights with intersecting segments/segments departing and arriving at the same time...");
        cumulativeExclusionList = FlightFilter.excludeFlights(standardFlightList,
                "INTERSECTING_SEGMENTS",
                "SAME_TIME_ARRIVAL");
        cumulativeExclusionList.forEach(System.out::println);
        System.out.println();

        System.out.println("Setting up flightlist for selectional and cumulative filtering");
        standardFlightList = FlightBuilder.createFlightsForFiltering();
        standardFlightList.forEach(System.out::println);

        System.out.println("Selecting flights arriving at 23-12-2023 6:00...");
        List<Flight> selectionalFilterList = FlightFilter.filter(standardFlightList, "ARRIVAL_AT/23-12-2023 06:00");
        selectionalFilterList.forEach(System.out::println);
        System.out.println("Selecting flights departing at 24-12-2023 18:00...");
        selectionalFilterList = FlightFilter.filter(standardFlightList, "DEPARTURE_AT/24-12-2023 18:00");
        selectionalFilterList.forEach(System.out::println);
        System.out.println("Selecting flights consisting of two segments...");
        selectionalFilterList = FlightFilter.filter(standardFlightList, "NUMBER_OF_SEGMENTS/2");
        selectionalFilterList.forEach(System.out::println);
        System.out.println();

        System.out.println("Filtering flights arriving before 24-12-2023 6:00," +
                " departing after 23-12-2023 1:00," +
                " departing before 23-12-2023 20:00," +
                " arriving after 23-12-2023 07:00...");
        List<Flight> cumulativeFilterList = FlightFilter.filter(standardFlightList,
                "ARRIVAL_BEFORE/24-12-2023 06:00",
                "DEPARTURE_AFTER/23-12-2023 01:00",
                "DEPARTURE_BEFORE/23-12-2023 20:00",
                "ARRIVAL_AFTER/23-12-2023 07:00");
        cumulativeFilterList.forEach(System.out::println);
    }
}