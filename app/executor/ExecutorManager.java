package executor;

import java.util.HashMap;

public class ExecutorManager {

    private static final HashMap<String, PipeExecutor> EXECUTORS = new HashMap<String, PipeExecutor>();

    private ExecutorManager() {
        // Private constructor
    }

    public static synchronized PipeExecutor get(String pipeName) {
        PipeExecutor executor = EXECUTORS.get(pipeName);
        if (executor == null) {
            // TODO
        }
        return null;
    }
}
