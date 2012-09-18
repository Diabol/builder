package orchestration;

import java.util.ArrayList;
import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;

/**
 * An instance of a pipeline, ie a specific run of a pipe.
 * Config is taken from {@link PipeConfig}.
 * Result is stored in {@link PipeResult}.
 * 
 * @author marcus
 */
class Pipe {

    // TODO This logic should be aligned with new Orchestrator or logic.

    private final PipeConfig config;

    Pipe(PipeConfig config) {
        this.config = config;
    }

    PipeResult start() {
        List<PhaseResult> phaseResultList = new ArrayList<PhaseResult>();

        for (PhaseConfig phaseConfig : getPhases()) {
            PhaseResult phaseResult = new Phase(phaseConfig).start();
            phaseResultList.add(phaseResult);
//            if (!phaseResult.success()) {
//                break;
//            }
        }

        return new PipeResult(this, phaseResultList);
    }

    public String getName() {
        return config.getName();
    }

    public List<PhaseConfig> getPhases() {
        return config.getPhases();
    }

}
