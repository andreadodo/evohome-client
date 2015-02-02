package com.jamierf.evohome.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Gateway {

    private final long id;
    private final String mac;
    private final String crc;

    @JsonCreator
    public Gateway(
            @JsonProperty("gatewayID") final long id,
            @JsonProperty("mac") final String mac,
            @JsonProperty("crc") final String crc) {
        this.id = id;
        this.mac = mac;
        this.crc = crc;
    }

    public long getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public String getCrc() {
        return crc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("mac", mac)
                .add("crc", crc)
                .toString();
    }
}
