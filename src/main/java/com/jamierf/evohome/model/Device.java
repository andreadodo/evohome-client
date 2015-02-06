package com.jamierf.evohome.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    private final long id;
    private final String name;
    private final boolean alive;
    private final String version;
    private final Temperature temperature;

    @JsonCreator
    public Device(
            @JsonProperty("deviceID") final long id,
            @JsonProperty("name") final String name,
            @JsonProperty("isAlive") final boolean alive,
            @JsonProperty("thermostatVersion") final String version,
            @JsonProperty("thermostat") final Temperature temperature) {
        this.id = id;
        this.name = name;
        this.alive = alive;
        this.version = version;
        this.temperature = temperature;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return alive;
    }

    public String getVersion() {
        return version;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Device device = (Device) o;

        if (id != device.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("alive", alive)
                .add("version", version)
                .add("temperature", temperature)
                .toString();
    }
}
