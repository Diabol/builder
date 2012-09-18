package controllers;

import java.net.MalformedURLException;
import java.net.URL;

import notification.PipeNotificationHandler;
import notification.impl.PipeListStatusChangeListener;
import orchestration.Orchestrator;
import orchestration.PipeVersion;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.PipeConfReader;
import views.html.pipeslist;
import views.html.startbuttons;

public class Pipes extends Controller {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();

    public static Result list() {
        // TODO We need to include current status in this list: Wrap PipeConfig with statuses
        return ok(pipeslist.render(configReader.getConfiguredPipes()));
    }

    public static Result start(String pipeName) {
        try {
            PipeVersion<?> pipeVersion = new Orchestrator().start(pipeName);
            URL pipeUrl = createNewPipeUrl(pipeVersion);
            return generatePipeStartedResult(pipeVersion, pipeUrl);
        } catch (Exception e) {
            String errMsg = "Could not start pipe: " + pipeName;
            Logger.error(errMsg, e);
            return internalServerError(errMsg);
        }
    }

    public static Result startTask(String taskName, String phaseName, String pipeName, String pipeVersion) {
        String msg = "task: '" + taskName + "' in phase: '" + phaseName + "' in pipe: '" + pipeName + "'";
        try {
            new Orchestrator().startTask(taskName, phaseName, pipeName, pipeVersion);
            return ok("Started " + msg + "' of version: '" + pipeVersion);
        } catch (Exception e) {
            String errMsg = "Could not start " + msg;
            Logger.error(errMsg, e);
            return internalServerError(errMsg);
        }
    }

    public static Result startButtons() {
        return ok(startbuttons.render(configReader.getConfiguredPipes()));
    }

    public static WebSocket<JsonNode> setupSocket() {
        return new WebSocket<JsonNode>() {
            // Called when the Websocket Handshake is done.
            @Override
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                try {
                    final PipeListStatusChangeListener listener = new PipeListStatusChangeListener(in,out);
                    PipeNotificationHandler.getInstance().addPhaseStatusChangedListener(listener);
                    PipeNotificationHandler.getInstance().addTaskStatusChangedListener(listener);

                    //Make sure the listeners are removed when socket is closed.
                    // When the socket is closed.
                    in.onClose(new F.Callback0() {
                        @Override
                        public void invoke() {
                            // Send a Quit message to the room.
                            PipeNotificationHandler.getInstance().removePhaseStatusChangedListener(listener);
                            PipeNotificationHandler.getInstance().removeTaskStatusChangedListener(listener);
                        }
                    });

                    //Notify the client tha
                    ObjectNode json = Json.newObject();
                    json.put("socket", "ready");
                    out.write(json);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
    }

    private static Status generatePipeStartedResult(PipeVersion<?> version, URL newPipeUrl) {
        response().setContentType("text/html");
        response().setHeader(LOCATION, newPipeUrl.toString());
        return created("Version '" + version.getVersion() + "' of Pipe '" + version.getPipeName() + "' started! <ul><li>" + newPipeUrl + "</li></ul>");
    }

    private static URL createNewPipeUrl(PipeVersion<?> pipeVersion) {
        try {
            return new URL("/pipe/" + pipeVersion.getPipeName() + "/" + pipeVersion.getVersion());
        } catch (MalformedURLException e) {
            Logger.error("Could not create url for pipe: " + pipeVersion, e);
            return null;
        }
    }
}