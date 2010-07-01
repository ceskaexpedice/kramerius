<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
	String fullImageServlet = FullImageServlet.fullImageServlet(request);
	String i18nServlet = I18NServlet.i18nServlet(request);
	pageContext.setAttribute("imgUrl",fullImageServlet);
	pageContext.setAttribute("i18nUrl",i18nServlet);
	
%>


<%@page import="cz.incad.Kramerius.FullImageServlet"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%><div class="menuOptions" id="openmenu-<c:out value="${models[status.count -1]}"/>" style="float:right;"  >
    <span class="menu_activation"><img title="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>" src="img/menu.png" onclick="toggleAdminOptions('<c:out value="${models[status.count -1]}" />');" /></span>
</div>
<div style="display:none;float:right;position:absolute;z-index:7;" class="menuOptions" id="menu-<c:out value="${models[status.count -1]}" />" >
    <div class="adminMenuHeader">
        <table width="100%">
            <tr>
                <td align="center"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></td>
                <td width="20px">
                    <a href="javascript:toggleAdminOptions('<c:out value="${models[status.count -1]}" />');" class="ui-dialog-titlebar-close ui-corner-all" role="button" unselectable="on" style="-moz-user-select: none;"><span class="ui-icon ui-icon-closethick" unselectable="on" style="-moz-user-select: none;">close</span></a>
                </td>
            </tr>
        </table>
    </div>
    <div class="adminMenuItems">
        <div align="left"><a title="View metadata" href="javascript:showMainContent('<c:out value="${status.count}" />', '<c:out value="${models[status.count -1]}" />');"><fmt:message bundle="${lctx}">administrator.menu.showmetadata</fmt:message></a> </div>	
        <div align="left"><a title="Generování PDF" href="javascript:generatePdf('<c:out value="${status.count}" />');"><fmt:message bundle="${lctx}">administrator.menu.generatepdf</fmt:message></a> </div>	
        <%if(request.getRemoteUser()!=null){%>
       	<div align="left"><a title="Export do PDF (CD)" href="javascript:generateStatic('<c:out value="${status.count}"/>','static_export_CD','<c:out value="${imgUrl}" />','<c:out value="${i18nUrl}" />','<%=request.getLocale().getISO3Country() %>','<%=request.getLocale().getISO3Language()%>');"><fmt:message bundle="${lctx}">administrator.menu.exportcd</fmt:message></a></div>
       	<div align="left"><a title="Reindexace" href="javascript:reindex('<c:out value="${status.count}"/>');"><fmt:message bundle="${lctx}">administrator.menu.reindex</fmt:message></a></a></div>
        <div align="left"><a title="Reindexace" href="javascript:deletefromindex('<c:out value="${status.count}"/>');"><fmt:message bundle="${lctx}">administrator.menu.deletefromindex</fmt:message></a></a></div>
    	<%}%>
    </div>
</div>