package notification.impl;

import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PhaseStatusChangedListener;
import notification.TaskStatusChangedListener;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import play.mvc.WebSocket;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-09-14
 * Time: 10:40
 * To change this template use File | Settings | File Templates.
 */
public class PipeListStatusChangeListener implements PhaseStatusChangedListener, TaskStatusChangedListener {
    private WebSocket.In<JsonNode> in;
    private WebSocket.Out<JsonNode> out;

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
        out.write(json);
    }
}
