<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.lang.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>system information</title>
</head>
<body>

Your system properties are:
<%
Enumeration enumeration = System.getProperties().propertyNames();
while(enumeration.hasMoreElements()){
String key =  (String) enumeration.nextElement();
String value = System.getProperty(key);
%>
<%= key +": "+value %><br/>
<%
}
%>
</body>
</html>