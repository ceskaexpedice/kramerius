<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>

<%@ page isELIgnored="false"%>
<div  id="main_menu_in">
    <%
    String[] langs = kconfig.getPropertyList("interface.languages");
    String base  =  request.getRequestURL().toString();
    String curLanguage = request.getLocale().getCountry();
    String queryString = request.getQueryString();
    String link = "";
        
    if (queryString == null) {
        queryString =  "";
    }
    
    for(int i=0; i<langs.length; i=i+2){
        
         link = base + "?language="+ langs[i+1] + "&" + queryString;
    %>
    <a href="<%=link%>"><%=langs[i]%></a>
    <%
    }
    %>

        <!--  show admin menu - only for logged users -->
        <scrd:loggedusers>
            <a id="adminHref" href="javascript:showAdminMenu();"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></a>
        </scrd:loggedusers>
        
        <!-- login - only for notlogged -->
        <scrd:notloggedusers>
            <a href="redirect.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.login</fmt:message></a>
        </scrd:notloggedusers>
        
        <!-- logout - only for logged -->
        <scrd:loggedusers>
            <a href="logout.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
        </scrd:loggedusers>

<a href="javascript:showHelp('<c:out value="${param.language}" />');"><fmt:message bundle="${lctx}">application.help</fmt:message>
</a>
<c:if test="${rows != 0}" ><a href="."><fmt:message bundle="${lctx}">application.home</fmt:message></a></c:if>
</div>

<scrd:loggedusers>
    <%@include file="adminMenu.jsp" %>
</scrd:loggedusers>