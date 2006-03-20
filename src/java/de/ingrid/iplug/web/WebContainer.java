/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.BasicAuthenticator;
import org.mortbay.http.HashUserRealm;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.SecurityHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

public class WebContainer extends Thread {
    private static Log log = LogFactory.getLog(WebContainer.class);

    private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

    private static Server fServer;

    private int fPort;

    // private String fWebAppPath;

    private HashUserRealm fRealm  = new HashUserRealm("HashUserRealm");;

    private boolean fSecured;

    // private HashUserRealm fHashRealm;

    public WebContainer(int port, boolean secured) {
        fPort = port;
        fSecured = secured;
    } /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */

    public void run() {
        try {
            fServer = new Server();
            // Create a port listener
            SocketListener listener = new SocketListener();
            listener.setPort(fPort);
            fServer.addListener(listener);

            if (fSecured) {
              
                // fRealm.put("admin", "admin");
                fServer.addRealm(fRealm);
            }
            fServer.start();
        } catch (Exception e) {
            log.error(e);
        }

    }

    public void addWebapp(String name, String path) throws Exception {
        if (fServer==null || !fServer.isStarted()) {
            throw new IOException("web container not started");
        }
        WebApplicationContext context = fServer.addWebApplication("/"+name, path);
        SecurityHandler handler = new SecurityHandler();
        handler.setAuthMethod("BASIC");
        context.addHandler(handler);
        context.setAuthenticator(new BasicAuthenticator());
        SecurityConstraint sc = new SecurityConstraint();
        sc.setAuthenticate(true);
        sc.addRole(SecurityConstraint.ANY_ROLE);
        context.addSecurityConstraint("/", sc);
        context.start();
    }

    /**
     * starts the webcontainer
     * 
     * @throws IOException
     */
    public void startContainer() throws IOException {
        start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        if (!fServer.isStarted()) {
            throw new IOException("Could not start web container");
        }
    }

    /**
     * stops the webcontainer
     * 
     * @throws InterruptedException
     */
    public void stopContainer() throws InterruptedException {
        if (fServer != null && fServer.isStarted()) {
            fServer.stop();
        }
    }

    /**
     * Adds a user allowed accessing the context
     * 
     * @param userName
     * @param password
     */
    public void addUser(String userName, String password) {
        // HashUserRealm hashRealm = new HashUserRealm();
        // hashRealm.setName(userName);
        fRealm.put(userName, password);
        // fServer.addRealm(hashRealm);
    }

    /**
     * Removes a user to be able to login
     * 
     * @param userName
     */
    public void removeUser(String userName) {
        fServer.removeRealm(userName);
    }

    public static void main(String[] args) throws IOException {
        URL url = WebContainer.class.getClassLoader().getResource("info");
        String path = url.getPath();
        if (WINDOWS && path.startsWith("/")) {
            path = path.substring(1);
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        File infoFolder = new File(path);

        // WebContainer container = new WebContainer(8082, infoFolder
        // .getCanonicalPath(), true);
        WebContainer container = new WebContainer(8082, true);
        container.startContainer();
        container.addUser("admin2", "password");

    }

}
