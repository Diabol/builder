package notification.impl;

import java.util.Date;

import models.PipeVersion;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PhaseStatusChangedListener;
import notification.PipeStatusChangedListener;
import notification.TaskStatusChangedListener;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import play.mvc.WebSocket;
import utils.DBHelper;
import utils.DataNotFoundException;
import controllers.PipeListHelper;

/**
 * Created by Daniel Grönberg
 * 
 * This listener is specific for the pipe list view. Consider implementing your
 * own listener if you need other data pushed to your page.
 */
public class PipeListStatusChangeListener implements PhaseStatusChangedListener,
        TaskStatusChangedListener, PipeStatusChangedListener {
    private final WebSocket.In<JsonNode> in;
    private final WebSocket.Out<JsonNode> out;

    public PipeListStatusChangeListener(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void recieveStatusChanged(PhaseStatus status) {
        ObjectNode json = Json.newObject();
        json.put("phaseUpdate", true);
        json.put("pipeName", status.getPipeName());
        json.put("phaseName", status.getPhaseName());
        json.put("status", status.getStatus().toString());
        json.put("version", status.getVersion());
        if (status.isRunning()) {
            json.put("started", PipeListHelper.formatDate(status.getStarted()));
        } else {
            try {
                // Started is not set on statuses about finished phase. Reading
                // it from the first task of the phase.
                Date started = DBHelper.getInstance()
                        .getTasks(status.getPipeName(), status.getVersion(), status.getPhaseName())
                        .get(0).started;
                long diff = (status.getFinished().getTime() - started.getTime());
                json.put("started", PipeListHelper.formatDate(started));
                json.put("executionTime", PipeListHelper.formatDuration(diff));
                json.put("finished", PipeListHelper.formatDate(status.getFinished()));
            } catch (DataNotFoundException ex) {
                Logger.error("Could not set started and finished of phase when pushing to pipe list view. "
                        + ex.getMessage());
            }
        }
        out.write(json);
    }

    @Override
    public void recieveStatusChanged(TaskStatus status) {
        ObjectNode json = Json.newObject();
        json.put("taskUpdate", true);
        json.put("pipeName", status.getPipeName());
        json.put("phaseName", status.getPhaseName());
        json.put("taskName", status.getTaskName());
        json.put("status", status.getStatus().toString());
        json.put("version", status.getVersion());
        out.write(json);
    }

    @Override
    public void receiveNewVersion(PipeVersion version) {
        ObjectNode json = Json.newObject();
        json.put("newPipeVersion", true);
        json.put("pipeName", version.getPipeName());
        json.put("version", version.getVersion());
        json.put("committer", version.getVersionControlInfo().committer.name);
        json.put("commitMsg", version.getVersionControlInfo().versionControlText);
        out.write(json);

    }
}
