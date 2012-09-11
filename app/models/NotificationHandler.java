package models;

import play.Logger;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;

import akka.util.*;
import akka.actor.*;
import akka.dispatch.*;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import java.util.*;
import java.util.List;

/**
 * Handler of all web socket connections.
 */
public class NotificationHandler extends UntypedActor {

    // Default handler.
    public static ActorRef defaultHandler = Akka.system().actorOf(new Props(NotificationHandler.class));


    List<WebSocket.Out<JsonNode>> listeners = new ArrayList<play.mvc.WebSocket.Out<JsonNode>>();

    public static void addSocket(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) throws Exception {
        // For each event received on the socket,
        in.onMessage(new Callback<JsonNode>() {
            public void invoke(JsonNode event) {

            }
        });

        // When the socket is closed.
        in.onClose(new Callback0() {
            public void invoke() {
                // Send a Quit message to the room.
                defaultHandler.tell(new RemoveSocket(out));
            }
        });

        // Send the Add Socket message to the handler
        defaultHandler.tell(new AddSocket(out));

    }

    @Override
    public void onReceive(Object message) {
        /**
         * Handle adding of new Web socket out channel
         */
        if (message instanceof AddSocket) {
            Logger.info("Adding socket");
            listeners.add(((AddSocket) message).channel);
        }
        /**
         * Handle removing a listener
         */
        else if (message instanceof RemoveSocket) {
            Logger.info("Removing socket");
            listeners.remove(((RemoveSocket) message).channel);
        }
        /**
         * Handle sending json to all listeners
         */
        else if (message instanceof SendJson) {
            Logger.info("Sending json: " + ((SendJson) message).json);
            for (WebSocket.Out<JsonNode> channel : listeners) {
                channel.write(((SendJson) message).json);
            }
        }


    }

    // -- Messages

    public static class AddSocket {
        final WebSocket.Out<JsonNode> channel;

        public AddSocket(WebSocket.Out<JsonNode> channel) {
            this.channel = channel;
        }
    }

    public static class SendJson {

        final ObjectNode json;

        public SendJson(ObjectNode json) {
            this.json = json;
        }
    }

    public static class RemoveSocket {
        final WebSocket.Out<JsonNode> channel;

        public RemoveSocket(WebSocket.Out<JsonNode> channel) {
            this.channel = channel;
        }
    }
}
