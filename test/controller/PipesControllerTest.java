package controller;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.callAction;
import static play.test.Helpers.charset;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.routeAndCall;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import org.junit.Test;

import play.mvc.Result;
import browser.pipelist.PipeListPage;

public class PipesControllerTest {

    private final PipeListPage pipeConfigListPage = new PipeListPage();

    @Test
    public void testPipeConfigControllerListAction() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = callAction(controllers.routes.ref.Pipes.list());

                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains("id=\"pipeList\"");
                assertThat(contentAsString(result)).contains("Component-A");
                assertThat(contentAsString(result)).contains("Component-B");
            }

        });
    }

    @Test
    public void testPipelistRoute() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = routeAndCall(fakeRequest(GET, pipeConfigListPage.getUri()));
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentAsString(result)).contains("id=\"pipeList\"");
            }
        });
    }

}
