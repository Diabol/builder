package controllers;

import java.net.MalformedURLException;
import java.net.URL;

import models.NotificationHandler;
import models.config.PipeValidationException;
import orchestration.Orchestrator;
import orchestration.PipeVersion;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
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
        // (new Thread(new SimplePipeExecutor(pipe))).run();
        try {
            PipeVersion<?> pipeVersion = new Orchestrator().start(pipeName);
            URL pipeUrl = createNewPipeUrl(pipeVersion);
            return generatePipeStartedResult(pipeVersion, pipeUrl);
        } catch (PipeValidationException e) {
            Logger.error("Could not start pipe: " + pipeName + " due to invalid config.", e);
            return internalServerError();
        }
    }

    public static Result startTask(String taskName, String phaseName, String pipeName, String pipeVersion) {
        String msg = "task: '" + taskName + "' in phase: '" + phaseName + "' in pipe: '" + pipeName;
        try {
            new Orchestrator().startTask(taskName, phaseName, pipeName, pipeVersion);
            return ok("Started " + msg + "' of version: '" + pipeVersion);
        } catch (PipeValidationException e) {
            String errMsg = "Could not start " + msg + " due to invalid config.";
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

                // Add add socket to notification handler.
                try {
                    ObjectNode json = Json.newObject();
                    json.put("socket", "ready");
                    out.write(json);
                    NotificationHandler.addSocket(in,out);
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