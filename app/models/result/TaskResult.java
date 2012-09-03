package models.result;

import static models.result.Result.ResultLevel.SUCESS;
import models.Task;

public class TaskResult implements Result {

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
