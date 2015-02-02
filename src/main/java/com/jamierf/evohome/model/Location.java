package com.jamierf.evohome.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

    private final long id;
    private final String name;
    private final Collection<Device> devices;

    @JsonCreator
    public Location(
            @JsonProperty("locationID") final long id,
            @JsonProperty("name") final String name,
            @JsonProperty("devices") final Collection<Device> devices) {
        this.id = id;
        this.name = name;
        this.devices = devices;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Collection<Device> getDevices() {
        return devices;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Location location = (Location) o;

        if (id != location.id) return false;

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
                .add("devices", devices)
                .toString();
    }
}
