package acceptance.pipe;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.domain.FluentList;
import org.junit.Test;

import play.libs.WS;
import acceptance.AbstractBrowserFluentTest;

public class PipeListBrowserTest extends AbstractBrowserFluentTest {

    @Page
    public PipeListPage pipeListPage;

    @Test
    public void testPipeListUrl() {
        running(testServer(TEST_SERVER_PORT), new Runnable() {
            @Override
            public void run() {
                assertThat(WS.url(LOCALHOST + ":" + TEST_SERVER_PORT).get().get().getStatus()).isEqualTo(OK);
            }
        });
    }

    @Test
    public void testPipeIsVivibleInBrowser() {
        goTo(pipeListPage);
        assertThat(pipeListPage).isAt();

        FluentList<?> pipeDivList = pipeListPage.getPipeDivList();
        assertThat(pipeDivList.size()).isEqualTo(2);

    }
}
