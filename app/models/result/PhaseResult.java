package models.result;

import static models.result.ResultLevel.SUCESS;
import models.Phase;

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
