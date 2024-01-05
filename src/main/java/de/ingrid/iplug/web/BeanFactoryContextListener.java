/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
