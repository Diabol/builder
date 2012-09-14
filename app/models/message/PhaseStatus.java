package models.message;

import models.config.PhaseConfig;
import orchestration.PipeVersion;

import org.joda.time.ReadableDateTime;

public class PhaseStatus extends StatusMessage {

    public PhaseStatus(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started,
            ReadableDateTime finished) {
        super(status, version, phase, started, finished);
        // TODO Auto-generated constructor stub
    }

}
