package models.statusdata;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.StatusInterface.State;

import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

public class PipeTest {

    Pipe pipe;
    List<Phase> phases = Collections.singletonList(new Phase("phase", State.NOT_STARTED, null,
            null, new ArrayList<Task>()));

    @Before
    public void prepare() {
        pipe = new Pipe("pipe", "version", State.NOT_STARTED, null, null, phases,
                VersionControlInfo.createVCInfoNotAvailable());
    }

    @Test
    public void testToObjectNodeSuccess() {
        pipe.startNow();
        pipe.finishNow(true);
        ObjectNode on = pipe.toObjectNode();
        assertThat(on.get("name").asText()).isEqualTo("pipe");
        assertThat(on.get("state").asText()).isEqualTo("SUCCESS");
        assertThat(on.get("started").asLong()).isNotNull();
        assertThat(on.get("finished").asLong()).isNotNull();
        assertThat(on.get("version").asText()).isEqualTo("version");
        assertThat(on.get("phases").size()).isEqualTo(1);
    }

    @Test
    public void testToObjectNodeFailure() {
        pipe.startNow();
        pipe.finishNow(false);
        ObjectNode on = pipe.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("FAILURE");
        assertThat(on.get("started").asLong()).isNotNull();
        assertThat(on.get("finished")).isNotNull();
    }

    @Test
    public void testToObjectNodeRunning() {
        pipe.startNow();
        ObjectNode on = pipe.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("RUNNING");
        assertThat(on.get("started").asLong()).isNotNull();
        assertThat(on.get("finished")).isNull();
    }

    @Test
    public void testToObjectNodeStarted() {
        ObjectNode on = pipe.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("NOT_STARTED");
        assertThat(on.get("started")).isNull();
        assertThat(on.get("finished")).isNull();
    }
}
