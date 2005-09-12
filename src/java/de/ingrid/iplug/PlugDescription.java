/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;


public class PlugDescription {
    
    private String fPlugId;
    private String fOraganisation;
    private String fPersonName;
    private String fPersonSureName;
    private String fPersoneMail;
    private IDataSourceConnection fConnection;
    private String fDataType;
    private String fWorkinDirectory;
    private boolean fCronBasedIndexing;
    private DataMapping fMapping;
    
    /**
     * @return Returns the connection.
     */
    public IDataSourceConnection getConnection() {
        return this.fConnection;
    }
    /**
     * @param connection The connection to set.
     */
    public void setConnection(IDataSourceConnection connection) {
        this.fConnection = connection;
    }
    /**
     * @return Returns the cronBasedIndexing.
     */
    public boolean isCronBasedIndexing() {
        return this.fCronBasedIndexing;
    }
    /**
     * @param cronBasedIndexing The cronBasedIndexing to set.
     */
    public void setCronBasedIndexing(boolean cronBasedIndexing) {
        this.fCronBasedIndexing = cronBasedIndexing;
    }
    /**
     * @return Returns the dataType.
     */
    public String getDataType() {
        return this.fDataType;
    }
    /**
     * @param dataType The dataType to set.
     */
    public void setDataType(String dataType) {
        this.fDataType = dataType;
    }
    /**
     * @return Returns the mapping.
     */
    public DataMapping getMapping() {
        return this.fMapping;
    }
    /**
     * @param mapping The mapping to set.
     */
    public void setMapping(DataMapping mapping) {
        this.fMapping = mapping;
    }
    /**
     * @return Returns the oraganisation.
     */
    public String getOraganisation() {
        return this.fOraganisation;
    }
    /**
     * @param oraganisation The oraganisation to set.
     */
    public void setOraganisation(String oraganisation) {
        this.fOraganisation = oraganisation;
    }
    /**
     * @return Returns the personeMail.
     */
    public String getPersoneMail() {
        return this.fPersoneMail;
    }
    /**
     * @param personeMail The personeMail to set.
     */
    public void setPersoneMail(String personeMail) {
        this.fPersoneMail = personeMail;
    }
    /**
     * @return Returns the personName.
     */
    public String getPersonName() {
        return this.fPersonName;
    }
    /**
     * @param personName The personName to set.
     */
    public void setPersonName(String personName) {
        this.fPersonName = personName;
    }
    /**
     * @return Returns the personSurName.
     */
    public String getPersonSureName() {
        return this.fPersonSureName;
    }
    /**
     * @param personSurName The personSurName to set.
     */
    public void setPersonSureName(String personSurName) {
        this.fPersonSureName = personSurName;
    }
    /**
     * @return Returns the plugId.
     */
    public String getPlugId() {
        return this.fPlugId;
    }
    /**
     * @param plugId The plugId to set.
     */
    public void setPlugId(String plugId) {
        this.fPlugId = plugId;
    }
    /**
     * @return Returns the workinDirectory.
     */
    public String getWorkinDirectory() {
        return this.fWorkinDirectory;
    }
    /**
     * @param workinDirectory The workinDirectory to set.
     */
    public void setWorkinDirectory(String workinDirectory) {
        this.fWorkinDirectory = workinDirectory;
    }
    
    
}
