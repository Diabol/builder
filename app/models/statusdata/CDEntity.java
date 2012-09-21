package models.statusdata;

import java.util.Date;
import java.util.Locale;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import models.StatusInterface;

import org.springframework.format.datetime.DateFormatter;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

/**
 * Base class for Pipe, Phase and Task.
 * @author danielgronberg
 *
 */
@MappedSuperclass
public abstract class CDEntity extends Model implements StatusInterface {
	
	@Constraints.Required
	@Enumerated(EnumType.STRING)
	public State state;

	@Formats.DateTime(pattern = "dd/MM/yyyy")
	public Date started;

	@Formats.DateTime(pattern = "dd/MM/yyyy")
	public Date finished;

	CDEntity(State state, Date started, Date finished) {
		this.state = state;
		this.started = started;
		this.finished = finished;
	}

	@Override
	public String toString() {
		DateFormatter formatter = new DateFormatter();
		StringBuffer buf = new StringBuffer();
		buf.append("state: " + state);
		if (started != null) {
			buf.append(", started: " + formatter.print(started, Locale.ENGLISH));
		}
		if (finished != null) {
			buf.append(", finished: "
					+ formatter.print(finished, Locale.ENGLISH));
		}
		return buf.toString();
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Date getStarted() {
		return started;
	}

	@Override
	public Date getFinished() {
		return finished;
	}

	@Override
	public boolean isSuccess() {
		return state == State.SUCCESS;
	}

	@Override
	public boolean isRunning() {
		return state == State.RUNNING;
	}

	public void startNow() {
		started = new Date();
		state = State.RUNNING;
	}
	
	public void finishNow(boolean success) {
		finished = new Date();
		state = success ? State.SUCCESS: State.FAILURE;
	}
}
