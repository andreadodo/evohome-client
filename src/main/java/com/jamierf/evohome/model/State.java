package com.jamierf.evohome.model;

public enum State {
    CREATED(false),
    RUNNING(false),
    SUCCEEDED(true);

    private final boolean complete;

    private State(final boolean complete) {
        this.complete = complete;
    }

    public boolean isComplete() {
        return complete;
    }
}
