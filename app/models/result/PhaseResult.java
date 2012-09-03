package models.result;

import static models.result.ResultLevel.FAILURE;
import static models.result.ResultLevel.SUCESS;

import java.util.ArrayList;
import java.util.List;

import models.Phase;

public class PhaseResult extends AbstractResult {

    private final Phase phase;
    private final List<TaskResult> taskResults = new ArrayList<TaskResult>();

    public PhaseResult(Phase phase) {
        this.phase = phase;
    }

    @Override
    public ResultLevel result() {
        // TODO We need to handle ongoing phases.
        // A phase may be open for an extended period if it has a manual step.

        for (TaskResult taskResult : taskResults) {
            if (!taskResult.success()) {
                return FAILURE;
            }
        }
        return SUCESS;
    }

    public void addTaskResult(TaskResult taskResult) {
        taskResults.add(taskResult);
    }

    public int executedTasks() {
        return taskResults.size();
    }

}
