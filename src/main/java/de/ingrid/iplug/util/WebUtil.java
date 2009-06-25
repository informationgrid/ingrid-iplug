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
