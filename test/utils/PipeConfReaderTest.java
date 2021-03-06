package utils;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import models.config.PipeConfig;

import org.junit.Test;

/**
 * @author danielgronberg
 */
public class PipeConfReaderTest {

    @Test
    public void testConfIsReadOk() throws Exception {
        PipeConfReader reader = PipeConfReader.getInstance();
        List<PipeConfig> result = reader.getConfiguredPipes();
        assertEquals(3, result.size());
        assertThat(result.get(0).getName()).isNotEmpty();
        assertThat(result.get(0).getPhases().get(0).getName()).isNotEmpty();
    }
}
