/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

/**
 * Container for meta data that describes a localy or remote living IPlug
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public interface IIPlug {

    /**
     * A network wide unique id for the IIPlug
     * 
     * @return The unique IPlug identifier
     */
    public String getId();

    /**
     * Allows getter access to the supported datafields
     * 
     * @return The supported data fields
     */
    public String[] getFields();

    /**
     * @return the data type of this plug or <code>null</code> if no data type
     *         was defined
     */
    public String getDataType();

}
