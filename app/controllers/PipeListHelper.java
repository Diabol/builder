package controllers;

import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import org.apache.commons.lang.time.DateFormatUtils;
import play.api.templates.Html;
import java.text.DateFormat;

/**
 * Helper class for the pipe list view.
 */
public class PipeListHelper {
    public static Html generateMarkupForTaskTree(Phase ph) {
        StringBuffer buf = new StringBuffer();
        // Create the nodes
        buf.append("<div id='taskHeader'>Tasks for " + ph.name + "</div>");
        for (int counter = 0; counter < ph.tasks.size(); counter++) {
            buf.append("<div id='" + ph.tasks.get(counter).name + "' class='task' style='left: "
                    + (30 + 150 * counter) + "px; top: 50px;'><div style='padding-top: 40%;'>"
                    + ph.tasks.get(counter).name + "</div></div>");
        }
        return new Html(buf.toString());
    }

    public static Html generateMarkupForPhase(Pipe pipe, Phase phase) {
        StringBuffer buf = new StringBuffer();
        // Create the nodes
        buf.append("<h3>" + phase.name + "</h3>");
        buf.append("<hr/>");
        String started = (phase.started != null) ? DateFormatUtils.format(phase.started,"MMM d, HH:mm:ss") : "Not yet started";
        String finished = (phase.finished != null) ? DateFormatUtils.format(phase.finished,"MMM d HH:mm:ss") : "Not yet finished";
        buf.append("<div id='started'><label>Started:</label><span> " + started + "</span></div>");
        buf.append("<div id='started'><label>Finished:</label><span> " + finished + "</span></div>");
        buf.append("<div id='tasks' >");
        for (int taskCount = 0; taskCount < phase.tasks.size(); taskCount++) {
            Task task = phase.tasks.get(taskCount);
            buf.append("<div id='" + pipe.name + phase.name + task.name + "' class='task "
                    + task.state + " " + pipe.name + "' style='left: " + (5 + taskCount * 30)
                    + "px;'></div>");
        }
        buf.append("</div>");
        return new Html(buf.toString());
    }
}
