/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;
import java.util.ArrayList;

public class PlugDescription {

    private String fPlugId;

    private String fOraganisation;

    private String fPersonName;

    private String fPersonSureName;

    private String fPersoneMail;

    private IDataSourceConnection fConnection;

    private String fDataType;

    private File fWorkinDirectory;

    private boolean fCronBasedIndexing;

    private IDataMapping fMapping;

    private ArrayList fFields = new ArrayList();

    /**
     * @return Returns the connection.
     */
    public IDataSourceConnection getConnection() {
        return fConnection;
    }

    /**
     * @param connection
     *            The connection to set.
     */
    public void setConnection(IDataSourceConnection connection) {
        fConnection = connection;
    }

    /**
     * @return Returns the cronBasedIndexing.
     */
    public boolean isCronBasedIndexing() {
        return fCronBasedIndexing;
    }

    /**
     * @param cronBasedIndexing
     *            The cronBasedIndexing to set.
     */
    public void setCronBasedIndexing(boolean cronBasedIndexing) {
        fCronBasedIndexing = cronBasedIndexing;
    }

    /**
     * @return Returns the dataType.
     */
    public String getDataType() {
        return fDataType;
    }

    /**
     * @param dataType
     *            The dataType to set.
     */
    public void setDataType(String dataType) {
        fDataType = dataType;
    }

    /**
     * @return Returns the mapping.
     */
    public IDataMapping getMapping() {
        return fMapping;
    }

    /**
     * @param mapping
     *            The mapping to set.
     */
    public void setMapping(IDataMapping mapping) {
        fMapping = mapping;
    }

    /**
     * @return Returns the oraganisation.
     */
    public String getOraganisation() {
        return fOraganisation;
    }

    /**
     * @param oraganisation
     *            The oraganisation to set.
     */
    public void setOraganisation(String oraganisation) {
        fOraganisation = oraganisation;
    }

    /**
     * @return Returns the personeMail.
     */
    public String getPersoneMail() {
        return fPersoneMail;
    }

    /**
     * @param personeMail
     *            The personeMail to set.
     */
    public void setPersoneMail(String personeMail) {
        fPersoneMail = personeMail;
    }

    /**
     * @return Returns the personName.
     */
    public String getPersonName() {
        return fPersonName;
    }

    /**
     * @param personName
     *            The personName to set.
     */
    public void setPersonName(String personName) {
        fPersonName = personName;
    }

    /**
     * @return Returns the personSureName.
     */
    public String getPersonSureName() {
        return fPersonSureName;
    }

    /**
     * @param personSureName
     *            The personSureName to set.
     */
    public void setPersonSureName(String personSureName) {
        fPersonSureName = personSureName;
    }

    /**
     * @return Returns the plugId.
     */
    public String getPlugId() {
        return fPlugId;
    }

    /**
     * @param plugId
     *            The plugId to set.
     */
    public void setPlugId(String plugId) {
        fPlugId = plugId;
    }

    /**
     * @return Returns the workinDirectory.
     */
    public File getWorkinDirectory() {
        return fWorkinDirectory;
    }

    /**
     * @param workinDirectory
     *            The workinDirectory to set.
     */
    public void setWorkinDirectory(File workinDirectory) {
        fWorkinDirectory = workinDirectory;
    }

    /**
     * @return the fields of this plug
     */
    public String[] getFields() {
        return (String[]) fFields.toArray(new String[this.fFields.size()]);
    }

    /**
     * Adds a fieldName to the plug description
     * @param fieldName
     */
    public void addField(String fieldName) {
        this.fFields.add(fieldName);
    }

}
