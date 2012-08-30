package acceptance;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import org.junit.Ignore;
import org.junit.Test;

import play.libs.F.Callback;
import play.libs.WS;
import play.mvc.Content;
import play.mvc.Result;
import play.test.TestBrowser;

/**
 * This is an example of testing using the stuff created by default by play at project
 * creation time.
 * 
 * @author marcus
 */
public class ApplicationAcceptanceTest {

    private static final String MESSAGE = "Your new application is ready.";

    /** Test TEMPLATE */
    @Test
    public void renderTemplate() {
        Content html = views.html.index.render("Coco");

        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Coco");
    }

    /** Test CONTROLLER ACTION */
    @Test
    public void callApplicationControllerIndexAction() {
        Result result = callAction(
                controllers.routes.ref.Application.index()
                );

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo("text/html");
        assertThat(charset(result)).isEqualTo("utf-8");
        assertThat(contentAsString(result)).contains(MESSAGE);
    }

    /** Test the ROUTE */
    @Test
    public void badRoute() {
        Result result = routeAndCall(fakeRequest(GET, "/routeNotMapped"));
        assertThat(result).isNull();
    }

    /** Test the ROUTE */
    @Test
    public void testApplicationRoute() {
        Result result = routeAndCall(fakeRequest(GET, "/"));
        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentAsString(result)).contains(MESSAGE);
    }

    /** Test with real HTTP server */
    @Test
    public void testInServer() {
        running(testServer(3366), new Runnable() {
            @Override
            public void run() {
                assertThat(WS.url("http://localhost:3366").get().get().getStatus()).isEqualTo(OK);
            }
        });
    }

    /** Test with a WEBDRIVER */
    /**
     * Ignored due to that webdriver does not seem to handle graffle api.
     * TODO: Figure out how to make it work
     */
    @Ignore
    @Test
    public void runInBrowser() {
        //System.setProperty("webdriver.chrome.driver", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");

        running(testServer(3366), HTMLUNIT, new Callback<TestBrowser>() {
            @Override
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3366/");

                // TODO: Detta borde hända tycker marcus:
                //assertThat(browser.find("article").findFirst("h1").getText()).isEqualTo("Welcome to Play 2.0");
                // men vi får bara:
                assertThat(browser.findFirst("h1").getText()).isEqualTo(MESSAGE);

                // Now you click etc as below, but please use the Page Object pattern.
                // NB: $ is an alias to find.
                // browser.$("a").click();
                // assertThat(browser.url()).isEqualTo("http://localhost:3333/Coco");
                // assertThat(browser.$("#title", 0).getText()).isEqualTo("Hello Coco");
            }
        });
    }
}
