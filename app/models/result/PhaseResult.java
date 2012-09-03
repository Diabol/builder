package models.result;

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
        // TODO Auto-generated method stub
        return SUCESS;
    }

    public void addTaskResult(TaskResult taskResult) {
        taskResults.add(taskResult);
    }

}
