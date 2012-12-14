import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import common.Phase;
import common.Pipe;

/**
 * 
 * @author danielgronberg
 */
public class PipeListTest extends PipeItBaseTest {

    @Test
    public void testPipeListNamesVisible() throws Exception {

        final HtmlPage page = webClient.getPage(host + "/pipes?disableWS");
        webClient.waitForBackgroundJavaScript(5000);
        Assert.assertEquals("All pipelines with latest run", page.getTitleText());

        final String pageAsText = page.asText();

        assertAllNamesOfPipesAndPhases(pageAsText);

        webClient.closeAllWindows();
    }

    private void assertAllNamesOfPipesAndPhases(String pageAsText) throws Exception {
        List<Pipe> pipes = getLatestPipes();
        for (Pipe pipe : pipes) {
            Assert.assertTrue(pageAsText.contains(pipe.getName()));
            for (Phase phase : pipe.getPhases()) {
                Assert.assertTrue(pageAsText.contains(phase.getName()));
            }
        }

    }
}
