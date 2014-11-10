/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;

/**
 * FileJobStoreTest
 * 
 * <br/><br/>created on 05.10.2005
 * 
 * @version $Revision: $
 * @author jz
 * @author $ Author: jz ${lastedit}
 * 
 */
public class FileJobStoreTest extends TestCase {

    private JobStore fJobStore;

    private JobDetail fJobDetail;

    private SampleSignaler fSignaler;

    protected void setUp() throws Exception {
        this.fJobStore = new FileJobStore();

        // for compliance-test with RAMJobStore comment line below in
        // this.fJobStore = new RAMJobStore();

        this.fSignaler = new SampleSignaler();
        this.fJobStore.initialize(null, this.fSignaler);
        if (this.fJobStore instanceof FileJobStore) {
            ((FileJobStore) this.fJobStore).clear();
        }

        this.fJobDetail = new JobDetail("job1", "jobGroup1", SampleJob.class);
        this.fJobDetail.setDurability(true);
        this.fJobStore.storeJob(null, this.fJobDetail, false);
    }

    /**
     * @throws Exception
     */
    public void testStore_RemoveJob() throws Exception {
        checkJob();

        // store 2nd time
        try {
            this.fJobStore.storeJob(null, this.fJobDetail, false);
            fail();
        } catch (ObjectAlreadyExistsException e) {
            //
        }

        // store 2nd time with overwrite option
        this.fJobStore.storeJob(null, this.fJobDetail, true);
        checkJob();
        
         JobDetail jobDetail = new JobDetail("job1", "jobGroup1", SampleJob.class);
         jobDetail.setDurability(true);
        
        

        // remove job
        assertTrue(this.fJobStore.removeJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup()));
        assertFalse(this.fJobStore.removeJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup()));
        this.fJobStore.storeJob(null, this.fJobDetail, false);
        checkJob();
    }

    private void checkJob() throws Exception {
        assertEquals(this.fJobDetail, this.fJobStore.retrieveJob(null, this.fJobDetail.getName(), this.fJobDetail
                .getGroup()));
        assertTrue(Arrays.asList(this.fJobStore.getJobGroupNames(null)).contains(this.fJobDetail.getGroup()));
        assertTrue(Arrays.asList(this.fJobStore.getJobNames(null, this.fJobDetail.getGroup())).contains(
                this.fJobDetail.getName()));
        assertEquals(0, this.fJobStore.getJobNames(null, "huh").length);
        assertEquals(1, this.fJobStore.getNumberOfJobs(null));
    }

    /**
     * fJobDetail must be durability for this test.
     * 
     * @throws Exception
     */
    public void testStore_RemoveTrigger() throws Exception {
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        this.fJobStore.storeTrigger(null, trigger, false);
        checkJob();
        checkTrigger(trigger);

        // store 2nd time
        try {
            this.fJobStore.storeTrigger(null, trigger, false);
            fail();
        } catch (ObjectAlreadyExistsException e) {
            //
        }

        // store 2nd time with overwrite option
        this.fJobStore.storeTrigger(null, trigger, true);
        checkJob();
        checkTrigger(trigger);

        // remove trigger
        assertTrue(this.fJobStore.removeTrigger(null, trigger.getName(), trigger.getGroup()));
        assertFalse(this.fJobStore.removeJob(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.storeTrigger(null, trigger, false);
    }

    private void checkTrigger(Trigger trigger) throws Exception {
        Trigger triggerFromStore = this.fJobStore.retrieveTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(trigger, triggerFromStore);
        assertTrue(trigger.getClass().isInstance(triggerFromStore));
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(1, this.fJobStore.getNumberOfTriggers(null));
        assertTrue(Arrays.asList(this.fJobStore.getTriggerGroupNames(null)).contains(trigger.getGroup()));
        assertTrue(Arrays.asList(this.fJobStore.getTriggerNames(null, trigger.getGroup())).contains(trigger.getName()));
    }

    /**
     * @throws Exception
     */
    public void testReplaceTrigger() throws Exception {
        Trigger trigger1 = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        this.fJobStore.storeTrigger(null, trigger1, false);

        // replace triggers
        Trigger trigger2 = new CronTrigger("trigger2", "triggerGroup2", this.fJobDetail.getName(), this.fJobDetail
                .getGroup());
        assertTrue(this.fJobStore.replaceTrigger(null, trigger1.getName(), trigger1.getGroup(), trigger2));
        assertFalse(this.fJobStore.replaceTrigger(null, trigger1.getName(), trigger1.getGroup(), trigger2));
        checkTrigger(trigger2);

        // replace again with undurability job
        this.fJobDetail.setDurability(false);
        this.fJobStore.storeJob(null, this.fJobDetail, true);
        assertTrue(this.fJobStore.replaceTrigger(null, trigger2.getName(), trigger2.getGroup(), trigger1));
        checkTrigger(trigger1);

        // replace again wrong triggername
        trigger2 = new CronTrigger("trigger2", "triggerGroup2", this.fJobDetail.getName(), this.fJobDetail.getGroup()
                + "huh");
        try {
            this.fJobStore.replaceTrigger(null, trigger1.getName(), trigger1.getGroup(), trigger2);
            fail();
        } catch (JobPersistenceException e) {
            //
        }
    }

    /**
     * @throws Exception
     */
    public void testDurable_UndurableJob() throws Exception {
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        this.fJobStore.storeTrigger(null, trigger, false);
        this.fJobStore.removeTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(1, this.fJobStore.getNumberOfJobs(null));

        this.fJobDetail.setDurability(false);
        this.fJobStore.storeJob(null, this.fJobDetail, true);
        this.fJobStore.storeTrigger(null, trigger, false);
        this.fJobStore.removeTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(0, this.fJobStore.getNumberOfJobs(null));
    }

    /**
     * @throws Exception
     */
    public void testGetTriggersForJob() throws Exception {
        assertEquals(0,
                this.fJobStore.getTriggersForJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup()).length);

        // add triggers
        Trigger[] triggers = new SimpleTrigger[10];
        for (int i = 0; i < triggers.length; i++) {
            triggers[i] = new SimpleTrigger("trigger" + i, "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                    .getGroup(), new Date(), new Date(), 0, 0);
            this.fJobStore.storeTrigger(null, triggers[i], false);
        }
        assertEquals(triggers.length, this.fJobStore.getTriggersForJob(null, this.fJobDetail.getName(), this.fJobDetail
                .getGroup()).length);

        // remove triggers
        for (int i = 0; i < triggers.length; i++) {
            this.fJobStore.removeTrigger(null, triggers[i].getName(), triggers[i].getGroup());
        }
        assertEquals(0,
                this.fJobStore.getTriggersForJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup()).length);
    }

    /**
     * @throws Exception
     */
    public void testTriggerStates() throws Exception {
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(System.currentTimeMillis() + 100000), new Date(
                System.currentTimeMillis() + 200000), 2, 2000);
        trigger.computeFirstFireTime(null);
        assertEquals(Trigger.STATE_NONE, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.storeTrigger(null, trigger, false);
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        this.fJobStore.pauseTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        this.fJobStore.resumeTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        assertNotNull(this.fJobStore.acquireNextTrigger(null,
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000));
        assertNull(this.fJobStore.acquireNextTrigger(null,
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000));
    }

    /**
     * Test all pause-methods which unaffect later added triggers.
     * 
     * @throws Exception
     */
    public void testTransientPause() throws Exception {
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        trigger.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger, false);

        // pause job
        this.fJobStore.pauseJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup());
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.resumeJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup());
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // pause trigger
        this.fJobStore.pauseTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.resumeTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
    }

    /**
     * Test all pause-methods which affect later added triggers.
     * 
     * @throws Exception
     */
    public void testPersistentPause() throws Exception {
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        trigger.computeFirstFireTime(null);

        // pause trigger group
        this.fJobStore.removeTrigger(null, trigger.getName(), trigger.getGroup());
        this.fJobStore.pauseTriggerGroup(null, trigger.getGroup());
        this.fJobStore.storeTrigger(null, trigger, true);
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.resumeTriggerGroup(null, trigger.getGroup());
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // pause all
        // need 2nd trigger so group is availible
        Trigger trigger2 = new SimpleTrigger("trigger2", trigger.getGroup(), this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        trigger2.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger2, false);
        this.fJobStore.removeTrigger(null, trigger.getName(), trigger.getGroup());

        this.fJobStore.pauseAll(null);
        this.fJobStore.storeTrigger(null, trigger, true);
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.resumeAll(null);
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
    }

    /**
     * (1) trigger2 = now - 10000 ____________________________________________
     * (2) trigger3 = now + 10000 ____________________________________________
     * (3) trigger1 = now + 20000 ____________________________________________
     * 
     * @throws Exception
     */
    public void testAcquireNextTrigger() throws Exception {
        Trigger trigger1 = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(System.currentTimeMillis() + 200000), new Date(
                System.currentTimeMillis() + 200000), 2, 2000);
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(System.currentTimeMillis() - 100000),
                new Date(System.currentTimeMillis() + 20000), 2, 2000);
        Trigger trigger3 = new SimpleTrigger("trigger1", "triggerGroup2", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(System.currentTimeMillis() + 100000), new Date(
                System.currentTimeMillis() + 200000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger1, false);
        this.fJobStore.storeTrigger(null, trigger2, false);
        this.fJobStore.storeTrigger(null, trigger3, false);

        assertNull(this.fJobStore.acquireNextTrigger(null, 10));
        assertEquals(trigger2, this.fJobStore.acquireNextTrigger(null, new Date(trigger1.getNextFireTime().getTime())
                .getTime() + 10000));
        assertEquals(trigger3, this.fJobStore.acquireNextTrigger(null, new Date(trigger1.getNextFireTime().getTime())
                .getTime() + 10000));
        assertEquals(trigger1, this.fJobStore.acquireNextTrigger(null, new Date(trigger1.getNextFireTime().getTime())
                .getTime() + 10000));
        assertNull(this.fJobStore.acquireNextTrigger(null,
                new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000));

        // because of trigger2
        assertEquals(1, this.fSignaler.fMisfireCount);

        // release trigger3
        this.fJobStore.releaseAcquiredTrigger(null, trigger3);
        assertEquals(trigger3, this.fJobStore.acquireNextTrigger(null, new Date(trigger1.getNextFireTime().getTime())
                .getTime() + 10000));
    }

    /**
     * @throws Exception
     */
    public void testTriggerFired_JobComplete() throws Exception {
        long now = System.currentTimeMillis();
        Trigger trigger1 = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 100000), new Date(now + 200000), 2, 2000);
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 200000), new Date(now + 200000), 2, 2000);
        Trigger trigger3 = new SimpleTrigger("trigger3", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 200000), new Date(now + 200000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger1, false);
        this.fJobStore.storeTrigger(null, trigger2, false);
        this.fJobStore.storeTrigger(null, trigger3, false);
        this.fJobStore.pauseTrigger(null, trigger3.getName(), trigger3.getGroup());

        // acquire
        assertEquals(trigger1, this.fJobStore.acquireNextTrigger(null, trigger1.getNextFireTime().getTime() + 1000));
        this.fJobStore.triggerFired(null, trigger1);
        assertEquals(Trigger.STATE_BLOCKED, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_BLOCKED, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger3.getName(), trigger3.getGroup()));

        // job complete
        this.fJobStore.triggeredJobComplete(null, trigger1, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore
                .getTriggerState(null, trigger2.getName(), trigger2.getGroup()));
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger3.getName(), trigger3.getGroup()));

        this.fJobStore.triggeredJobComplete(null, trigger2, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));

        // restore trigger1
        this.fJobStore.storeTrigger(null, trigger1, true);
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore
                .getTriggerState(null, trigger1.getName(), trigger1.getGroup()));
    }

    /**
     * @throws Exception
     */
    public void testTriggerInstructions() throws Exception {
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 2, 2000);
        trigger.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger, false);

        // delete
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail, Trigger.INSTRUCTION_DELETE_TRIGGER);
        assertEquals(Trigger.STATE_NONE, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // complete
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore
                .getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // error
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_ERROR);
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore.pauseTrigger(null, trigger.getName(), trigger.getGroup());
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // all complete
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 2, 2000);
        this.fJobStore.storeTrigger(null, trigger2, true);
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail,
                Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore
                .getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));

        // all error
        this.fJobStore.storeTrigger(null, trigger2, true);
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail,
                Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_ERROR);
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2.getGroup()));
    }

    /**
     * @throws Exception
     */
    public void testStoreCalender() throws Exception {
        Calendar calendar = new AnnualCalendar();
        String calenderName = "calender";

        this.fJobStore.storeCalendar(null, calenderName, calendar, false, false);
        checkCalender(calendar, calenderName);

        // store 2nd time
        try {
            this.fJobStore.storeCalendar(null, calenderName, calendar, false, false);
            fail();
        } catch (ObjectAlreadyExistsException e) {
            // 
        }
        checkCalender(calendar, calenderName);

        // store 2nd time with overwrite option
        calendar = new AnnualCalendar();
        this.fJobStore.storeCalendar(null, calenderName, calendar, true, false);
        checkCalender(calendar, calenderName);

        // store with update triggers
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 2, 2000);
        trigger.setCalendarName(calenderName);
        this.fJobStore.storeTrigger(null, trigger, false);
        this.fJobStore.storeCalendar(null, calenderName, calendar, true, true);

        // remove
        try {
            this.fJobStore.removeCalendar(null, calenderName);
            fail("referenced by a trigger");
        } catch (JobPersistenceException e) {
            // 
        }
        checkCalender(calendar, calenderName);

        assertTrue(this.fJobStore.removeTrigger(null, trigger.getName(), trigger.getGroup()));
        assertTrue(this.fJobStore.removeCalendar(null, calenderName));
        assertFalse(this.fJobStore.removeCalendar(null, calenderName));
    }

    private void checkCalender(Calendar calendar, String calenderName) throws Exception {
        assertEquals(calendar, this.fJobStore.retrieveCalendar(null, calenderName));
        assertEquals(1, this.fJobStore.getNumberOfCalendars(null));
        assertEquals(1, this.fJobStore.getCalendarNames(null).length);
        assertEquals(calenderName, this.fJobStore.getCalendarNames(null)[0]);
    }

    /**
     * @throws Exception
     */
    public void testPersistenceSimple() throws Exception {
        if (!this.fJobStore.supportsPersistence()) {
            return;
        }
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 0, 0);
        this.fJobStore.storeTrigger(null, trigger, false);
        Calendar calendar = new AnnualCalendar();
        this.fJobStore.storeCalendar(null, "calender", calendar, false, false);

        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(1, this.fJobStore.getNumberOfJobs(null));
        assertEquals(1, this.fJobStore.getNumberOfTriggers(null));
        assertEquals(1, this.fJobStore.getNumberOfCalendars(null));
        checkTrigger(trigger);
    }

    /**
     * Same as testTriggerFired_JobComplete but with restarts within.
     * 
     * @throws Exception
     */
    public void testPersistenceTriggerFired_JobComplete() throws Exception {
        if (!this.fJobStore.supportsPersistence()) {
            return;
        }

        Trigger trigger1 = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(System.currentTimeMillis() + 100000), new Date(
                System.currentTimeMillis() + 200000), 2, 2000);
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(System.currentTimeMillis() + 200000), new Date(
                System.currentTimeMillis() + 200000), 2, 2000);
        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger1, false);
        this.fJobStore.storeTrigger(null, trigger2, false);

        assertEquals(trigger1, this.fJobStore.acquireNextTrigger(null, trigger1.getNextFireTime().getTime() + 1000));
        this.fJobStore.triggerFired(null, trigger1);
        assertEquals(Trigger.STATE_BLOCKED, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_BLOCKED, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));

        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        this.fJobStore.triggeredJobComplete(null, trigger1, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore
                .getTriggerState(null, trigger2.getName(), trigger2.getGroup()));

        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        this.fJobStore.triggeredJobComplete(null, trigger2, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));

        // restore trigger1
        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        this.fJobStore.storeTrigger(null, trigger1, true);
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore
                .getTriggerState(null, trigger1.getName(), trigger1.getGroup()));
    }

    /**
     * Same as testTriggerInstructions but with restarts within.
     * 
     * @throws Exception
     */
    public void testPersistenceTriggerInstructions() throws Exception {
        if (!this.fJobStore.supportsPersistence()) {
            return;
        }

        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 2, 2000);
        trigger.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger, false);

        // delete
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail, Trigger.INSTRUCTION_DELETE_TRIGGER);
        assertEquals(Trigger.STATE_NONE, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(Trigger.STATE_NONE, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // complete
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore
                .getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore
                .getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // error
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail, Trigger.INSTRUCTION_SET_TRIGGER_ERROR);
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));

        // all complete
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 2, 2000);
        this.fJobStore.storeTrigger(null, trigger2, true);
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail,
                Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore
                .getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore
                .getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(Trigger.STATE_COMPLETE, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2
                .getGroup()));

        // all error
        this.fJobStore.storeTrigger(null, trigger2, true);
        this.fJobStore.storeTrigger(null, trigger, true);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail,
                Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_ERROR);
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2.getGroup()));
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger.getName(), trigger.getGroup()));
        assertEquals(Trigger.STATE_ERROR, this.fJobStore.getTriggerState(null, trigger2.getName(), trigger2.getGroup()));
    }

    /**
     * @throws Exception
     */
    public void testPersistenceJobDataMap() throws Exception {
        if (!this.fJobStore.supportsPersistence()) {
            return;
        }
        String key1 = "key1";
        String value1 = "value1";

        JobDataMap dataMap = new JobDataMap();
        dataMap.put(key1, value1);
        this.fJobDetail.setJobDataMap(dataMap);
        this.fJobStore.storeJob(null, this.fJobDetail, true);

        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(value1, this.fJobStore.retrieveJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup())
                .getJobDataMap().get(key1));

        // jobDataMap at stateful jobs
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        String value2 = "value2";
        dataMap.put(key1, value2);
        this.fJobDetail.setJobDataMap(dataMap);
        Trigger trigger = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(), new Date(), 2, 2000);
        trigger.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger, false);
        this.fJobStore.triggeredJobComplete(null, trigger, this.fJobDetail,
                Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE);
        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(value2, this.fJobStore.retrieveJob(null, this.fJobDetail.getName(), this.fJobDetail.getGroup())
                .getJobDataMap().get(key1));
    }

    /**
     * @throws Exception
     */
    public void testPersistencePausedTriggerGroups() throws Exception {
        if (!this.fJobStore.supportsPersistence()) {
            return;
        }
        long now = System.currentTimeMillis();
        Trigger trigger1 = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 100000), new Date(now + 1000000), 2, 2000);
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 200000), new Date(now + 1000000), 2, 2000);
        Trigger trigger3 = new SimpleTrigger("trigger1", "triggerGroup2", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 300000), new Date(now + 1000000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger1, false);
        this.fJobStore.storeTrigger(null, trigger2, false);
        this.fJobStore.storeTrigger(null, trigger3, false);

        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);

        this.fJobStore.pauseTriggerGroup(null, trigger1.getGroup());
        assertEquals(1, this.fJobStore.getPausedTriggerGroups(null).size());
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger1.getName(), trigger1.getGroup()));
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger2.getName(), trigger2.getGroup()));
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore
                .getTriggerState(null, trigger3.getName(), trigger3.getGroup()));
        assertEquals(trigger3, this.fJobStore.acquireNextTrigger(null, trigger3.getNextFireTime().getTime()));

        // add new trigger
        Trigger trigger4 = new SimpleTrigger("trigger4", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 100000), new Date(now + 1000000), 2, 2000);
        this.fJobStore.storeTrigger(null, trigger4, false);
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger4.getName(), trigger4.getGroup()));
    }

    /**
     * @throws Exception
     */
    public void testPersitenceBlockedPaused() throws Exception {
        if (!this.fJobStore.supportsPersistence()) {
            return;
        }
        long now = System.currentTimeMillis();
        Trigger trigger1 = new SimpleTrigger("trigger1", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 100000), new Date(now + 1000000), 2, 2000);
        Trigger trigger2 = new SimpleTrigger("trigger2", "triggerGroup1", this.fJobDetail.getName(), this.fJobDetail
                .getGroup(), new Date(now + 200000), new Date(now + 1000000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        this.fJobStore.storeTrigger(null, trigger1, false);
        this.fJobStore.storeTrigger(null, trigger2, false);

        this.fJobStore.pauseTrigger(null, trigger2.getName(), trigger2.getGroup());
        this.fJobStore.triggerFired(null, trigger1);
        assertEquals(Trigger.STATE_BLOCKED, this.fJobStore.getTriggerState(null, trigger1.getName(), trigger1
                .getGroup()));
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger2.getName(), trigger2.getGroup()));

        // restart
        this.fJobStore = new FileJobStore();
        this.fJobStore.initialize(null, this.fSignaler);
        assertEquals(Trigger.STATE_NORMAL, this.fJobStore
                .getTriggerState(null, trigger1.getName(), trigger1.getGroup()));
        assertEquals(Trigger.STATE_PAUSED, this.fJobStore
                .getTriggerState(null, trigger2.getName(), trigger2.getGroup()));
    }

    private class SampleJob implements StatefulJob {

        /**
         * 
         */
        public int fExecutionCount = 0;

        public void execute(JobExecutionContext context) throws JobExecutionException {
            this.fExecutionCount++;
        }
    }

    private class SampleSignaler implements SchedulerSignaler {

        /**
         * 
         */
        public int fMisfireCount = 0;

        /**
         * 
         */
        public int fChangeCount = 0;

        public void notifyTriggerListenersMisfired(Trigger trigger) {
            this.fMisfireCount++;
        }

        public void signalSchedulingChange() {
            this.fChangeCount++;
        }

        @Override
        public void notifySchedulerListenersFinalized(Trigger arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void signalSchedulingChange(long arg0) {
            // TODO Auto-generated method stub

        }
    }
}
