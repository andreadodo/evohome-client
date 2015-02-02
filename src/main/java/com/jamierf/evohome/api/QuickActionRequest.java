package com.jamierf.evohome.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.jamierf.evohome.model.QuickAction;

import java.util.Date;

public class QuickActionRequest {

    @JsonProperty("QuickAction")
    private final QuickAction action;

    @JsonProperty("QuickActionNextTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private final Optional<Date> until;

    public QuickActionRequest(final QuickAction action, final Optional<Date> until) {
        this.action = action;
        this.until = until;
    }

    public QuickAction getAction() {
        return action;
    }

    public Optional<Date> getUntil() {
        return until;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("action", action)
                .add("until", until)
                .toString();
    }
}
