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
import helpers.MockConfigHelper;

import java.util.Collections;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import notification.PhaseStatusChangedListener;
import notification.PipeNotificationHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import play.mvc.Result;
import test.MockitoTestBase;
import utils.PipeConfReader;
import browser.pipelist.PipeListPage;
import controllers.Pipes;

@RunWith(MockitoJUnitRunner.class)
public class PipesControllerTest extends MockitoTestBase implements PhaseStatusChangedListener {

    private final PipeListPage pipeConfigListPage = new PipeListPage();
    @Mock
    private PipeConfReader configReader;
    private final PipeConfig mockedConf = MockConfigHelper.mockConfig();

    private int phaseCompletedCount;

    @Before
    public void prepare() {
        phaseCompletedCount = 0;
        Pipes.setPipeConfigReader(configReader);
        PipeNotificationHandler.getInstance().addPhaseStatusChangedListener(this);
    }

    @After
    public void after() {
        Pipes.setPipeConfigReader(PipeConfReader.getInstance());
        PipeNotificationHandler.getInstance().removeAllPhaseListeners();
    }

    @Test
    public void testPipeConfigControllerListAction() {
        Mockito.when(configReader.getConfiguredPipes()).thenReturn(
                Collections.singletonList(mockedConf));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = callAction(controllers.routes.ref.Pipes.list());

                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("text/html");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains("id=\"pipeList\"");
                assertThat(contentAsString(result)).contains("ThePipe");
            }

        });
    }

    @Test
    public void testPipelistRoute() {
        Mockito.when(configReader.get("ThePipe")).thenReturn(MockConfigHelper.mockConfig());
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = routeAndCall(fakeRequest(GET, pipeConfigListPage.getUri()));
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentAsString(result)).contains("id=\"pipeList\"");
            }
        });
    }

    @Test
    public void testGetLatestPhasesReturnsANotStartedPipeWhenNotstarted() {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPhasesForLatestVersion("ThePipe");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                for (PhaseConfig conf : mockedConf.getPhases()) {
                    assertThat(contentAsString(result)).contains(conf.getName());
                    for (TaskConfig task : conf.getTasks()) {
                        assertThat(contentAsString(result)).contains(task.getTaskName());
                    }
                }
                assertThat(contentAsString(result).contains("NOT_STARTED"));
            }
        });
    }

    @Test
    public void testGetLatestPhasesOfASuccessFullPipe() {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                callAction(controllers.routes.ref.Pipes.start("ThePipe"));
                while (phaseCompletedCount < 3) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        assertThat(true).isFalse();
                    }
                }
                Result result = Pipes.getPhasesForLatestVersion("ThePipe");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                for (PhaseConfig conf : mockedConf.getPhases()) {
                    assertThat(contentAsString(result)).contains(conf.getName());
                    for (TaskConfig task : conf.getTasks()) {
                        assertThat(contentAsString(result)).contains(task.getTaskName());
                    }
                }
                assertThat(contentAsString(result).contains("SUCCESS"));
            }
        });
    }

    @Override
    public void recieveStatusChanged(PhaseStatus status) {
        if (status.isSuccess()) {
            phaseCompletedCount++;
        }
    }

}
