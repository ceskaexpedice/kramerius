<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.Kramerius.views.WellcomeViewObject"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%
    Injector wellcomeInjector = (Injector)application.getAttribute(Injector.class.getName());
    WellcomeViewObject wellcomeViewObject = new WellcomeViewObject();
    wellcomeInjector.injectMembers(wellcomeViewObject);
    pageContext.setAttribute("wellcomeViewObject", wellcomeViewObject);
    String[] tabs = kconfig.getPropertyList("search.home.tabs");
    pageContext.setAttribute("tabs", tabs);
%>
<div id="intro" class="shadow10">
    <ul>
        <li><a href="#browse"><fmt:message bundle="${lctx}" key="Procházet" /></a></li>
    <c:forEach varStatus="status" var="tab" items="${tabs}">
        <li><a href="#intro${status.count}"><fmt:message bundle="${lctx}">home.tab.${tab}</fmt:message></a></li>
    </c:forEach>
    </ul>
    <div id="browse"><%@ include file="browse.jsp" %></div>

    <c:forEach varStatus="status" var="tab" items="${tabs}">
        <div id="intro${status.count}" style="height:220px;overflow:hidden;"></div>
        <script type="text/javascript">
                $.get('inc/home/${tab}.jsp', function(data){
                   $('#intro${status.count}').html(data) ;
                });
        </script>
    </c:forEach>
</div>
<script type="text/javascript" language="javascript">
        $(document).ready(function(){
            $('#intro').tabs();
        });
</script>