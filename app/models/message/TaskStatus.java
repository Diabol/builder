package models.message;

import models.config.PhaseConfig;
import orchestration.PipeVersion;

import org.joda.time.ReadableDateTime;

public class TaskStatus extends StatusMessage {

    public TaskStatus(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started,
            ReadableDateTime finished) {
        super(status, version, phase, started, finished);
        // TODO Auto-generated constructor stub
    }

}
