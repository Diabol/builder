package notification.impl;

import models.PipeVersion;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PhaseStatusChangedListener;
import notification.PipeStatusChangedListener;
import notification.TaskStatusChangedListener;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.WebSocket;
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

        String started = status.getStarted() != null ? PipeListHelper.formatDate(status
                .getStarted()) : "Not yet started";
        json.put("started", started);
        String finished = status.getFinished() != null ? PipeListHelper.formatDate(status
                .getFinished()) : "Not yet finished";
        json.put("finished", finished);
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
        json.put("commitId", version.getVersionControlInfo().versionControlId);
        json.put("commitMsg", version.getVersionControlInfo().versionControlText);
        out.write(json);

    }
}
