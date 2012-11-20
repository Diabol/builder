package controller;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.NOT_FOUND;
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
import java.util.HashMap;
import java.util.Map;

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

import play.Logger;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import test.MockitoTestBase;
import utils.DataNotFoundException;
import utils.PipeConfReader;
import browser.pipelist.PipeListPage;
import controllers.GitHub;
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
    public void testPipelistRoute() throws Exception {
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
    public void testGetLatestPhasesReturnsANotStartedPipeWhenNotstarted() throws Exception {
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
    public void testGetLatestPhasesOfASuccessFullPipe() throws Exception {
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

    @Test
    public void testGetLatestPhasesReturnsNotFoundWhenNotConfigured() throws Exception {
        Mockito.when(configReader.get("NotConfigured")).thenThrow(new DataNotFoundException(""));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPhasesForLatestVersion("NotConfigured");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testPhasesForVersionReturnsNotFoundWhenNotStarted() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPhases("ThePipe", "version");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testPhasesForVersionReturnsNotFoundWhenNotConfigured() throws Exception {
        Mockito.when(configReader.get("NotConfigured")).thenThrow(new DataNotFoundException(""));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPhases("NotConfigured", "version");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testGetPhasesOfAExistingVersion() throws Exception {
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
                Result result = Pipes.getPhases("ThePipe", "1.1");
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

    @Test
    public void testGetLatestVersionOfPipesReturnsNotStartedtWhenNotStarted() {
        Mockito.when(configReader.getConfiguredPipes()).thenReturn(
                Collections.singletonList(mockedConf));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getLatestPipes();
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains(mockedConf.getName());
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
    public void testGetLatestVersionOfPipesReturnsLatestStarted() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        Mockito.when(configReader.getConfiguredPipes()).thenReturn(
                Collections.singletonList(mockedConf));
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
                Result result = Pipes.getLatestPipes();
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains(mockedConf.getName());
                for (PhaseConfig conf : mockedConf.getPhases()) {
                    assertThat(contentAsString(result)).contains(conf.getName());
                    for (TaskConfig task : conf.getTasks()) {
                        assertThat(contentAsString(result)).contains(task.getTaskName());
                    }
                }
                assertThat(contentAsString(result).contains("version: 1.1"));
                assertThat(contentAsString(result).contains("NOT_STARTED"));
            }
        });
    }

    @Test
    public void testGetLatestVersionOfPipeReturnsNotStartedWhenNotStarted() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getLatestPipe("ThePipe");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains(mockedConf.getName());
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
    public void testGetLatestVersionOfPipeReturnsLatestStarted() throws Exception {
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
                Result result = Pipes.getLatestPipe("ThePipe");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains(mockedConf.getName());
                for (PhaseConfig conf : mockedConf.getPhases()) {
                    assertThat(contentAsString(result)).contains(conf.getName());
                    for (TaskConfig task : conf.getTasks()) {
                        assertThat(contentAsString(result)).contains(task.getTaskName());
                    }
                }
                assertThat(contentAsString(result).contains("version: 1.1"));
                assertThat(contentAsString(result).contains("NOT_STARTED"));
            }
        });
    }

    @Test
    public void testGetPipe() throws Exception {
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
                Result result = Pipes.getPipe("ThePipe", "1.1");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains(mockedConf.getName());
                for (PhaseConfig conf : mockedConf.getPhases()) {
                    assertThat(contentAsString(result)).contains(conf.getName());
                    for (TaskConfig task : conf.getTasks()) {
                        assertThat(contentAsString(result)).contains(task.getTaskName());
                    }
                }
                assertThat(contentAsString(result).contains("version: 1.1"));
                assertThat(contentAsString(result).contains("NOT_STARTED"));
            }
        });
    }

    @Test
    public void testGetLatestVersionOfPipeReturnsNotFoundWhenNotConfigured() throws Exception {
        Mockito.when(configReader.get("NotConfigured")).thenThrow(new DataNotFoundException(""));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getLatestPipe("NotConfigured");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testGetPipeReturnsNotFoundWhenVersionNotPersisted() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPipe("ThePipe", "version");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testGetPipeReturnsNotFoundWhenNotConfigured() throws Exception {
        Mockito.when(configReader.get("NotConfigured")).thenThrow(new DataNotFoundException(""));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPipe("NotConfigured", "version");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testGetPipeVersions() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                startPipeWithGithubVCInfo("vc1", "commit1");
                startPipeWithGithubVCInfo("vc2", "commit2");
                while (phaseCompletedCount < 6) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        assertThat(true).isFalse();
                    }
                }
                Result result = Pipes.getPipeVersions("ThePipe");
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result).contains(
                        "\"1\":{\"versionControlId\":\"vc1\",\"versionControlText\":\"commit1\"}"));
                assertThat(contentAsString(result).contains(
                        "\"2\":{\"versionControlId\":\"vc2\",\"versionControlText\":\"commit2\"}"));
            }

            private void startPipeWithGithubVCInfo(String commitId, String commitText) {
                // Mock the request object where the json payload from
                // Github is read.
                Map<String, String[]> json = new HashMap<String, String[]>();
                String jsonString = "{\"commits\":[{\"message\":\"" + commitText + "\",\"id\":\""
                        + commitId + "\"}]}";
                String[] array = { jsonString };
                json.put("payload", array);
                Request requestMock = Mockito.mock(Request.class);
                RequestBody body = Mockito.mock(RequestBody.class);
                Mockito.when(body.asFormUrlEncoded()).thenReturn(json);
                Mockito.when(requestMock.body()).thenReturn(body);
                Context.current.set(new Context(requestMock, new HashMap<String, String>(),
                        new HashMap<String, String>()));
                GitHub.start("ThePipe");
            }
        });
    }

    @Test
    public void testGetPipeVersionsReturnsNotFoundWhenNotConfigured() throws Exception {
        Mockito.when(configReader.get("NotConfigured")).thenThrow(new DataNotFoundException(""));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPipeVersions("NotConfigured");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Test
    public void testGetPipeVersionsReturnsNotRun() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.getPipeVersions("ThePipe");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

    @Override
    public void recieveStatusChanged(PhaseStatus status) {
        Logger.error("status " + status.getState() + " received for " + status.getPhaseName());
        if (status.isSuccess()) {
            phaseCompletedCount++;
        }
    }

    @Test
    public void testIncrementMajor() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenReturn(mockedConf);
        Mockito.when(configReader.getConfiguredPipes()).thenReturn(
                Collections.singletonList(mockedConf));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                callAction(controllers.routes.ref.Pipes.incrementMajor("ThePipe"));
                while (phaseCompletedCount < 3) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        assertThat(true).isFalse();
                    }
                }
                Result result = Pipes.getLatestPipes();
                assertThat(status(result)).isEqualTo(OK);
                assertThat(contentType(result)).isEqualTo("application/json");
                assertThat(charset(result)).isEqualTo("utf-8");
                assertThat(contentAsString(result)).contains(mockedConf.getName());
                for (PhaseConfig conf : mockedConf.getPhases()) {
                    assertThat(contentAsString(result)).contains(conf.getName());
                    for (TaskConfig task : conf.getTasks()) {
                        assertThat(contentAsString(result)).contains(task.getTaskName());
                    }
                }
                assertThat(contentAsString(result).contains("version: 2.1"));
            }
        });
    }

    @Test
    public void testIncrementMajorReturnsNotFoundWhenNotConfigured() throws Exception {
        Mockito.when(configReader.get("ThePipe")).thenThrow(new DataNotFoundException("Message"));
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Result result = Pipes.incrementMajor("ThePipe");
                assertThat(status(result)).isEqualTo(NOT_FOUND);
            }
        });
    }

}
