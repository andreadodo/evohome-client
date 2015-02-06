package com.jamierf.evohome.model;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class UnitsTest {
    private static final double PRECISION = 0.001;

    @Test
    public void testCelsiusToCelsiusDoesntChange() {
        assertThat(Units.CELSIUS.toCelsius(37.5), closeTo(37.5, PRECISION));
    }

    @Test
    public void testZeroCelsiusToFahrenheit() {
        assertThat(Units.CELSIUS.toFahrenheit(0), closeTo(32, PRECISION));
    }

    @Test
    public void testCelsiusToFahrenheit() {
        assertThat(Units.CELSIUS.toFahrenheit(-100), closeTo(-148, PRECISION));
        assertThat(Units.CELSIUS.toFahrenheit(100), closeTo(212, PRECISION));
    }

    @Test
    public void testFahrenheitToFahrenheitDoesntChange() {
        assertThat(Units.FAHRENHEIT.toFahrenheit(37.5), closeTo(37.5, PRECISION));
    }

    @Test
    public void testZeroFahrenheitToCelsius() {
        assertThat(Units.FAHRENHEIT.toCelsius(0), closeTo(-17.778, PRECISION));
    }

    @Test
    public void testFahrenheitToCelsius() {
        assertThat(Units.FAHRENHEIT.toCelsius(-100), closeTo(-73.333, PRECISION));
        assertThat(Units.FAHRENHEIT.toCelsius(100), closeTo(37.778, PRECISION));
    }
}
