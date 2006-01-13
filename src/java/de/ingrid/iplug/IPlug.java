/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import de.ingrid.utils.ISearcher;

/**
 * the offical plug interface
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public interface IPlug extends ISearcher {

    /**
     * will be invoked directly after instantiating the Iplug Object by the IPlugServer, better use
     * this method than a default construtor to setup fields
     * 
     * @param plugDescription
     */
    void configure(PlugDescription plugDescription) throws Exception;

}
