package acceptance.config.pipe;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static play.mvc.Http.Status.OK;

import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.domain.FluentList;
import org.junit.Test;

import play.libs.WS;
import acceptance.AbstractBrowserFluentTest;

public class PipeConfigListBrowserTest extends AbstractBrowserFluentTest {

    @Page
    public PipeConfigListPage pipeConfigListPage;

    @Test
    public void testPipeConfigListUrl() {
        assertThat(WS.url(pipeConfigListPage.getUrl()).get().get().getStatus()).isEqualTo(OK);
    }

    /**
     * TODO: Figure out how to do gui testing. Webdriver does not seem to handle graffle api.
     */
    @Test
    public void testPipeConfigVisibleInBrowser() {
        goTo(pipeConfigListPage);
        assertThat(pipeConfigListPage).isAt();

        FluentList<?> pipeDivList = pipeConfigListPage.getPipeDivList();
        assertThat(pipeDivList.size()).isEqualTo(2);
    }

}
