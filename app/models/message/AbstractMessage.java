package models.message;
import java.util.Date;

import models.StatusInterface;

import org.joda.time.ReadableDateTime;

public abstract class AbstractMessage implements StatusInterface {
    
    private final State state;
    private final ReadableDateTime started;
    private final ReadableDateTime finished;

    public AbstractMessage(State state, ReadableDateTime started, ReadableDateTime finished) {
        this.state = state;
        this.started = started;
        this.finished = finished;
    }

    public State getStatus() {
        return state;
    }

    @Override
	public Date getStarted() {
        return started.toDateTime().toDate();
    }

    @Override
	public Date getFinished() {
        return finished.toDateTime().toDate();
    }

    @Override
	public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    @Override
	public boolean isRunning() {
        return state == State.RUNNING;
    }

	@Override
	public State getState() {
		return state;
	}
}
