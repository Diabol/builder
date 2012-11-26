package models;

import java.util.Date;

public interface StatusInterface {
    public enum State {
        NOT_STARTED, PENDING, RUNNING, SUCCESS, FAILURE;

        public static State state(boolean success) {
            return success ? SUCCESS : FAILURE;
        }
    };

    public State getState();

    public Date getStarted();

    public Date getFinished();

    public boolean isSuccess();

    public boolean isRunning();

    public boolean isPending();

    public boolean hasStarted();
}
