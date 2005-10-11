/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.scheduler;

import junit.framework.TestCase;

import org.quartz.CronTrigger;

/**
 * SchedulingServiceTest
 * 
 * <br/><br/>created on 11.10.2005
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author jz ${lastedit}
 * 
 */
public class SchedulingServiceTest extends TestCase {

    private String fJobName = "jobName";

    protected void setUp() throws Exception {
        //clean directory
        FileJobStore jobStore= new FileJobStore();
        jobStore.initialize(null,null);
        jobStore.clear();
        
        SchedulingService.init();
    }

    protected void tearDown() throws Exception {
        SchedulingService.removeJob(this.fJobName, null);
        SchedulingService.shutdown();
        CountJob.fExecutionCount = 0;
    }

    /**
     * @throws Exception
     */
    public void testScheduleCronJob1() throws Exception {
        SchedulingService.scheduleCronJob(this.fJobName, null, CountJob.class, null, "0/1 * * * * ? *");
        Thread.sleep(500);
        assertTrue(CountJob.fExecutionCount > 0);
    }

    /**
     * @throws Exception
     */
    public void testScheduleCronJob2() throws Exception {
        SchedulingService.scheduleCronJob(this.fJobName, null, CountJob.class, null, "0/1", null, null, null, null,
                "?", null);
        Thread.sleep(500);
        assertTrue(CountJob.fExecutionCount > 0);
    }

    /**
     * @throws Throwable
     * 
     */
    public void testGetCronExpression() throws Throwable {
        String cronExpression = SchedulingService.getCronExpression("0", "0/5", null, null, null, "?", null);
        assertEquals("0 0/5 * * * ? *", cronExpression);
        new CronTrigger().setCronExpression(cronExpression);
        cronExpression = SchedulingService.getCronExpression("0", "30", "10-13", "?", null, "WED,FRI", null);
        assertEquals("0 30 10-13 ? * WED,FRI *", cronExpression);
        new CronTrigger().setCronExpression(cronExpression);
    }
}
