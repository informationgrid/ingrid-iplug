/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.scheduler;

import java.io.File;

import junit.framework.TestCase;

import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

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

    private File fTestFolder;

    private SchedulingService fSchedulingService;

    protected void setUp() throws Exception {
        // clean directory
        this.fTestFolder = new File("" + System.currentTimeMillis() + "");
        this.fTestFolder.mkdirs();
        this.fSchedulingService = new SchedulingService(this.fTestFolder);
        FileJobStore jobStore = new FileJobStore();
        jobStore.initialize(null, null);
        jobStore.clear();
    }

    protected void tearDown() throws Exception {
        try {
            this.fSchedulingService.removeJob(this.fJobName, null);
        } catch (SchedulerException e) {
            // ignore
        }
        this.fSchedulingService.shutdown();
        File[] files = this.fTestFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        this.fTestFolder.delete();
        CountJob.fExecutionCount = 0;

    }

    /**
     * @throws Exception
     */
    public void testScheduleCronJob1() throws Exception {
        this.fSchedulingService.scheduleCronJob(this.fJobName, null, CountJob.class, null, "0/1 * * * * ? *");
        Thread.sleep(500);
        assertTrue(CountJob.fExecutionCount > 0);
    }

    /**
     * @throws Exception
     */
    public void testScheduleCronJob2() throws Exception {
        this.fSchedulingService.scheduleCronJob(this.fJobName, null, CountJob.class, null, "0/1", null, null, null,
                null, "?", null);
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

    /**
     * @throws Exception
     */
    public void testSchedulerRestart() throws Exception {
        this.fSchedulingService.scheduleCronJob(this.fJobName, null, CountJob.class, null, "0/1 * * * * ? *");
        Thread.sleep(500);
        assertTrue(CountJob.fExecutionCount > 0);

        // restart
        this.fSchedulingService.shutdown();
        CountJob.fExecutionCount = 0;
        this.fSchedulingService = new SchedulingService(this.fTestFolder);
        Thread.sleep(1000);
        assertTrue(CountJob.fExecutionCount > 0);
    }
}
