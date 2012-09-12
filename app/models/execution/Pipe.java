package models.execution;

import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.PipeValidationException;
import models.result.PhaseResult;
import models.result.PipeResult;

/**
 * An instance of a pipeline, ie a specific run of a pipe.
 * Config is taken from {@link PipeConfig}.
 * Result is stored in {@link PipeResult}.
 * 
 * @author marcus
 */
public class Pipe {

    private final PipeConfig config;

    public Pipe(PipeConfig config) throws PipeValidationException {
        config.validate();
        this.config = config;
    }

    public PipeResult start() throws PipeValidationException {
        PipeResult result = new PipeResult(this);

        for (PhaseConfig phaseConfig : getPhases()) {
            PhaseResult phaseResult = phaseConfig.createPhase().start();
            result.add(phaseResult);
            if (!phaseResult.success()) {
                break;
            }
        }

        return result;
    }

    public String getName() {
        return config.getName();
    }

    public List<PhaseConfig> getPhases() {
        return config.getPhases();
    }

}
