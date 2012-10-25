package models.statusdata;

import static org.fest.assertions.Assertions.assertThat;
import models.StatusInterface.State;

import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

public class TaskTest {

    Task task;

    @Before
    public void prepare() {
        task = new Task("task", State.NOT_STARTED, null, null);
    }

    @Test
    public void testToObjectNodeSuccess() {
        task.startNow();
        task.finishNow(true);
        ObjectNode on = task.toObjectNode();
        assertThat(on.get("name").asText()).isEqualTo("task");
        assertThat(on.get("state").asText()).isEqualTo("SUCCESS");
        assertThat(on.get("started")).isNotNull();
        assertThat(on.get("finished")).isNotNull();
    }

    @Test
    public void testToObjectNodeFailure() {
        task.startNow();
        task.finishNow(false);
        ObjectNode on = task.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("FAILURE");
        assertThat(on.get("started")).isNotNull();
        assertThat(on.get("finished")).isNotNull();
    }

    @Test
    public void testToObjectNodeRunning() {
        task.startNow();
        ObjectNode on = task.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("RUNNING");
        assertThat(on.get("started")).isNotNull();
        assertThat(on.get("finished").asText()).isEqualTo("NA");
    }

    @Test
    public void testToObjectNodeStarted() {
        ObjectNode on = task.toObjectNode();
        assertThat(on.get("state").asText()).isEqualTo("NOT_STARTED");
        assertThat(on.get("started").asText()).isEqualTo("NA");
        assertThat(on.get("finished").asText()).isEqualTo("NA");
    }
}
