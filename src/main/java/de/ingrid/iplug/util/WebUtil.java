/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for web applications.
 */
public class WebUtil {

    /**
     * Returns a parameter from a HttpServletRequest by its key. It is possible
     * to set a default value in the case the parameter doesn't exist.
     * 
     * @param request
     *            The servlet request.
     * @param key
     *            The parameter name.
     * @param defaultValue
     *            The default value to set.
     * @return The value of a parameter identified by its name.
     */
    public static String getParameter(HttpServletRequest request, String key, String defaultValue) {
        String parameter = request.getParameter(key);
        if (parameter != null) {
            return parameter;
        }
        return defaultValue;
    }

    public static String[] getParameters(HttpServletRequest request, String parameterName, String[] defaultValues) {
        String[] parameter = request.getParameterValues(parameterName);
        String[] returnValue = defaultValues;
        if (parameter != null && parameter.length > 0) {
            returnValue = parameter;
        }
        return returnValue;
    }

}
