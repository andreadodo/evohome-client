package com.jamierf.evohome.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.jamierf.evohome.model.TemperatureStatus;

import java.util.Date;

public class SetTemperatureRequest {

    @JsonProperty("Value")
    private final float temperature;

    @JsonProperty("NextTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private final Optional<Date> until;

    @JsonProperty("Status")
    private final TemperatureStatus status;

    public SetTemperatureRequest(final float temperature, final Optional<Date> until) {
        this.temperature = temperature;
        this.until = until;

        status = until.isPresent() ? TemperatureStatus.Temporary : TemperatureStatus.Hold;
    }

    public float getTemperature() {
        return temperature;
    }

    public Optional<Date> getUntil() {
        return until;
    }

    public TemperatureStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("temperature", temperature)
                .add("until", until)
                .add("status", status)
                .toString();
    }
}
