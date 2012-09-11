package executor;

import models.NotificationHandler;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-09-10
 * Time: 14:19
 * To change this template use File | Settings | File Templates.
 */
public class SimplePipeExecutor implements Runnable {
    private PipeConfig pipe;

    public SimplePipeExecutor(PipeConfig pipe) {
        this.pipe = pipe;
    }

    @Override
    public void run() {
        for (PhaseConfig phase : pipe.getPhases()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            ObjectNode json = Json.newObject();
            json.put("pipeName", pipe.getName());
            json.put("phaseName", phase.getName());
            json.put("status", "SUCCESS");
            NotificationHandler.defaultHandler.tell(new NotificationHandler.SendJson(json));
        }
    }
}
