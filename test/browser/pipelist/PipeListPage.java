package browser.pipelist;

import static browser.AbstractBrowserFluentTest.LOCALHOST_BASE_TEST_URL;
import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.withId;

import java.util.concurrent.TimeUnit;

import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.domain.FluentList;

/**
 * This is a Page Objects that is Selenium Test Helpers as outlined in:
 * https://github.com/FluentLenium/FluentLenium#page-object-pattern
 * 
 * It represents a PipeList page, and encapsulates plumbing relating to how
 * pages interact with each other and how the user interacts with the page,
 * which makes tests a lot easier to read and to maintain.
 * 
 * @author marcus
 */
public class PipeListPage extends FluentPage {

    @Override
    public String getUrl() {
        return LOCALHOST_BASE_TEST_URL + getUri();
    }

    @Override
    public void isAt() {
        await().atMost(5, TimeUnit.SECONDS).until(".canvas").hasSize(3);
        assertThat(title()).isEqualTo("Pipe List");
        assertThat(find("div", withId("pipeList")).size()).isEqualTo(1);
    }

    public FluentList<?> getPipeDivList() {
        return find("div", withId("pipeList")).find(".canvas");
    }

    public String getUri() {
        return "/" + "pipes";
    }
}
