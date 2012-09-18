package models;

import org.joda.time.ReadableDateTime;

public class Status {

    public enum State {
        NOT_STARTED, RUNNING, SUCCESS, FAILURE;

        public static State state(boolean success) {
            return success ? SUCCESS : FAILURE;
        }
    };

    private final State state;
    private final ReadableDateTime started;
    private final ReadableDateTime finished;

    public Status(State state, ReadableDateTime started, ReadableDateTime finished) {
        this.state = state;
        this.started = started;
        this.finished = finished;
    }

    public State getStatus() {
        return state;
    }

    public ReadableDateTime getStarted() {
        return started;
    }

    public ReadableDateTime getFinished() {
        return finished;
    }
}
