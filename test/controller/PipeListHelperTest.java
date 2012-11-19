package controller;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;

import controllers.PipeListHelper;

public class PipeListHelperTest {

    @Test
    public void testFormatDateToday() {
        Date now = new Date();
        String expected = DateFormatUtils.format(now, "HH:mm");
        String formatted = PipeListHelper.formatDate(now);
        Assert.assertEquals(expected, formatted);
    }

    @Test
    public void testFormatDateSameWeek() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK) - 1);
            String expected = DateFormatUtils.format(cal, "E HH:mm");
            String formatted = PipeListHelper.formatDate(cal.getTime());
            Assert.assertEquals(expected, formatted);
        }
    }

    @Test
    public void testFormatDatePreviousMonthSameYear() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if (cal.get(Calendar.MONTH) > 0) {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
            String expected = DateFormatUtils.format(cal, "MM/dd HH:mm");
            String formatted = PipeListHelper.formatDate(cal.getTime());
            Assert.assertEquals(expected, formatted);
        }
    }

    @Test
    public void testFormatDateSameMonthDifferentWeek() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if (cal.get(Calendar.WEEK_OF_MONTH) != 1) {
            cal.set(Calendar.WEEK_OF_MONTH, cal.get(Calendar.WEEK_OF_MONTH) - 1);
            String expected = DateFormatUtils.format(cal, "MM/dd HH:mm");
            String formatted = PipeListHelper.formatDate(cal.getTime());
            Assert.assertEquals(expected, formatted);
        }
    }

    @Test
    public void testFormatDatePreviousYear() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
        String expected = DateFormatUtils.format(cal, "yyyy/MM/dd HH:mm");
        String formatted = PipeListHelper.formatDate(cal.getTime());
        Assert.assertEquals(expected, formatted);
    }
}
