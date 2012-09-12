package controllers;

import models.NotificationHandler;
import models.config.PipeValidationException;
import orchestration.Orchestrator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.PipeConfReader;
import views.html.pipeconfgraphit;
import views.html.startbuttons;

public class Pipes extends Controller {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();

    public static Result list() {
        return ok(pipeconfgraphit.render(configReader.getConfiguredPipes()));
    }

    public static Result start(String pipeName) {
        // (new Thread(new SimplePipeExecutor(pipe))).run();
        try {
            new Orchestrator().start(pipeName);
        } catch (PipeValidationException e) {
            Logger.error("Could not start pipe: " + pipeName + " due to invalid config.", e);
            return internalServerError();
        }
        return ok();
    }

    public static Result startTask(String taskName, String phaseName, String pipeName) {
        try {
            new Orchestrator().startTask(taskName, phaseName, pipeName);
        } catch (PipeValidationException e) {
            Logger.error("Could not start task: " + taskName + " of phase: " + phaseName + " of pipe: " + pipeName + " due to invalid config.", e);
            return internalServerError();
        }
        return ok();
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
}