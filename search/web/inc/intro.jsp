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
    <c:forEach varStatus="status" var="tab" items="${tabs}">
        <li><a href="#intro${status.count}"><fmt:message bundle="${lctx}">home.tab.${tab}</fmt:message></a></li>
    </c:forEach>
    </ul>

    <c:forEach varStatus="status" var="tab" items="${tabs}">
        <div id="intro${status.count}" style="height:220px;overflow:hidden;"></div>
        <script type="text/javascript">
                $.get('inc/home/${tab}.jsp', function(data){
                   $('#intro${status.count}').html(data) ;
                });
        </script>
    </c:forEach>
</div>
<%--
<div id="intro" class="shadow10">
     <ul>
         <li><a href="#intro1"><fmt:message bundle="${lctx}">Nejnovější</fmt:message></a></li>
         <li><a href="#intro2"><fmt:message bundle="${lctx}">Nejžádanější</fmt:message></a></li>
         <li><a href="#intro3"><fmt:message bundle="${lctx}">Informace</fmt:message></a></li>
     </ul>
     <div id="intro1" style="height:220px;overflow:hidden;"></div>
         <script>
                $.get('inc/newest.jsp?' , function(data){
                   $('#intro1').html(data) ;
                });
         </script>
     <div id="intro2" style="height:220px;overflow:hidden;">
     </div>
         <script>
                $.get('inc/mostDesirables.jsp', function(data){
                   $('#intro2').html(data) ;
                });
         </script>
	<div id="intro3">
	    ${wellcomeViewObject.intro} 
	</div>
</div>
--%>
<script language="javascript">
        $(document).ready(function(){
            $('#intro').tabs();
        });
</script>