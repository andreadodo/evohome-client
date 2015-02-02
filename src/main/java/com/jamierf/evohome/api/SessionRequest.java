package com.jamierf.evohome.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class SessionRequest {

    @JsonProperty("Username")
    private final String username;

    @JsonProperty("Password")
    private final String password;

    @JsonProperty("ApplicationId")
    private final String applicationId;

    public SessionRequest(final String username, final String password, final String applicationId) {
        this.username = username;
        this.password = password;
        this.applicationId = applicationId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("password", password)
                .add("applicationId", applicationId)
                .toString();
    }
}
