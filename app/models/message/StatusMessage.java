package models.message;

import models.config.PhaseConfig;
import orchestration.PipeVersion;

import org.joda.time.ReadableDateTime;

public abstract class StatusMessage {

    public enum Status {
        NOT_STARTED, RUNNING, SUCCESS, FAILURE;

        public static Status status(boolean success) {
            return success ? SUCCESS: FAILURE;
        }
    };

    private final Status status;
    private final PipeVersion<?> version;
    private final PhaseConfig phase;
    private final ReadableDateTime started;
    private final ReadableDateTime finished;

    protected StatusMessage(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started,
            ReadableDateTime finished) {
        this.status = status;
        this.version = version;
        this.phase = phase;
        this.started = started;
        this.finished = finished;
    }

    public Status getStatus() {
        return status;
    }

    public String getVersion() {
        return version.getVersion().toString();
    }

    public String getPhaseName() {
        return phase.getName();
    }

    public ReadableDateTime getStarted() {
        return started;
    }

    public ReadableDateTime getFinished() {
        return finished;
    }
}
