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

public class WebContainer implements Runnable {
    private static Log log = LogFactory.getLog(WebContainer.class);

    private static final boolean WINDOWS = System.getProperty("os.name")
            .startsWith("Windows");

    private static Server fServer;

    private int fPort;

    private String fWebAppPathe;

    public WebContainer(int port, String webappPath) {
        fPort = port;
        fWebAppPathe = webappPath;
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
            WebApplicationContext context = fServer.addWebApplication("/", fWebAppPathe);
            
            HashUserRealm hr = new HashUserRealm();
            hr.setName("Test Realm");
            hr.put("admin", "admin");
            hr.remove("admin");
            fServer.addRealm(hr);
            SecurityHandler handler = new SecurityHandler();
            handler.setAuthMethod("BASIC");
            context.addHandler(handler);
            context.setAuthenticator(new BasicAuthenticator()); 
            SecurityConstraint sc = new SecurityConstraint();
            sc.setAuthenticate(true);
            sc.addRole(SecurityConstraint.ANY_ROLE);

            context.addSecurityConstraint("/", sc); 
            
            fServer.start();
        } catch (Exception e) {
            log.error(e);
        }

    }

    /**
     * starts the webcontainer
     * 
     * @throws IOException
     */
    public static void startContainer(int port, String webappPath)
            throws IOException {
        if (fServer != null && fServer.isStarted()) {
            throw new IOException("container is allready running");
        }
        new Thread(new WebContainer(port, webappPath)).start();
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
    public static void stopContainer() throws InterruptedException {
        if (fServer != null && fServer.isStarted()) {
            fServer.stop();
        }
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
        WebContainer.startContainer(8082, infoFolder.getCanonicalPath());
    }

}
