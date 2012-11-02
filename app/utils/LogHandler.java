package utils;

import java.util.HashMap;
import java.util.Map;

public class LogHandler {
    private static LogHandler instance;

    static {
        instance = new LogHandler();
    }

    private LogHandler() {

    }

    public static LogHandler getInstance() {
        return instance;
    }

    private final Map<String, String> logMap = new HashMap<String, String>();

    public synchronized String getLog(String key) throws DataNotFoundException {
        String result = logMap.get(key);
        if (result != null) {
            return result;
        } else {
            throw new DataNotFoundException("No log found for key: " + key);
        }
    }

    public synchronized void storeLog(String key, String log) {
        logMap.put(key, log);
    }
}
