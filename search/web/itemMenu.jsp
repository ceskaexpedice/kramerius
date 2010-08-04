<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<%
	if(pageContext.getAttribute("lctx")==null){
		pageContext.setAttribute("lctx", ((Injector)application.getAttribute(Injector.class.getName())).getProvider(LocalizationContext.class).get());
	}
%>
<%@ include file="inc/initVars.jsp" %>
<%@ include file="inc/details/itemMenu.jsp" %>