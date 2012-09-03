package models.result;

import static models.result.ResultLevel.SUCESS;
import models.Task;

public class TaskResult extends AbstractResult {

    private final Task task;

    public TaskResult(Task task) {
        this.task = task;
    }

    @Override
    public ResultLevel result() {
        // TODO Auto-generated method stub
        return SUCESS;
    }

}
