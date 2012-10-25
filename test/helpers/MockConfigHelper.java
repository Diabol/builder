package helpers;

import java.util.ArrayList;
import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;

/**
 * The purpose of this class is to provide convenience methods for mocking
 * configuration data.
 * 
 * @author danielgronberg
 * 
 */
public class MockConfigHelper {
    public static PipeConfig mockConfig() {
        PipeConfig config = new PipeConfig("ThePipe");
        List<PhaseConfig> phaseList = new ArrayList<PhaseConfig>();
        phaseList.add(creatFirstPhase());
        phaseList.add(createSecondPhase());
        phaseList.add(createThirdPhase());
        config.setPhases(phaseList);
        return config;
    }

    private static PhaseConfig createThirdPhase() {
        PhaseConfig thirdPhase = new PhaseConfig("ThirdPhase");
        TaskConfig task1 = new TaskConfig("Task1", "sleep 1", true);
        TaskConfig task2 = new TaskConfig("Task2", "sleep 1", true);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(task2.getTaskName());
        task1.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(task1);
        taskList.add(task2);
        thirdPhase.setTasks(taskList);
        return thirdPhase;
    }

    private static PhaseConfig createSecondPhase() {
        PhaseConfig secondPhase = new PhaseConfig("SecondPhase");
        TaskConfig automaticTask = new TaskConfig("AutomaticTask", "sleep 1", true);
        TaskConfig nonFailing = new TaskConfig("NonFailing", "boguscmd", true);
        nonFailing.setIsBlocking(false);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(nonFailing.getTaskName());
        automaticTask.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(automaticTask);
        taskList.add(nonFailing);
        secondPhase.setTasks(taskList);
        return secondPhase;
    }

    private static PhaseConfig creatFirstPhase() {
        PhaseConfig firstPhase = new PhaseConfig("FirstPhase");
        TaskConfig firstTask = new TaskConfig("FirstTask", "sleep 1", true);
        TaskConfig parallell1 = new TaskConfig("Parallell1", "sleep 1", true);
        TaskConfig parallell2 = new TaskConfig("Parallell2", "sleep 1", true);
        TaskConfig nonFailing = new TaskConfig("NonFailing", "sleep 2", true);
        nonFailing.setIsBlocking(false);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(parallell1.getTaskName());
        firstTaskTriggers.add(parallell2.getTaskName());
        firstTaskTriggers.add(nonFailing.getTaskName());
        firstTask.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(firstTask);
        taskList.add(parallell2);
        taskList.add(parallell1);
        taskList.add(nonFailing);
        firstPhase.setTasks(taskList);
        return firstPhase;
    }
}
