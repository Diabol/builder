package models.statusdata;

import java.util.Date;
import java.util.Locale;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

import models.StatusInterface;

import org.codehaus.jackson.node.ObjectNode;
import org.springframework.format.datetime.DateFormatter;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Json;
import controllers.PipeListHelper;

/**
 * Base class for Pipe, Phase and Task.
 * 
 * @author danielgronberg
 * 
 */
@MappedSuperclass
public abstract class CDEntity extends Model implements StatusInterface {

    @Constraints.Required
    @Enumerated(EnumType.STRING)
    public StatusInterface.State state;

    @Formats.DateTime(pattern = "yyyy/MM/dd HH:mm:ss")
    public Date started;

    @Formats.DateTime(pattern = "yyyy/MM/dd HH:mm:ss")
    public Date finished;

    CDEntity(State state, Date started, Date finished) {
        this.state = state;
        this.started = started;
        this.finished = finished;
    }

    @Override
    public String toString() {
        DateFormatter formatter = new DateFormatter("yyyy/MM/dd HH:mm:ss");
        StringBuffer buf = new StringBuffer();
        buf.append("state: " + state);
        if (started != null) {
            buf.append(", started: " + formatter.print(started, Locale.ENGLISH));
        }
        if (finished != null) {
            buf.append(", finished: " + formatter.print(finished, Locale.ENGLISH));
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
    public boolean isPending() {
        return state == State.PENDING;
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
        state = success ? State.SUCCESS : State.FAILURE;
    }

    public ObjectNode toObjectNode() {
        ObjectNode result = Json.newObject();
        result.put("state", state.toString());
        if (started != null) {
            result.put("started", started.getTime());
            result.put("startedAsString", PipeListHelper.formatDate(started));
        }
        if (finished != null) {
            result.put("finished", finished.getTime());
            long diff = (finished.getTime() - started.getTime());
            result.put("executionTime", PipeListHelper.formatDuration(diff));
        }
        return result;
    }
}
