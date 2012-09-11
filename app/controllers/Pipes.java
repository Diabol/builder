package controllers;

import executor.SimplePipeExecutor;
import models.NotificationHandler;
import models.Pipe;
import models.config.PipeConfig;
import models.config.PipeValidationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.PipeConfReader;
import views.html.pipe;
import views.html.pipeconfgraphit;
import views.html.startbuttons;

public class Pipes extends Controller {

    private static PipeConfReader configReader = PipeConfReader.getInstance() ;

    public static Result list() {
        return ok(pipeconfgraphit.render(configReader.getConfiguredPipes()));
    }

    public static Result startPipeExecutor(String name) {
        PipeConfig pipe = PipeConfReader.getInstance().get(name);
        (new Thread(new SimplePipeExecutor(pipe))).run();
        return ok();
    }

    public static Result startButtons() {
        return ok(startbuttons.render(configReader.getConfiguredPipes()));
    }

    public static Result start(String name) throws PipeValidationException {
        Pipe newPipe = new Pipe(configReader.get(name));
        newPipe.start();
        return ok(pipe.render(newPipe));
    }

    public static WebSocket<JsonNode> setupSocket() {
        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
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