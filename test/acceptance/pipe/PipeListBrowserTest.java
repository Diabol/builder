package acceptance.pipe;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static play.mvc.Http.Status.OK;

import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.domain.FluentList;
import org.junit.Ignore;
import org.junit.Test;

import play.libs.WS;
import acceptance.AbstractBrowserFluentTest;

public class PipeListBrowserTest extends AbstractBrowserFluentTest {

    @Page
    public PipeListPage pipeListPage;

    @Test
    public void testPipeListUrl() {
        assertThat(WS.url(LOCALHOST + ":" + TEST_SERVER_PORT).get().get().getStatus()).isEqualTo(OK);
    }

    /**
     * TODO: Figure out how to do gui testing. Webdriver does not seem to handle graffle api.
     */
    @Test
    public void testPipeIsVivibleInBrowser() {
        goTo(pipeListPage);
        assertThat(pipeListPage).isAt();

        FluentList<?> pipeDivList = pipeListPage.getPipeDivList();
        assertThat(pipeDivList.size()).isEqualTo(2);
    }

}
