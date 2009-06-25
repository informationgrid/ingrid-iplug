package de.ingrid.iplug.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.BeanFactory;

public class PlugdescriptionContextListener implements ServletContextListener {

    private static Log LOG = LogFactory.getLog(PlugdescriptionContextListener.class);

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent servletcontextevent) {
        ServletContext servletContext = servletcontextevent.getServletContext();
        String pdFile = servletContext.getInitParameter("plugdescription.xml");
        BeanFactory beanFactory = (BeanFactory) servletContext.getAttribute("beanFactory");
        try {
            beanFactory.addBean("pd_file", new File(pdFile));
        } catch (IOException e) {
            LOG.error("can not add plugdescription", e);
        }

    }

}
