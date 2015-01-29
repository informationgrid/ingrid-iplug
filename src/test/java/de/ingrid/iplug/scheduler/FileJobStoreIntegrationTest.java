/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.scheduler;

import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

/**
 * FileJobStoreIntegrationTest test integration of FileJobStore into quartz.
 * 
 * <br/><br/>created on 11.10.2005
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author jz ${lastedit}
 * 
 */
public class FileJobStoreIntegrationTest extends TestCase {

    private Scheduler fScheduler;

    private JobDetail fJobDetail;

    private SchedulerFactory fSchedulerFactory;

    protected void setUp() throws Exception {
        //clean directory
        FileJobStore jobStore= new FileJobStore();
        jobStore.initialize(null,null);
        jobStore.clear();
        
        Properties properties = new Properties();
        properties.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, FileJobStore.class.getName());
        properties.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount", "5");
        this.fSchedulerFactory = new StdSchedulerFactory(properties);
        this.fScheduler = this.fSchedulerFactory.getScheduler();
        this.fScheduler.start();

        this.fJobDetail = new JobDetail("myJob", null, CountJob.class);
        this.fJobDetail.setDurability(true);
        this.fScheduler.addJob(this.fJobDetail, false);

        Trigger trigger = TriggerUtils.makeImmediateTrigger(2, 200);
        trigger.setStartTime(new Date());
        trigger.setName("myTrigger");
        trigger.setJobName(this.fJobDetail.getName());
        trigger.setJobGroup(this.fJobDetail.getGroup());
        this.fScheduler.scheduleJob(trigger);
    }

    protected void tearDown() throws Exception {
        this.fScheduler.deleteJob(this.fJobDetail.getName(), this.fJobDetail.getGroup());
        this.fScheduler.shutdown();
        CountJob.fExecutionCount = 0;
    }

    /**
     * @throws Exception
     */
    public void testSimpleCount() throws Exception {
        Thread.sleep(1000);
        assertEquals(3, CountJob.fExecutionCount);
    }

    /**
     * @throws Exception
     */
    public void testSimpleCountWithRestart() throws Exception {
        Thread.sleep(100);
        assertEquals(1, CountJob.fExecutionCount);

        // restart
        this.fScheduler.shutdown();
        this.fScheduler = this.fSchedulerFactory.getScheduler();
        this.fScheduler.start();
        Thread.sleep(1000);
        assertEquals(3, CountJob.fExecutionCount);
    }
}
