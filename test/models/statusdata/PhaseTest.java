package models.statusdata;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import models.StatusInterface.State;

import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

public class PhaseTest {

    Phase phase;
    List<Task> tasks = Collections.singletonList(new Task("task", State.NOT_STARTED, null, null));

    @Before
    public void prepare() {
        phase = new Phase("phase", State.NOT_STARTED, null, null, tasks);
    }

    @Test
    public void testToObjectNodeSuccess() {
        phase.startNow();
        phase.finishNow(true);
        ObjectNode on = phase.toObjectNode();
        assertThat(on.get("name").asText()).isEqualTo("phase");
        assertThat(on.get("state").asText()).isEqualTo("SUCCESS");
        assertThat(on.get("started")).isNotNull();
        assertThat(on.get("finished")).isNotNull();
        assertThat(on.get("tasks").size()).isEqualTo(1);
    }

    @Test
    public void testToObjectNodeFailure() {
        phase.startNow();
        phase.finishNow(false);
        ObjectNode on = phase.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("FAILURE");
        assertThat(on.get("started")).isNotNull();
        assertThat(on.get("finished")).isNotNull();
    }

    @Test
    public void testToObjectNodeRunning() {
        phase.startNow();
        ObjectNode on = phase.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("RUNNING");
        assertThat(on.get("started")).isNotNull();
        assertThat(on.get("finished").asText()).isEqualTo("NA");
    }

    @Test
    public void testToObjectNodeStarted() {
        ObjectNode on = phase.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("NOT_STARTED");
        assertThat(on.get("started").asText()).isEqualTo("NA");
        assertThat(on.get("finished").asText()).isEqualTo("NA");
    }
}
