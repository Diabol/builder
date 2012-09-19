package browser;

import static play.test.Helpers.*;

import org.fluentlenium.adapter.FluentTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import play.test.TestServer;

public abstract class AbstractBrowserFluentTest extends FluentTest {

    protected static final String LOCALHOST = "http://localhost";
    /** If we start a test server in a test we use this port. 9000 is default when running play locally in console. */
    protected static final int TEST_SERVER_PORT = 3366;

    public static final String LOCALHOST_BASE_TEST_URL = LOCALHOST + ":" + TEST_SERVER_PORT;

    /** Marcus thinks it should work with a shared test server. If not: refactor! ;) */
    private static TestServer testServer;

    @BeforeClass
    public static void setUp() {
        testServer = testServer(TEST_SERVER_PORT, fakeApplication());
        start(testServer);
    }

    @AfterClass
    public static void tearDown() {
        stop(testServer);
    }

    @Override
    public WebDriver getDefaultDriver() {
        return new HtmlUnitDriver();
    }

}