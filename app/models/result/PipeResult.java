package models.result;

import static models.result.ResultLevel.SUCESS;

import java.util.ArrayList;
import java.util.List;

import models.Pipe;

public class PipeResult implements Result {

    private final Pipe pipe;
    private final List<PhaseResult> phaseResults = new ArrayList<PhaseResult>();

    public PipeResult(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public ResultLevel result() {
        // TODO Return the sum of all phase results
        return SUCESS;
    }

    public void add(PhaseResult phaseResult) {
        phaseResults.add(phaseResult);
    }

}
