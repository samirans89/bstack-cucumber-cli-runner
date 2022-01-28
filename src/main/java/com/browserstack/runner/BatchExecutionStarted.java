package com.browserstack.runner;

import io.cucumber.plugin.event.Event;

import java.time.Instant;
import java.util.Objects;

public final class BatchExecutionStarted implements Event {

    private final Instant instant;
    private final int batch;

    BatchExecutionStarted(Instant timeInstant, int batch) {
        this.instant = Objects.requireNonNull(timeInstant);
        this.batch = batch;
    }

    @Override
    public Instant getInstant() {
        return instant;
    }

    public int getBatch() {
        return batch;
    }
}
