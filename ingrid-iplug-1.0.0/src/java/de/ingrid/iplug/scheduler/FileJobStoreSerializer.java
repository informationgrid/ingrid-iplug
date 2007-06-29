/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.quartz.JobPersistenceException;

/**
 * FileJobStoreSerializer is responsible for writing the relevant data of
 * FileJobStore to files.
 * Note:only the write/save methods are synchronized cause it is assumed
 * that load and clean operations invoked synchronous.
 * 
 * @version $Revision: $
 */
public class FileJobStoreSerializer {

    private File fJobsFile;

    private File fTriggersFile;

    private File fCalendarsFile;

    private File fTriggerStatesFile;

    private File fPausedTriggerGroupsFile;

    /**
     * Initializes a FileJobStoreSerializer.
     * @param storeDirectory
     * @throws JobPersistenceException
     */
    public FileJobStoreSerializer(String storeDirectory) throws JobPersistenceException {
        File storeDirectoryFile = new File(storeDirectory);
        storeDirectoryFile.mkdirs();
        if (!storeDirectoryFile.exists()) {
            throw new JobPersistenceException("could not create the store directory " + storeDirectoryFile.getPath());
        }

        this.fJobsFile = new File(storeDirectoryFile, "jobs.dat");
        this.fTriggersFile = new File(storeDirectoryFile, "triggers.dat");
        this.fCalendarsFile = new File(storeDirectoryFile, "calendars.dat");
        this.fTriggerStatesFile = new File(storeDirectoryFile, "trigger-states.dat");
        this.fPausedTriggerGroupsFile = new File(storeDirectoryFile, "paused-trigger-groups.dat");
    }

    /**
     * Returns the root directory of the store.
     * @return The root directory of the store.
     */
    public File getStoreDirectory() {
        return this.fJobsFile.getParentFile();
    }

    /**
     * Cleans the store to conceive a empty store.
     */
    public void clear() {
        this.fJobsFile.delete();
        this.fTriggersFile.delete();
        this.fCalendarsFile.delete();
        this.fTriggerStatesFile.delete();
        this.fPausedTriggerGroupsFile.delete();
    }

    /**
     * Saves the serialized jobs to a file.
     * @param jobs The serialized jobs to store.
     * @throws JobPersistenceException
     */
    public void saveJobs(Serializable jobs) throws JobPersistenceException {
        synchronized (this.fJobsFile) {
            saveObjectToFile(this.fJobsFile, jobs);
        }

    }

    /**
     * Saves the serialized triggers to a file.
     * @param triggers The serialized triggers to store.
     * @throws JobPersistenceException
     */
    public void saveTriggers(Serializable triggers) throws JobPersistenceException {
        synchronized (this.fTriggersFile) {
            saveObjectToFile(this.fTriggersFile, triggers);
        }
    }

    /**
     * Saves the serialized calendars to a file. 
     * @param calendars The serialized calendars to store.
     * @throws JobPersistenceException
     */
    public void saveCalendars(Serializable calendars) throws JobPersistenceException {
        synchronized (this.fCalendarsFile) {
            saveObjectToFile(this.fCalendarsFile, calendars);
        }
    }

    /**
     * Saves the serialized trigger states to a file.
     * @param triggerStates The serialized trigger states to store.
     * @throws JobPersistenceException
     */
    public void saveTriggerStates(Serializable triggerStates) throws JobPersistenceException {
        synchronized (this.fTriggerStatesFile) {
            saveObjectToFile(this.fTriggerStatesFile, triggerStates);
        }
    }

    /**
     * Saves the serialized paused trigger groups to a file.
     * @param pausedTriggerGroups The serialized paused trigger groups to store.
     * @throws JobPersistenceException
     */
    public void savePausedTriggerGroups(Serializable pausedTriggerGroups) throws JobPersistenceException {
        synchronized (this.fPausedTriggerGroupsFile) {
            saveObjectToFile(this.fPausedTriggerGroupsFile, pausedTriggerGroups);
        }
    }

    /**
     * Loads previously stored jobs.
     * @return The loaded jobs.
     * @throws JobPersistenceException
     */
    public Serializable loadJobs() throws JobPersistenceException {
        return loadObjectsFromFile(this.fJobsFile);
    }

    /**
     * Loads previously stored triggers.
     * @return The loaded triggers.
     * @throws JobPersistenceException
     */
    public Serializable loadTriggers() throws JobPersistenceException {
        return loadObjectsFromFile(this.fTriggersFile);
    }

    /**
     * Loads previously stored calendars.
     * @return The loaded calendars.
     * @throws JobPersistenceException
     */
    public Serializable loadCalendars() throws JobPersistenceException {
        return loadObjectsFromFile(this.fCalendarsFile);
    }

    /**
     * Loads previously stored trigger states.
     * @return The loaded trigger states.
     * @throws JobPersistenceException
     */
    public Serializable loadTriggerStates() throws JobPersistenceException {
        return loadObjectsFromFile(this.fTriggerStatesFile);
    }

    /**
     * Loads previously stored paused trigger groups.
     * @return The loaded paused trigger groups.
     * @throws JobPersistenceException
     */
    public Serializable loadPausedTriggerGroups() throws JobPersistenceException {
        return loadObjectsFromFile(this.fPausedTriggerGroupsFile);
    }

    private void saveObjectToFile(File file, Serializable serializable) throws JobPersistenceException {
        try {
            FileOutputStream fileOutStream = new FileOutputStream(file);
            ObjectOutputStream objOutStream = new ObjectOutputStream(fileOutStream);
            objOutStream.writeObject(serializable);
            fileOutStream.close();
        } catch (IOException e) {
            throw new JobPersistenceException("could not store to file " + file.getPath(), e);
        }
    }

    private Serializable loadObjectsFromFile(File file) throws JobPersistenceException {
        if (!file.exists() || file.length() == 0)
            return null;

        try {
            FileInputStream fileInStream = new FileInputStream(file);
            ObjectInputStream objInStream = new ObjectInputStream(fileInStream);
            Serializable serializable = (Serializable) objInStream.readObject();
            objInStream.close();
            fileInStream.close();
            return serializable;
        } catch (Exception e) {
            throw new JobPersistenceException("could not load from file " + file.getPath(), e);
        }
    }
}
