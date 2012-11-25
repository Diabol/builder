package models.config;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import org.junit.Before;
import org.junit.Test;

public class EnvironmentConfigTest {
    
    private final EnvironmentConfig envConfOK = new EnvironmentConfig();
    private final EnvironmentConfig envConfNoName = new EnvironmentConfig();
    
    @Before
    public void setUp() {
        envConfOK.setName("test env");
    }

    @Test
    public void testValidateNoName() throws Exception {
        shouldThrowPipeValidationException(envConfNoName);
    }

    @Test
    public void testValidateOK() throws Exception {
        envConfOK.validate();
    }
    
    private static void shouldThrowPipeValidationException(EnvironmentConfig ec) {
        try {
            ec.validate();
            fail("EnvironmentConfig: " + ec + " is not valid");
        } catch (PipeValidationException e) {
            assertThat(e.getMessage()).contains("EnvironmentConfig");
        }
    }
}
