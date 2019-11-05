package com.camunda.training;

import org.apache.tomcat.jni.Time;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;

public class ProcessJUnitTest {
    @Rule
    @ClassRule
    //  public ProcessEngineRule rule = new ProcessEngineRule();
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();


    @Before
    public void setup() {
        /*if using delegate expression and delegate as a bean*/
       // Mocks.register("createTweetDelegate", new CreateTweetDelegate());
        init(rule.getProcessEngine());
    }

    @Test
    @Deployment(resources = "approveTweet.bpmn")
    public void testHappyPath() {
        //long variant

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", true);
        variables.put("content", "Exercise 4 test - MY NAME HERE" + DateTime.now());
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);

        // short variant
        // ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", withVariables("approved", true));

        assertThat(processInstance).isWaitingAt("ReviewTweetTask");
        /*long way*/
        List<Task> taskList = taskService()
                .createTaskQuery()
                .taskCandidateGroup("management")
                .processInstanceId(processInstance.getId())
                .list();

        assertThat(taskList).isNotNull();
        assertThat(taskList).hasSize(1);
        Task task = taskList.get(0);

        assertThat(task).hasCandidateGroup("management").isNotAssigned();

        Map<String, Object> approvedMap = new HashMap<String, Object>();
        approvedMap.put("approved", true);
        taskService().complete(task.getId(), approvedMap);

        /*short way*/
//        assertThat(processInstance).task().hasCandidateGroup("management").isNotAssigned();
//        complete(task(), withVariables("approved", true));

        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();
        assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);

        assertThat(processInstance).isEnded().hasPassed("TweetPublishedEndEvent");
        assertThat(processInstance).hasVariables("approved");
    }

    @Test
    @Deployment(resources = "approveTweet.bpmn")
    public void testUnHappyPath() {

        ProcessInstance processInstance = runtimeService()
                .createProcessInstanceByKey("TwitterQAProcess")
                .startBeforeActivity("TweetApprovedGateway")
                .setVariable("approved", false)
                .execute();

        assertThat(processInstance).isStarted();
        assertThat(processInstance)
                .isWaitingAt("SendRejectionNoteTask")
                .externalTask()
                .hasTopicName("notification");
        complete(externalTask());

        //execute(job());
        assertThat(processInstance).isEnded().hasPassed("TweetRejectedEndEvent");

    }

    @Test
    @Deployment(resources = "approveTweet.bpmn")
    public void testMessage(){
        ProcessInstance processInstance = runtimeService()
                .createMessageCorrelation("superuserTweet")
                .setVariable("content", "My Exercise 11 Tweet r - " + System.currentTimeMillis())
                .correlateWithResult()
                .getProcessInstance();

        assertThat(processInstance).isStarted();
        // get the job
        List<Job> jobList = jobQuery()
                .processInstanceId(processInstance.getId())
                .list();

        // execute the job
        assertThat(jobList).hasSize(1);
        Job job = jobList.get(0);
        execute(job);

        assertThat(processInstance).isEnded();

//        runtimeService()
//                .createMessageCorrelation("tweetWithdrawn")
//                .processInstanceId(processInstance.getId())
//                .correlateWithResult();

    }

}
