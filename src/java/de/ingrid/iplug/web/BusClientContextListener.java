package de.ingrid.iplug.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;
import de.ingrid.ibus.client.BusClient;
import de.ingrid.utils.BeanFactory;

public class BusClientContextListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(BusClientContextListener.class);

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent servletcontextevent) {
        ServletContext servletContext = servletcontextevent.getServletContext();
        String communicationFile = servletContext.getInitParameter("communication.properties");
        BusClient busClient = null;
        try {
            busClient = connectIBus(communicationFile);
            BeanFactory beanFactory = (BeanFactory) servletContext.getAttribute("beanFactory");
            beanFactory.addBean("busClient", busClient);
        } catch (IOException e1) {
            LOG.error("can not connect to ibus", e1);
        }

    }

    private BusClient connectIBus(String communicationFile) throws IOException {
        ICommunication communication = initCommunication(new File(communicationFile));
        BusClient busClient = BusClient.instance();
        busClient.setCommunication(communication);

        // DIRTY HACK; we cast the ICommunication into TcpCommunication.
        String serverName = (String) ((TcpCommunication) communication).getServerNames().get(0);
        busClient.setBusUrl(serverName);
        // DIRTY HACK
        return busClient;
    }

    private static ICommunication initCommunication(File communicationProperties) throws IOException {
        LOG.info("read communication.properties: " + communicationProperties.getAbsolutePath());
        FileInputStream confIS = new FileInputStream(communicationProperties);
        ICommunication communication = StartCommunication.create(confIS);
        communication.startup();
        return communication;
    }

}
