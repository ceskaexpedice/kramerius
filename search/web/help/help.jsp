<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page isELIgnored="false" %>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.Kramerius.views.help.HelpViewObject"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
	HelpViewObject helpViewObject = new HelpViewObject();
	Injector hinj = (Injector)application.getAttribute(Injector.class.getName());
	((Injector)application.getAttribute(Injector.class.getName())).injectMembers(helpViewObject);
	pageContext.setAttribute("helpViewObject", helpViewObject);
%>

<%
	//TODO: Neinterpretuje el expression ?? Proc ??
	if (helpViewObject.getTextAccessible()) {
	    out.println(helpViewObject.getText());
	} else {
	    helpViewObject.redirectToDefault();
	}
%>
