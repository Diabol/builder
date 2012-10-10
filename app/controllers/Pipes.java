package controllers;

import java.util.ArrayList;
import java.util.List;

import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import notification.PipeNotificationHandler;
import notification.impl.PipeListStatusChangeListener;
import orchestration.Orchestrator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.DBHelper;
import utils.DataNotFoundException;
import utils.PipeConfReader;
import views.html.pipeslist;
import views.html.startbuttons;

public class Pipes extends Controller {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();

    public static Result list() {
        List<Pipe> latestPipes = new ArrayList<Pipe>();
        for (PipeConfig pipeConf : configReader.getConfiguredPipes()) {
            try {
                Pipe latest = DBHelper.getInstance().getLatestPipe(pipeConf);
                latestPipes.add(latest);
            } catch (DataNotFoundException ex) {
                Pipe notYetStarted = createNotStartedPipe(pipeConf);
                latestPipes.add(notYetStarted);
            }

        }
        return ok(pipeslist.render(latestPipes));
    }

    private static Pipe createNotStartedPipe(PipeConfig pipeConf) {
        Pipe result = Pipe.createNewFromConfig("NA", pipeConf);
        for (PhaseConfig phaseConf : pipeConf.getPhases()) {
            Phase phase = Phase.createNewFromConfig(phaseConf);
            result.phases.add(phase);
            /**
             * Tasks are not added to the initial data. Fetched when clicking on
             * a phase.
             */
        }
        return result;
    }

    public static Result start(String pipeName) {
        try {
            PipeVersion pipeVersion = new Orchestrator().start(pipeName);
            String pipeUrl = createNewPipeUrl(pipeVersion);
            return generatePipeStartedResult(pipeVersion, pipeUrl);
        } catch (Exception e) {
            String errMsg = "Could not start pipe: " + pipeName;
            Logger.error(errMsg, e);
            return internalServerError(errMsg);
        }
    }

    public static Result startTask(String taskName, String phaseName, String pipeName,
            String pipeVersion) {
        String msg = "task: '" + taskName + "' in phase: '" + phaseName + "' in pipe: '" + pipeName
                + "'";
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

    public static Result getTasksForLatestVersion(String pipeName, String phaseName) {
        List<Task> tasks;
        try {
            tasks = DBHelper.getInstance().getLatestTasks(pipeName, phaseName);
        } catch (DataNotFoundException ex) {
            tasks = createNotStartedTasks(pipeName, phaseName);
        }
        List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
        for (Task task : tasks) {
            jsonList.add(task.toObjectNode());
        }
        return ok(Json.toJson(jsonList.toArray()));

    }

    public static Result getTasks(String pipeName, String version, String phaseName) {
        try {
            List<Task> tasks = DBHelper.getInstance().getTasks(pipeName, version, phaseName);
            List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
            for (Task task : tasks) {
                jsonList.add(task.toObjectNode());
            }
            return ok(Json.toJson(jsonList.toArray()));
        } catch (DataNotFoundException ex) {
            return notFound(ex.getMessage());
        }
    }

    private static List<Task> createNotStartedTasks(String pipeName, String phaseName) {
        List<Task> result = new ArrayList<Task>();
        PipeConfig pipeConf = PipeConfReader.getInstance().get(pipeName);
        for (PhaseConfig phaseConf : pipeConf.getPhases()) {
            if (phaseConf.getName().equals(phaseName)) {
                for (TaskConfig task : phaseConf.getTasks()) {
                    result.add(Task.createNewFromConfig(task));
                }
                break;
            }
        }
        return result;
    }

    public static WebSocket<JsonNode> setupSocket() {
        return new WebSocket<JsonNode>() {
            // Called when the Websocket Handshake is done.
            @Override
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    final PipeListStatusChangeListener listener = new PipeListStatusChangeListener(
                            in, out);
                    PipeNotificationHandler.getInstance().addPhaseStatusChangedListener(listener);
                    PipeNotificationHandler.getInstance().addTaskStatusChangedListener(listener);
                    PipeNotificationHandler.getInstance().addPipeStatusChangedListener(listener);
                    // Make sure the listeners are removed when socket is
                    // closed.
                    // When the socket is closed.
                    in.onClose(new F.Callback0() {
                        @Override
                        public void invoke() {
                            // Send a Quit message to the room.
                            PipeNotificationHandler.getInstance().removePhaseStatusChangedListener(
                                    listener);
                            PipeNotificationHandler.getInstance().removeTaskStatusChangedListener(
                                    listener);
                            PipeNotificationHandler.getInstance().removePipeStatusChangedListener(
                                    listener);
                        }
                    });

                    // Notify the client tha
                    ObjectNode json = Json.newObject();
                    json.put("socket", "ready");
                    out.write(json);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
    }

    private static Status generatePipeStartedResult(PipeVersion version, String newPipeUrl) {
        response().setContentType("text/html");
        response().setHeader(LOCATION, newPipeUrl);
        return created("Version '" + version.getVersion() + "' of Pipe '" + version.getPipeName()
                + "' started! <ul><li>" + newPipeUrl + "</li></ul>");
    }

    private static String createNewPipeUrl(PipeVersion pipeVersion) {
        return "/pipe/" + pipeVersion.getPipeName() + "/" + pipeVersion.getVersion();
    }
}