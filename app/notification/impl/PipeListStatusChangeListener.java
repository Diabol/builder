package notification.impl;

import models.PipeVersion;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.VersionControlInfo;
import notification.PhaseStatusChangedListener;
import notification.PipeStatusChangedListener;
import notification.TaskStatusChangedListener;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.WebSocket;

/**
 * Created by Daniel Grönberg
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
    public void receiveNewVersion(PipeVersion version, VersionControlInfo vcInfo) {
        ObjectNode json = Json.newObject();
        json.put("newPipeVersion", true);
        json.put("pipeName", version.getPipeName());
        json.put("version", version.getVersion());
        json.put("commitId", vcInfo.versionControlId);
        json.put("commitMsg", vcInfo.versionControlText);
        out.write(json);

    }
}
