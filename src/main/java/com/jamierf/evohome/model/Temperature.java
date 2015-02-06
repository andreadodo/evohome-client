package com.jamierf.evohome.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Temperature {

    public static Temperature celsius(final double value) {
        return new Temperature(Units.CELSIUS, value);
    }

    public static Temperature fahrenheit(final double value) {
        return new Temperature(Units.FAHRENHEIT, value);
    }

    private final Units units;
    private final double value;

    @JsonCreator
    public Temperature(
            @JsonProperty("units") final Units units,
            @JsonProperty("indoorTemperature") final double value) {
        this.units = units;
        this.value = value;
    }

    public Units getUnits() {
        return units;
    }

    public double getValue() {
        return value;
    }

    public double toCelsius() {
        return units.toCelsius(value);
    }

    public double toFahrenheit() {
        return units.toFahrenheit(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Temperature that = (Temperature) o;
        if (Double.compare(that.value, value) != 0) return false;
        if (units != that.units) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = units != null ? units.hashCode() : 0;
        long temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("%f %s", value, units);
    }
}
