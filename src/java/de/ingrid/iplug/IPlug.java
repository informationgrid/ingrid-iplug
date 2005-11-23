/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;

/**
 * the offical plug interface
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public interface IPlug {

    /**
     * Search the data source represented as plug for matching.
     * 
     * @param query
     * @param start
     * @param lenght
     * @return matching documents
     * @throws Exception
     */
    IngridDocument[] search(IngridQuery query, int start, int lenght) throws Exception;

}
