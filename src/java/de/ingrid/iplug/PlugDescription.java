/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug;

import java.io.File;
import java.util.ArrayList;

import de.ingrid.utils.IngridDocument;

/**
 * PlugDescription contains describing values of an {@link de.ingrid.iplug.IPlug}. Standard values could be stored and
 * retrieved using the PlugDescription methods. Additional values could be stored and retrieved using the put and get
 * methods derived from {@link java.util.HashMap}.
 * 
 * <p/>created on 19.09.2005
 * 
 * @version $Revision: $
 * @author sg
 * @author $Author jz ${lastedit}
 *  
 */
public class PlugDescription extends IngridDocument {

    private static final String CONNECTION = "connection";

    private static final String IS_CRONBASED_INDEXING = "cronbasedIndexing";

    private static final String DATA_TYPE = "dataType";

    private static final String MAPPING = "mapping";

    private static final String ORGANISATION = "organisation";

    private static final String PERSONAL_MAIL = "personalMail";

    private static final String PERSON_NAME = "personName";

    private static final String PERSON_SURE_NAME = "personSureName";

    private static final String IPLUG_ID = "plugId";

    private static final String WORKING_DIRECTORY = "workingDirectory";

    private static final String FIELDS = "fields";

    private static final Object PROXY_SERVICE_URL = "proxyServiceUrl";

    private static final Object IPLUG_CLASS = "iPlugClass";

    /**
     * @return Returns the connection.
     */
    public IDataSourceConnection getConnection() {
        return (IDataSourceConnection) get(CONNECTION);
    }

    /**
     * @param connection
     *            The connection to set.
     */
    public void setConnection(IDataSourceConnection connection) {
        put(CONNECTION, connection);
    }

    /**
     * @return Returns the cronBasedIndexing.
     */
    public boolean isCronBasedIndexing() {
        return getBoolean(IS_CRONBASED_INDEXING);
    }

    /**
     * @param cronBasedIndexing
     *            The cronBasedIndexing to set.
     */
    public void setCronBasedIndexing(boolean cronBasedIndexing) {
        setBoolean(IS_CRONBASED_INDEXING, cronBasedIndexing);
    }

    /**
     * @return Returns the dataType.
     */
    public String getDataType() {
        return (String) get(DATA_TYPE);
    }

    /**
     * @param dataType
     *            The dataType to set.
     */
    public void setDataType(String dataType) {
        put(DATA_TYPE, dataType);
    }

    /**
     * @return Returns the mapping.
     */
    public IDataMapping getMapping() {
        return (IDataMapping) get(MAPPING);
    }

    /**
     * @param mapping
     *            The mapping to set.
     */
    public void setMapping(IDataMapping mapping) {
        put(MAPPING, mapping);
    }

    /**
     * @return Returns the oraganisation.
     */
    public String getOraganisation() {
        return (String) get(ORGANISATION);
    }

    /**
     * @param oraganisation
     *            The oraganisation to set.
     */
    public void setOraganisation(String oraganisation) {
        put(ORGANISATION, oraganisation);
    }

    /**
     * @return Returns the personeMail.
     */
    public String getPersoneMail() {
        return (String) get(PERSONAL_MAIL);
    }

    /**
     * @param personeMail
     *            The personeMail to set.
     */
    public void setPersoneMail(String personeMail) {
        put(PERSONAL_MAIL, personeMail);
    }

    /**
     * @return Returns the personName.
     */
    public String getPersonName() {
        return (String) get(PERSON_NAME);
    }

    /**
     * @param personName
     *            The personName to set.
     */
    public void setPersonName(String personName) {
        put(PERSON_NAME, personName);
    }

    /**
     * @return Returns the personSureName.
     */
    public String getPersonSureName() {
        return (String) get(PERSON_SURE_NAME);
    }

    /**
     * @param personSureName
     *            The personSureName to set.
     */
    public void setPersonSureName(String personSureName) {
        put(PERSON_SURE_NAME, personSureName);
    }

    /**
     * @return Returns the plugId.
     */
    public String getPlugId() {
        return (String) get(IPLUG_ID);
    }

    /**
     * @param plugId
     *            The plugId to set.
     */
    public void setPlugId(String plugId) {
        put(IPLUG_ID, plugId);
    }

    /**
     * @return Returns the workinDirectory.
     */
    public File getWorkinDirectory() {
        String folder = (String) get(WORKING_DIRECTORY);
        return new File(folder);
    }

    /**
     * @param workinDirectory
     *            The workinDirectory to set.
     */
    public void setWorkinDirectory(File workinDirectory) {
        put(WORKING_DIRECTORY, workinDirectory.getAbsolutePath());
    }

    /**
     * @return the fields of this plug
     */
    public String[] getFields() {
        ArrayList arrayList = getArrayList(FIELDS);
        if (arrayList == null) {
            return new String[0];
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    /**
     * Adds a fieldName to the plug description
     * 
     * @param fieldName
     */
    public void addField(String fieldName) {
        addToList(FIELDS, fieldName);
    }

    /**
     * @param proxyServiceUrl
     *            The URL of the proxy service.
     */
    public void setProxyServiceURL(String proxyServiceUrl) {
        put(PROXY_SERVICE_URL, proxyServiceUrl);
    }

    /**
     * @return The URL of the proxy service.
     */
    public String getProxyServiceURL() {
        return (String) get(PROXY_SERVICE_URL);
    }

    /**
     * @param iPlugClass
     *            The class of the used IPlug.
     */
    public void setIPlugClass(Class iPlugClass) {
        put(IPLUG_CLASS, iPlugClass);
    }

    /**
     * @return The class of the used IPlug.
     */
    public Class getIPlugClass() {
        return (Class) get(IPLUG_CLASS);
    }
}
