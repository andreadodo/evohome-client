package com.jamierf.evohome.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Thermostat {

    private final Units units;
    private final float temperature;

    @JsonCreator
    public Thermostat(
            @JsonProperty("units") final Units units,
            @JsonProperty("indoorTemperature") final float temperature) {
        this.units = units;
        this.temperature = temperature;
    }

    public Units getUnits() {
        return units;
    }

    public float getTemperature() {
        return temperature;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Thermostat that = (Thermostat) o;

        if (Float.compare(that.temperature, temperature) != 0) return false;
        if (units != that.units) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = units != null ? units.hashCode() : 0;
        result = 31 * result + (temperature != +0.0f ? Float.floatToIntBits(temperature) : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("units", units)
                .add("temperature", temperature)
                .toString();
    }
}
