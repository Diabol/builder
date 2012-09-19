package controller;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import org.junit.Test;

import browser.pipelist.PipeListPage;

import play.mvc.Result;

public class PipesControllerTest {

    private final PipeListPage pipeConfigListPage = new PipeListPage();

    @Test
    public void testPipeConfigControllerListAction() {
        Result result = callAction(controllers.routes.ref.Pipes.list());

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo("text/html");
        assertThat(charset(result)).isEqualTo("utf-8");
        assertThat(contentAsString(result)).contains("id=\"pipeList\"");
        assertThat(contentAsString(result)).contains("Component-A");
        assertThat(contentAsString(result)).contains("Component-B");
    }

    @Test
    public void testPipelistRoute() {
        Result result = routeAndCall(fakeRequest(GET, pipeConfigListPage.getUri()));
        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("id=\"pipeList\"");
    }

}
