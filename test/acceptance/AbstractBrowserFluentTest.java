package acceptance;

import org.fluentlenium.adapter.FluentTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public abstract class AbstractBrowserFluentTest extends FluentTest {

    protected static final String LOCALHOST = "http://localhost";
    /** If we start a test server in a test we use this port*/
    protected static final int TEST_SERVER_PORT = 3366;
    /**  This is the default port */
    protected static final int PORT = 9000;

    public static final String LOCALHOST_BASE_URL = LOCALHOST + ":" + PORT + "/";

    @Override
    public WebDriver getDefaultDriver() {
        return new HtmlUnitDriver();
    }

}