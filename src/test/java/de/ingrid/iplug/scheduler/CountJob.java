/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * An quartz job which count its execution times. 
 * 
 * <br/><br/>created on 11.10.2005
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author jz ${lastedit}
 *  
 */
public class CountJob implements Job {

    /**
     * 
     */
    public static int fExecutionCount = 0;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        fExecutionCount++;
    }

}
