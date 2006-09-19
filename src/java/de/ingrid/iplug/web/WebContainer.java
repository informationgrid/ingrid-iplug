/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.web;

import java.io.IOException;
import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.BasicAuthenticator;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SocketListener;
import org.mortbay.http.UserRealm;
import org.mortbay.http.handler.SecurityHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

/**
 * 
 */
public class WebContainer extends Thread {

    private static Log log = LogFactory.getLog(WebContainer.class);

    private static final int SESSION_TIMEOUT = 60 * 60 * 8; // 8 days for timeout...

    private static Server fServer;

    private int fPort;

    private UserRealm fRealm;

    private boolean fSecured;

    /**
     * @param port
     * @param secured
     */
    public WebContainer(int port, boolean secured) {
        this.fPort = port;
        this.fSecured = secured;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            fServer = new Server();
            // Create a port listener
            SocketListener listener = new SocketListener();
            listener.setPort(this.fPort);
            fServer.addListener(listener);

            if (this.fSecured) {
                fServer.addRealm(this.fRealm);
            }

            fServer.start();
        } catch (Exception e) {
            log.error(e);
        }

    }

    /**
     * @param name
     * @param path
     * @throws Exception
     */
    public void addWebapp(String name, String path) throws Exception {
        if (fServer == null || !fServer.isStarted()) {
            throw new IOException("web container not started");
        }
        WebApplicationContext context = fServer.addWebApplication('/' + name, path);
        context.getServletHandler().setSessionInactiveInterval(SESSION_TIMEOUT);

        if (this.fSecured) {
            SecurityHandler handler = new SecurityHandler();
            handler.setAuthMethod("BASIC");
            context.addHandler(handler);
            context.setAuthenticator(new BasicAuthenticator());
            SecurityConstraint sc = new SecurityConstraint();
            sc.setAuthenticate(true);
            sc.addRole(SecurityConstraint.ANY_ROLE);
            context.addSecurityConstraint("/", sc);
        }
        context.setAttribute("server", this);
        context.start();
    }

    /**
     * Starts the webContainer.
     * 
     * @throws IOException
     */
    public void startContainer() throws IOException {
        int i = 3;
        start();

        while ((null == fServer) || (!fServer.isStarted())) {
            i--;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                log.error(ie.getMessage(), ie);
            }
            
            if (0 == i) {
                throw new IOException("Could not start web container");
            }
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
     * logout a user.
     * 
     * @param principal
     */
    public void logoutUser(Principal principal) {
        if (this.fRealm != null) {
            this.fRealm.disassociate(principal);
            this.fRealm.logout(principal);
        } else {
            log.warn("No realm is set.");
        }
    }

    /**
     * Removes a user to be able to login
     * 
     * @param userName
     */
    public void removeUser(String userName) {
        fServer.removeRealm(userName);
    }

    /**
     * @return
     */
    public UserRealm getRealm() {
        return this.fRealm;
    }

    /**
     * @param realm
     */
    public void setRealm(UserRealm realm) {
        this.fRealm = realm;
    }
}
