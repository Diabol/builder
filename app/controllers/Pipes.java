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
import models.statusdata.VersionControlInfo;
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
import executor.TaskExecutor;

public class Pipes extends Controller {

    private static PipeConfReader configReader = PipeConfReader.getInstance();
    private static DBHelper dbHelper = DBHelper.getInstance();
    private static PipeNotificationHandler notificationHandler = PipeNotificationHandler
            .getInstance();
    private static TaskExecutor taskExecutor = TaskExecutor.getInstance();

    /**
     * The below setters is needed for mocking in test.
     * 
     */
    public static void setDBHelper(DBHelper dbHelper) {
        Pipes.dbHelper = dbHelper;
    }

    public static void setPipeNotificationHandler(PipeNotificationHandler notificationHandler) {
        Pipes.notificationHandler = notificationHandler;
    }

    public static void setTaskexecutor(TaskExecutor taskExecutor) {
        Pipes.taskExecutor = taskExecutor;
    }

    public static void setPipeConfigReader(PipeConfReader configReader) {
        Pipes.configReader = configReader;
    }

    public static Result list() {
        List<Pipe> latestPipes = new ArrayList<Pipe>();
        for (PipeConfig pipeConf : configReader.getConfiguredPipes()) {
            try {
                Pipe latest = dbHelper.getLatestPipe(pipeConf);
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
        result.versionControlInfo = VersionControlInfo.createVCInfoNotAvailable();
        for (PhaseConfig phaseConf : pipeConf.getPhases()) {
            Phase phase = Phase.createNewFromConfig(phaseConf);
            result.phases.add(phase);
            for (TaskConfig taskConfig : phaseConf.getTasks()) {
                phase.tasks.add(Task.createNewFromConfig(taskConfig));
            }
        }
        return result;
    }

    public static Result start(String pipeName) {
        return startPipe(pipeName, VersionControlInfo.createVCInfoNotAvailable());
    }

    public static Result start(String pipeName, VersionControlInfo vcInfo) {
        return startPipe(pipeName, vcInfo);
    }

    private static Result startPipe(String pipeName, VersionControlInfo vcInfo) {
        try {
            PipeVersion pipeVersion = new Orchestrator(configReader, dbHelper, notificationHandler,
                    taskExecutor).start(pipeName, vcInfo);
            String pipeUrl = createNewPipeUrl(pipeVersion);
            return generatePipeStartedResult(pipeVersion, pipeUrl);
        } catch (Exception e) {
            String errMsg = "Could not start pipe: " + pipeName;
            e.printStackTrace();
            Logger.error(errMsg, e);
            return internalServerError(errMsg);
        }
    }

    public static Result startTask(String taskName, String phaseName, String pipeName,
            String pipeVersion) {
        String msg = "task: '" + taskName + "' in phase: '" + phaseName + "' in pipe: '" + pipeName
                + "'";
        try {
            new Orchestrator(configReader, dbHelper, notificationHandler, taskExecutor).startTask(
                    taskName, phaseName, pipeName, pipeVersion);
            return ok("Started " + msg + "' of version: '" + pipeVersion);
        } catch (DataNotFoundException e) {
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
            tasks = dbHelper.getLatestTasks(pipeName, phaseName);
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
            List<Task> tasks = dbHelper.getTasks(pipeName, version, phaseName);
            List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
            for (Task task : tasks) {
                jsonList.add(task.toObjectNode());
            }
            return ok(Json.toJson(jsonList.toArray()));
        } catch (DataNotFoundException ex) {
            return notFound(ex.getMessage());
        }
    }

    private static List<Phase> createNotStartedPhases(String pipeName) {
        List<Phase> result = new ArrayList<Phase>();
        PipeConfig pipeConf = configReader.get(pipeName);
        for (PhaseConfig phaseConf : pipeConf.getPhases()) {
            Phase phase = Phase.createNewFromConfig(phaseConf);
            for (TaskConfig task : phaseConf.getTasks()) {
                phase.tasks.add(Task.createNewFromConfig(task));
            }
            result.add(phase);
        }
        return result;
    }

    private static List<Task> createNotStartedTasks(String pipeName, String phaseName) {
        List<Task> result = new ArrayList<Task>();
        PipeConfig pipeConf = configReader.get(pipeName);
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
                    notificationHandler.addPhaseStatusChangedListener(listener);
                    notificationHandler.addTaskStatusChangedListener(listener);
                    notificationHandler.addPipeStatusChangedListener(listener);
                    // Make sure the listeners are removed when socket is
                    // closed.
                    // When the socket is closed.
                    in.onClose(new F.Callback0() {
                        @Override
                        public void invoke() {
                            // Send a Quit message to the room.
                            notificationHandler.removePhaseStatusChangedListener(listener);
                            notificationHandler.removeTaskStatusChangedListener(listener);
                            notificationHandler.removePipeStatusChangedListener(listener);
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

    public static Result getPhasesForLatestVersion(String pipeName) {
        List<Phase> phases;
        try {
            Pipe latest = dbHelper.getLatestPipe(configReader.get(pipeName));
            phases = latest.phases;
        } catch (DataNotFoundException ex) {
            phases = createNotStartedPhases(pipeName);
        }
        return ok(createJsonForPhase(phases));
    }

    public static Result getPhases(String pipeName, String pipeVersion) {
        List<Phase> phases;
        try {
            Pipe pipe = dbHelper.getPipe(pipeName, pipeVersion);
            phases = pipe.phases;
        } catch (DataNotFoundException ex) {
            return notFound(ex.getMessage());
        }
        return ok(createJsonForPhase(phases));
    }

    private static JsonNode createJsonForPhase(List<Phase> phases) {
        List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
        for (Phase phase : phases) {
            jsonList.add(phase.toObjectNode());
        }
        return Json.toJson(jsonList.toArray());
    }

    public static Result getLatestPipes() {
        return TODO;
    }

    public static Result getLatestPipe(String pipeName) {
        return TODO;
    }

    public static Result getPipe(String pipeName, String pipeVersion) {
        return TODO;
    }

    public static Result getPipeVersions(String pipeName) {
        return TODO;
    }

    public static Result getNumberOfLatestPipe(String pipe, int number) {
        return TODO;
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