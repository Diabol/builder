package executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import play.Logger;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class TaskResult {

    private final StringBuilder out = new StringBuilder();
    private final StringBuilder err = new StringBuilder();
    private final int exitValue;
    private final TaskExecutionContext context;

    /** Only call this for a finished process */
    @SuppressWarnings(value = "DM_DEFAULT_ENCODING", justification = "The default encoding should be correct since I assume that's what process uses.")
    TaskResult(Process process, TaskExecutionContext context) {
        readOut(new BufferedReader(new InputStreamReader(process.getInputStream())));
        readErr(new BufferedReader(new InputStreamReader(process.getErrorStream())));
        exitValue = process.exitValue();
        this.context = context;
    }

    /** Creates an empty result (failed) */
    private TaskResult(TaskExecutionContext context) {
        err.append("Unknown error");
        out.append("");
        exitValue = 666;
        this.context = context;
    }

    static TaskResult getEmptyFailedResult(TaskExecutionContext context) {
        return new TaskResult(context);
    }

    public String out() {
        return out.toString();
    }

    public String err() {
        return err.toString();
    }

    public int exitValue() {
        return exitValue;
    }

    public boolean success() {
        return exitValue() == 0;
    }

    public TaskExecutionContext context() {
        return context;
    }

    private void readOut(BufferedReader outReader) {
        try {
            readBuffer(outReader, out);
        } catch (IOException e) {
            Logger.error("Failed to read out stream", e);
            out.append("");
        }
    }

    private void readErr(BufferedReader errReader) {
        try {
            readBuffer(errReader, err);
        } catch (IOException e) {
            Logger.error("Failed to read err stream", e);
            err.append("");
        }
    }

    private void readBuffer(BufferedReader reader, StringBuilder sb) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
        }
    }
}
