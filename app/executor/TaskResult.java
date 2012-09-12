package executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import play.Logger;

public class TaskResult {

    public static final TaskResult EMPTY_FAILED_RESULT = new TaskResult();

    private final StringBuilder out = new StringBuilder();
    private final StringBuilder err = new StringBuilder();
    private final int exitValue;

    /** Only call this for a finished process */
    TaskResult(Process process) {
        readOut(new BufferedReader(new InputStreamReader(process.getInputStream())));
        readErr(new BufferedReader(new InputStreamReader(process.getErrorStream())));
        exitValue = process.exitValue();
    }

    /** Creates an empty result (failed) */
    private TaskResult() {
        err.append("Unknown error");
        out.append("");
        exitValue = 666;
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
