<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page import="cz.incad.Kramerius.FullImageServlet"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%><div class="menuOptions" id="openmenu<c:out value="${status.count}" />-<c:out value="${itemViewObject.models[status.count -1]}"/>" style="float:right;"  >
    <span class="menu_activation"><img title="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>" alt="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>" src="img/menu.png" onclick="toggleAdminOptions(<c:out value="${status.count}" />, '<c:out value="${itemViewObject.models[status.count -1]}" />');" /></span>
</div>
<div style="display:none;float:right;position:absolute;z-index:7;" class="ui-tabs ui-widget ui-corner-all facet shadow10" id="menu<c:out value="${status.count}" />-<c:out value="${itemViewObject.models[status.count -1]}" />" >
    <div class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active adminMenuHeader">
        <table width="100%">
            <tr>
                <td align="center"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></td>
                <td width="25px" align="center">
                    <a href="javascript:toggleAdminOptions(<c:out value="${status.count}" />,'<c:out value="${itemViewObject.models[status.count -1]}" />');" class="ui-dialog-titlebar-close ui-corner-all" role="button" unselectable="on" style="-moz-user-select: none;"><span class="ui-icon ui-icon-closethick" unselectable="on" style="-moz-user-select: none;">close</span></a>
                </td>
            </tr>
        </table>
    </div>
    <div class="adminMenuItems">
		<c:forEach var="menuItem" items="${menu.contextMenuItems}">
			${menuItem}
		</c:forEach>
	</div>
</div>