package models.message;

import models.PipeVersion;

import org.joda.time.ReadableDateTime;

public class PipeStatus extends AbstractMessage {

    private final PipeVersion version;

    protected PipeStatus(PipeVersion version, State state, ReadableDateTime started,
            ReadableDateTime finished) {
        super(state, started, finished);
        this.version = version;
    }

    public String getPipeName() {
        return version.getPipeName();
    }

    public String getVersion() {
        return version.getVersion().toString();
    }

}
