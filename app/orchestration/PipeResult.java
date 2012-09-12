package orchestration;

import java.util.ArrayList;
import java.util.List;

//TODO: This should be modified
public class PipeResult {

    private final Pipe pipe;
    private final List<PhaseResult> phaseResults = new ArrayList<PhaseResult>();

    public PipeResult(Pipe pipe, List<PhaseResult> phaseResults) {
        this.pipe = pipe;
        for (PhaseResult phaseResult : phaseResults) {
            this.phaseResults.add(phaseResult);
        }
    }

//    public TaskState result() {
//        return getLastPhaseResult().result();
//    }

    public int executedPhases() {
        return phaseResults.size();
    }

    private PhaseResult getLastPhaseResult() {
        return phaseResults.get(phaseResults.size() - 1);
    }

}
