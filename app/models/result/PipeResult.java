package models.result;

import java.util.ArrayList;
import java.util.List;

import models.execution.Pipe;

public class PipeResult extends AbstractResult {

    private final Pipe pipe;
    private final List<PhaseResult> phaseResults = new ArrayList<PhaseResult>();

    public PipeResult(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public ResultLevel result() {
        return getLastPhaseResult().result();
    }

    public void add(PhaseResult phaseResult) {
        phaseResults.add(phaseResult);
    }

    public int executedPhases() {
        return phaseResults.size();
    }

    private PhaseResult getLastPhaseResult() {
        return phaseResults.get(phaseResults.size() - 1);
    }

}
