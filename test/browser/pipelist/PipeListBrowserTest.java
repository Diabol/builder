package browser.pipelist;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static play.mvc.Http.Status.OK;

import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.domain.FluentList;
import org.junit.Test;

import play.libs.WS;
import utils.PipeConfReader;
import browser.AbstractBrowserFluentTest;

public class PipeListBrowserTest extends AbstractBrowserFluentTest {

    @Page
    private PipeListPage pipeListPage;

    @Test
    public void testPipeConfigListUrl() {
        assertThat(WS.url(pipeListPage.getUrl()).get().get().getStatus()).isEqualTo(OK);
    }

    /**
     * TODO: Figure out how to do gui testing. Webdriver does not seem to handle
     * graffle api.
     */
    @Test
    public void testPipeConfigVisibleInBrowser() {
        goTo(pipeListPage);
        assertThat(pipeListPage).isAt();

        FluentList<?> pipeDivList = pipeListPage.getPipeDivList();
        assertThat(pipeDivList.size()).isEqualTo(
                PipeConfReader.getInstance().getConfiguredPipes().size());
    }

}
