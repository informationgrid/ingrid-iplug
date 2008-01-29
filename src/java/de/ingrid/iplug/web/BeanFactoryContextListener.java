package de.ingrid.iplug.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import de.ingrid.utils.BeanFactory;

public class BeanFactoryContextListener implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent servletcontextevent) {

    }

    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute("beanFactory", new BeanFactory());
    }

}
