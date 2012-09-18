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
    PipeConfReader reader = PipeConfReader.getInstance();

    @Test
    public void testConfIsReadOk() throws Exception{
        List<PipeConfig> result = reader.getConfiguredPipes();
        assertEquals(2, result.size());
        assertThat(result.get(0).getName()).isNotEmpty();
        assertThat(result.get(0).getPhases().get(0).getName()).isNotEmpty();
    }
}

