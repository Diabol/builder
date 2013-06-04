package browser.pipelist;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static play.mvc.Http.Status.OK;
import helpers.MockConfigHelper;

import java.util.Collections;

import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.domain.FluentList;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import play.libs.WS;
import utils.PipeConfReader;
import browser.AbstractBrowserFluentTest;
import controllers.Pipes;

public class PipeListBrowserTest extends AbstractBrowserFluentTest {

    @Page
    private PipeListPage pipeListPage;

    private final PipeConfReader confReader = Mockito.mock(PipeConfReader.class);

    @Before
    public void prepare() {
        Mockito.when(confReader.getConfiguredPipes()).thenReturn(
                Collections.singletonList(MockConfigHelper.mockConfig()));
        Pipes.setPipeConfigReader(confReader);
    }

    @After
    public void after() {
        //super.after();
        Pipes.setPipeConfigReader(PipeConfReader.getInstance());
    }

    @Test
    public void testPipeConfigListUrl() {
        assertThat(WS.url(pipeListPage.getUrl()).get().get().getStatus()).isEqualTo(OK);
    }

    /**
     * TODO: Figure out how to do gui testing. This test does not load
     * javascript and hence doesn't test the view.
     */
    @Ignore
    @Test
    public void testPipeConfigVisibleInBrowser() {
        goTo(pipeListPage);
        assertThat(pipeListPage).isAt();

        FluentList<?> pipeDivList = pipeListPage.getPipeDivList();
        assertThat(pipeDivList.size()).isEqualTo(1);
    }

}
