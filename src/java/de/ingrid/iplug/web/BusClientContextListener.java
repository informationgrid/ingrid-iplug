package de.ingrid.iplug.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.BeanFactory;

public class BusClientContextListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(BusClientContextListener.class);

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent servletcontextevent) {
        ServletContext servletContext = servletcontextevent.getServletContext();
        String communicationFile = servletContext.getInitParameter("communication.xml");
        BusClient busClient = null;
        try {
            busClient = connectIBus(communicationFile);
            BeanFactory beanFactory = (BeanFactory) servletContext.getAttribute("beanFactory");
            beanFactory.addBean("busClient", busClient);
        } catch (Exception e1) {
            LOG.error("can not connect to ibus", e1);
        }

    }

    private BusClient connectIBus(String communicationFile) throws Exception {
        BusClient busClient = BusClientFactory.createBusClient(new File(communicationFile));
        return busClient;
    }

}
