package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

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
        buf.append("<h3>" + phase.name + "</h3>");
        buf.append("<hr/>");
        String started = (phase.started != null) ? formatDate(phase.started) : "Not yet started";
        String finished = (phase.finished != null) ? formatDate(phase.finished)
                : "Not yet finished";
        buf.append("<div id='started'><label>Started:</label><span> " + started + "</span></div>");
        buf.append("<div id='finished'><label>Finished:</label><span> " + finished
                + "</span></div>");
        buf.append("<div id='tasks' >");
        for (int taskCount = 0; taskCount < phase.tasks.size(); taskCount++) {
            Task task = phase.tasks.get(taskCount);
            buf.append("<div id='" + pipe.name + phase.name + task.name + "' class='task "
                    + task.state + " " + pipe.name + "' onClick=\"getTaskDetails('" + pipe.name
                    + ":" + phase.name + ":" + task.name + "')\" style='left: "
                    + (5 + taskCount * 30) + "px;'><div class='taskInfo'><label>" + task.name
                    + "</label></div></div>");
        }
        buf.append("</div>");
        return new Html(buf.toString());
    }

    public static String formatDate(Date dateToFormat) {
        String format = "";
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        Calendar calToFormat = Calendar.getInstance();
        calToFormat.setTime(dateToFormat);
        if (DateUtils.isSameDay(now, calToFormat)) {
            // Same day. Eg. 14:56
            format = "HH:mm";
        } else if (now.get(Calendar.YEAR) != calToFormat.get(Calendar.YEAR)) {
            // Different year. Eg 2012/03/30 14:56
            format = "yyyy/MM/dd HH:mm";
        } else if (now.get(Calendar.WEEK_OF_YEAR) == calToFormat.get(Calendar.WEEK_OF_YEAR)) {
            // Same year and same week. Eg Mon 14:56
            format = "E HH:mm";
        } else {
            // Same year but different week. Eg 03/30 14:56
            format = "MM/dd HH:mm";
        }
        return DateFormatUtils.format(dateToFormat, format);
    }

    public static String formatDuration(long millis) {
        String result;
        if (millis < 1000) {
            result = String.format("%dms", millis);
        } else if (millis < 60 * 1000) {
            result = String.format("%ds", TimeUnit.MILLISECONDS.toSeconds(millis));
        } else if (millis < 60 * 60 * 1000) {
            result = String.format(
                    "%dm %ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        } else {
            result = String.format(
                    "%dh %dm",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
        }
        return result;
    }
}
