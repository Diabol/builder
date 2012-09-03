package models.config;

import static org.fest.assertions.Fail.fail;

import java.util.Collections;

import org.junit.Test;

public class PipeConfigTest {

    @SuppressWarnings("unchecked")
    @Test
    public void pipeWithNoPhasesThrowsValidationExceptionWhenCreated() {
        try {
            PipeConfig target = new PipeConfig();
            target.setName("");
            target.setPhases(Collections.EMPTY_LIST);
            target.validate();
            fail();
        } catch (PipeValidationException e) {
            //Expected
        }
    }
}
