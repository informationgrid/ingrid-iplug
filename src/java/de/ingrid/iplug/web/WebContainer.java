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
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

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
            fServer.addWebApplication("/", fWebAppPathe);

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
        WebContainer.startContainer(8080, infoFolder.getCanonicalPath());
    }

}
