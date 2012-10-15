package controllers;

import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import play.api.templates.Html;

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
        buf.append("<div id='name'>" + phase.name + "</div><div id='version'>Version: "
                + pipe.version + "</div><div id='state'>State: " + phase.state + "</div>");
        buf.append("<div id='tasks' style='top:5px;'>");
        for (int taskCount = 0; taskCount < phase.tasks.size(); taskCount++) {
            Task task = phase.tasks.get(taskCount);
            buf.append("<div id='" + pipe.name + phase.name + task.name + "' class='task "
                    + task.state + " " + pipe.name + "' style='left: " + (10 + taskCount * 30)
                    + "px;'></div>");
        }
        buf.append("</div>");
        return new Html(buf.toString());
    }
}
