/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: DispatcherTest.java,v $
 */

package de.ingrid.iplug.scheduler;

import java.io.File;

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

	private File fTestFolder;

	private SchedulingService fSchedulingService;

	protected void setUp() throws Exception {
		// clean directory
		fTestFolder = new File("" + System.currentTimeMillis() + "");
		fTestFolder.mkdirs();
		fSchedulingService = new SchedulingService(fTestFolder);
		FileJobStore jobStore = new FileJobStore();
		jobStore.initialize(null, null);
		jobStore.clear();
	}

	protected void tearDown() throws Exception {

		fSchedulingService.removeJob(this.fJobName, null);
		fSchedulingService.shutdown();
		File[] files = fTestFolder.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
		fTestFolder.delete();
		CountJob.fExecutionCount = 0;

	}

	/**
	 * @throws Exception
	 */
	public void testScheduleCronJob1() throws Exception {
		fSchedulingService.scheduleCronJob(this.fJobName, null, CountJob.class,
				null, "0/1 * * * * ? *");
		Thread.sleep(500);
		assertTrue(CountJob.fExecutionCount > 0);
	}

	/**
	 * @throws Exception
	 */
	public void testScheduleCronJob2() throws Exception {
		fSchedulingService.scheduleCronJob(this.fJobName, null,
				CountJob.class, null, "0/1", null, null, null, null, "?", null);
		Thread.sleep(500);
		assertTrue(CountJob.fExecutionCount > 0);
	}

	/**
	 * @throws Throwable
	 * 
	 */
	public void testGetCronExpression() throws Throwable {
		String cronExpression = SchedulingService.getCronExpression("0", "0/5",
				null, null, null, "?", null);
		assertEquals("0 0/5 * * * ? *", cronExpression);
		new CronTrigger().setCronExpression(cronExpression);
		cronExpression = SchedulingService.getCronExpression("0", "30",
				"10-13", "?", null, "WED,FRI", null);
		assertEquals("0 30 10-13 ? * WED,FRI *", cronExpression);
		new CronTrigger().setCronExpression(cronExpression);
	}
}
