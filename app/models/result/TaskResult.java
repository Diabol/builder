package models.result;

import static models.result.ResultLevel.FAILURE;
import static models.result.ResultLevel.SUCESS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import models.Task;

public class TaskResult extends AbstractResult implements Result {

    private final Task task;
    private final StringBuilder out = new StringBuilder();
    private final StringBuilder err = new StringBuilder();
    private int exitValue;

    public TaskResult(Task task) {
        this.task = task;
    }

    public void setResult(Process process) throws IOException {
        readOut(new BufferedReader(new InputStreamReader(process.getInputStream())));
        readErr(new BufferedReader(new InputStreamReader(process.getErrorStream())));
        setExitValue(process.exitValue());
    }

    public String getCmd() {
        return task.getCommand();
    }

    public String getOut() {
        return out.toString();
    }

    public String getErr() {
        return err.toString();
    }

    private void readOut(BufferedReader outReader) throws IOException {
        readBuffer(outReader, out);
    }

    private void readErr(BufferedReader errReader) throws IOException {
        readBuffer(errReader, err);
    }

    public int getExitValue() {
        return exitValue;
    }

    private void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    private void readBuffer(BufferedReader reader, StringBuilder sb) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
        }
    }

    @Override
    public ResultLevel result() {
        if (exitValue == 0) {
            return SUCESS;
        } else {
            return FAILURE;
        }
    }

}
