package de.ingrid.iplug.util;

import javax.servlet.http.HttpServletRequest;

public class WebUtil {

	public static String getParameter(HttpServletRequest request, String key,
			String defaultValue) {
		String parameter = request.getParameter(key);
		if (parameter != null) {
			return parameter;
		}
		return defaultValue;
	}

}
