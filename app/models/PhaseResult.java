package models;

import static models.result.Result.ResultLevel.SUCESS;
import models.result.Result;

public class PhaseResult implements Result {

    private final Phase phase;

    public PhaseResult(Phase phase) {
        this.phase = phase;
    }

    @Override
    public ResultLevel result() {
        // TODO Auto-generated method stub
        return SUCESS;
    }

}
