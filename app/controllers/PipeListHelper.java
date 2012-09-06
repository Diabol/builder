package controllers;

import models.config.PhaseConfig;
import models.config.PipeValidationException;
import play.api.templates.Html;

/**
 * Helper class for the pipe list view.
 */
public class PipeListHelper {
    public static Html generateMarkupForTaskTree(PhaseConfig ph) throws PipeValidationException {
        StringBuffer buf = new StringBuffer();
        //Create the nodes
        buf.append("<div id='taskHeader'>Tasks for "+ph.getName()+"</div>");
        for(int counter = 0; counter < ph.getTasks().size(); counter++){
            buf.append("<div id=task"+counter+" class='task' style='left: "+(30+150*counter)+"px; top: 50px;'><div style='padding-top: 40%;'>"+ph.getTasks().get(counter).getTaskName()+"</div></div>");
        }
        return new Html(buf.toString());
    }
}
