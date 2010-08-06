<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<%
	if(pageContext.getAttribute("lctx")==null){
		pageContext.setAttribute("lctx", ((Injector)application.getAttribute(Injector.class.getName())).getProvider(LocalizationContext.class).get());
	}

	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	// view objekt pro stranku = veskera logika 
	ItemViewObject itemViewObject = new ItemViewObject();
	inj.injectMembers(itemViewObject);
	pageContext.setAttribute("itemViewObject", itemViewObject);
%>
<%@ include file="inc/initVars.jsp" %>
<%@ include file="inc/details/itemMenu.jsp" %>