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
import utils.LogHandler;
import utils.PipeConfReader;
import views.html.pipeslist;
import views.html.startbuttons;
import views.html.taskdetails;
import executor.TaskExecutor;

public class Pipes extends Controller {

    private static PipeConfReader configReader = PipeConfReader.getInstance();
    private static DBHelper dbHelper = DBHelper.getInstance();
    private static PipeNotificationHandler notificationHandler = PipeNotificationHandler
            .getInstance();
    private static TaskExecutor taskExecutor = TaskExecutor.getInstance();
    private static LogHandler logHandler = LogHandler.getInstance();

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

    public static void setLogHandler(LogHandler logHandler) {
        Pipes.logHandler = logHandler;
    }

    public static Result list() {
        List<Pipe> latestPipes = createLatestPipes();
        return ok(pipeslist.render(latestPipes));
    }

    /**
     * Get the latest version of each configured pipe. Create a not started pipe
     * if not started.
     * 
     * @return
     */
    private static List<Pipe> createLatestPipes() {
        List<Pipe> result = new ArrayList<Pipe>();
        for (PipeConfig pipeConf : configReader.getConfiguredPipes()) {
            try {
                Pipe latest = dbHelper.getLatestPipe(pipeConf);
                result.add(latest);
            } catch (DataNotFoundException ex) {
                Pipe notYetStarted = createNotStartedPipe(pipeConf);
                result.add(notYetStarted);
            }
        }
        return result;
    }

    private static Pipe createNotStartedPipe(PipeConfig pipeConf) {
        Pipe result = Pipe.createNewFromConfig("N/A", pipeConf,
                VersionControlInfo.createVCInfoNotAvailable());
        for (PhaseConfig phaseConf : pipeConf.getPhases()) {
            Phase phase = Phase.createNewFromConfig(phaseConf);
            result.phases.add(phase);
            for (TaskConfig taskConfig : phaseConf.getTasks()) {
                phase.tasks.add(Task.createNewFromConfig(taskConfig));
            }
        }
        return result;
    }

    /**
     * Starts a pipe of a given name.
     * 
     * @param pipeName
     * @return
     */
    public static Result start(String pipeName) {
        return startPipe(pipeName, VersionControlInfo.createVCInfoNotAvailable());
    }

    /**
     * Starts a pipe of a given name and adds the given version control info to
     * it.
     * 
     * @param pipeName
     * @param vcInfo
     * @return
     */
    public static Result start(String pipeName, VersionControlInfo vcInfo) {
        return startPipe(pipeName, vcInfo);
    }

    private static Result startPipe(String pipeName, VersionControlInfo vcInfo) {
        try {
            PipeVersion pipeVersion = new Orchestrator(configReader, dbHelper, notificationHandler,
                    taskExecutor, logHandler).start(pipeName, vcInfo);
            String pipeUrl = createNewPipeUrl(pipeVersion);
            return generatePipeStartedResult(pipeVersion, pipeUrl);
        } catch (DataNotFoundException ex) {
            Logger.error("Could not start " + pipeName + ". " + ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
    }

    /**
     * Starts a specific task of a running pipe.
     * 
     * @param taskName
     * @param phaseName
     * @param pipeName
     * @param pipeVersion
     * @return
     */
    public static Result startTask(String taskName, String phaseName, String pipeName,
            String pipeVersion) {
        String msg = "task: '" + taskName + "' in phase: '" + phaseName + "' in pipe: '" + pipeName
                + "'";
        try {
            new Orchestrator(configReader, dbHelper, notificationHandler, taskExecutor, logHandler)
                    .startTask(taskName, phaseName, pipeName, pipeVersion);
            return ok("Started " + msg + "' of version: '" + pipeVersion);
        } catch (DataNotFoundException e) {
            String errMsg = "Could not start " + msg;
            Logger.error(errMsg, e);
            return notFound(errMsg);
        }
    }

    public static Result getTaskLog(String taskName, String phaseName, String pipeName,
            String pipeVersion) {
        String msg = "task: '" + taskName + "' in phase: '" + phaseName + "' in pipe: '" + pipeName
                + "' of version '" + pipeVersion + "'";
        try {
            String key = taskName + phaseName + pipeName + pipeVersion;
            String log = logHandler.getLog(key);
            return ok(log);
        } catch (DataNotFoundException e) {
            String errMsg = "Could not retrieve log for " + msg;
            Logger.error(errMsg, e);
            return notFound(errMsg);
        }
    }

    public static Result taskDetails(String taskName, String phaseName, String pipeName,String pipeVersion) {
        return ok(taskdetails.render(taskName, phaseName, pipeName,pipeVersion));
    }

    public static Result startButtons() {
        return ok(startbuttons.render(configReader.getConfiguredPipes()));
    }

    /**
     * Returns the tasks of the latest run of the given phase.
     * 
     * @param pipeName
     * @param phaseName
     * @return [Task]
     */
    public static Result getTasksForLatestVersion(String pipeName, String phaseName) {
        PipeConfig config;
        PhaseConfig phaseConfig;
        try {
            config = configReader.get(pipeName);
            phaseConfig = config.getPhaseByName(phaseName);
            if (phaseConfig == null) {
                String errorMsg = "Phase with name '" + phaseName + "' is not configured for '"
                        + pipeName + "'.";
                Logger.error(errorMsg);
                return notFound(errorMsg);
            }
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
        List<Task> tasks;
        try {
            tasks = dbHelper.getLatestTasks(pipeName, phaseName);
        } catch (DataNotFoundException ex) {
            tasks = createNotStartedTasks(phaseConfig);
        }
        List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
        for (Task task : tasks) {
            jsonList.add(task.toObjectNode());
        }
        return ok(Json.toJson(jsonList.toArray()));

    }

    /**
     * Returns the tasks of a given phase and version.
     * 
     * @param pipeName
     * @param version
     * @param phaseName
     * @return [Task]
     */
    public static Result getTasks(String pipeName, String version, String phaseName) {
        try {
            List<Task> tasks = dbHelper.getTasks(pipeName, version, phaseName);
            List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
            for (Task task : tasks) {
                jsonList.add(task.toObjectNode());
            }
            return ok(Json.toJson(jsonList.toArray()));
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
    }

    private static List<Phase> createNotStartedPhases(PipeConfig pipeConf) {
        List<Phase> result = new ArrayList<Phase>();
        for (PhaseConfig phaseConf : pipeConf.getPhases()) {
            Phase phase = Phase.createNewFromConfig(phaseConf);
            for (TaskConfig task : phaseConf.getTasks()) {
                phase.tasks.add(Task.createNewFromConfig(task));
            }
            result.add(phase);
        }
        return result;
    }

    private static List<Task> createNotStartedTasks(PhaseConfig phaseConf) {
        List<Task> result = new ArrayList<Task>();
        for (TaskConfig task : phaseConf.getTasks()) {
            result.add(Task.createNewFromConfig(task));
        }
        return result;
    }

    /**
     * Sets up a web socket.
     * 
     * @return
     */
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
                    in.onClose(new F.Callback0() {
                        @Override
                        public void invoke() {
                            notificationHandler.removePhaseStatusChangedListener(listener);
                            notificationHandler.removeTaskStatusChangedListener(listener);
                            notificationHandler.removePipeStatusChangedListener(listener);
                        }
                    });
                    ObjectNode json = Json.newObject();
                    json.put("socket", "ready");
                    out.write(json);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Result incrementMajor(String pipeName) {
        try {
            PipeVersion pipeVersion = new Orchestrator(configReader, dbHelper, notificationHandler,
                    taskExecutor, logHandler).incrementMajor(pipeName);
            String pipeUrl = createNewPipeUrl(pipeVersion);
            return generatePipeStartedResult(pipeVersion, pipeUrl);
        } catch (DataNotFoundException ex) {
            Logger.error(
                    "Could not increment major version for " + pipeName + ". " + ex.getMessage(),
                    ex);
            return notFound(ex.getMessage());
        }
    }

    /**
     * Returns the phases of the latest version of a given pipe.
     * 
     * @param pipeName
     * @return [Phase]
     */
    public static Result getPhasesForLatestVersion(String pipeName) {
        PipeConfig config;
        try {
            config = configReader.get(pipeName);
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
        List<Phase> phases;
        try {
            Pipe latest = dbHelper.getLatestPipe(config);
            phases = latest.phases;
        } catch (DataNotFoundException ex) {
            phases = createNotStartedPhases(config);
        }
        return ok(createJsonForPhases(phases));
    }

    /**
     * returns the phases of a given pipe and version.
     * 
     * @param pipeName
     * @param pipeVersion
     * @return [Phase]
     */
    public static Result getPhases(String pipeName, String pipeVersion) {
        List<Phase> phases;
        try {
            Pipe pipe = dbHelper.getPipe(pipeName, pipeVersion);
            phases = pipe.phases;
        } catch (DataNotFoundException ex) {
            return notFound(ex.getMessage());
        }
        return ok(createJsonForPhases(phases));
    }

    private static JsonNode createJsonForPhases(List<Phase> phases) {
        List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
        for (Phase phase : phases) {
            jsonList.add(phase.toObjectNode());
        }
        return Json.toJson(jsonList.toArray());
    }

    /**
     * Returns the latest version of all configured pipes.
     * 
     * @param pipeName
     * @return {Pipe}
     */
    public static Result getLatestPipes() {
        List<Pipe> latestPipes = createLatestPipes();
        List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
        for (Pipe pipe : latestPipes) {
            jsonList.add(pipe.toObjectNode());
        }
        return ok(Json.toJson(jsonList.toArray()));

    }

    /**
     * Get the latest version of a given pipe
     * 
     * @param pipeName
     * @return
     */
    public static Result getLatestPipe(String pipeName) {
        PipeConfig config;
        try {
            config = configReader.get(pipeName);
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
        Pipe result;
        try {
            result = dbHelper.getLatestPipe(config);
        } catch (DataNotFoundException ex) {
            result = createNotStartedPipe(config);
        }
        return ok(result.toObjectNode());
    }

    /**
     * Returns the pipe of the given version.
     * 
     * @param pipeName
     * @param pipeVersion
     * @return {Pipe}
     */
    public static Result getPipe(String pipeName, String pipeVersion) {
        Pipe result;
        try {
            result = dbHelper.getPipe(pipeName, pipeVersion);
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
        return ok(result.toObjectNode());
    }

    /**
     * Returns a map of version to VersionControlInfo of a specific pipe.
     * 
     * @param pipeName
     * @return {version:VersionControlInfo}
     */
    public static Result getPipeVersions(String pipeName) {
        ObjectNode json = Json.newObject();
        try {
            List<Pipe> pipes = dbHelper.getAll(pipeName);
            for (Pipe pipe : pipes) {
                json.put(pipe.version, pipe.versionControlInfo.toObjectNode());
            }
            return ok(json);
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
    }

    /**
     * Get the latest versions of a pipe, limited by a given number.
     * 
     * @param pipe
     * @param number
     * @return
     */
    public static Result getNumberOfLatestPipe(String pipeName, int number) {
        try {
            List<Pipe> pipes = dbHelper.getAll(pipeName);
            List<Pipe> resultList;
            if (pipes.size() <= number) {
                resultList = pipes;
            } else {
                resultList = pipes.subList(pipes.size() - number, pipes.size());
            }
            List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
            for (Pipe pipe : resultList) {
                jsonList.add(pipe.toObjectNode());
            }
            return ok(Json.toJson(jsonList.toArray()));
        } catch (DataNotFoundException ex) {
            Logger.error(ex.getMessage(), ex);
            return notFound(ex.getMessage());
        }
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