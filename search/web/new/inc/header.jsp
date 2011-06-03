<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="cz.incad.Kramerius.views.SearchFormViewObject"%>
<%@page import="cz.incad.Kramerius.views.adminmenu.AdminMenuViewObject"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%
            pageContext.setAttribute("remoteUser", request.getRemoteUser());
            Injector searchFormInjector = (Injector) application.getAttribute(Injector.class.getName());
            Provider<Locale> localesProvider = searchFormInjector.getProvider(Locale.class);
            pageContext.setAttribute("lang", localesProvider.get().getLanguage());

            AdminMenuViewObject adminMenuViewObject = new AdminMenuViewObject();
            searchFormInjector.injectMembers(adminMenuViewObject);
            pageContext.setAttribute("adminMenuViewObject", adminMenuViewObject);

            SearchFormViewObject searchFormViewObject = new SearchFormViewObject();
            searchFormInjector.injectMembers(searchFormViewObject);
            pageContext.setAttribute("searchFormViewObject", searchFormViewObject);
%>
<div class="clear" id="header"><div style="float:left;"><%@ include file="logo.jsp" %></div>
<div id="main_menu" style="">
    <%@ include file="menu.jsp" %>
</div>
<div id="searchFormDiv" style="vertical-align:middle;margin:auto;padding:15px;width:600px;">
    <%@ include file="searchForm.jsp" %>
</div>
</div>
<script type="text/javascript">
    $('#main_menu_in a').button();
    $('#searchFormDiv a').button();
</script>