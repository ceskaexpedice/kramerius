<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager,cz.incad.kramerius.processes.DefinitionManager" %>
<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%@page import="cz.incad.Kramerius.views.item.menu.ContextMenuItem" %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration" %>
<%@page import="cz.incad.kramerius.security.SecuredActions" %>
<%
    ArrayList<ContextMenuItem> menus = new ArrayList<ContextMenuItem>();
    menus.add(new ContextMenuItem("administrator.menu.showmetadata", "", "viewMetadata", ""));
    menus.add(new ContextMenuItem("administrator.menu.persistenturl", "", "persistentURL", ""));
    menus.add(new ContextMenuItem("administrator.menu.generatepdf", "_data_x_role", "printMorePages", ""));
    menus.add(new ContextMenuItem("administrator.menu.downloadOriginal", "_data_x_role", "downloadOriginal", ""));
    
    if (request.getRemoteUser() != null) {
        menus.add(new ContextMenuItem("administrator.menu.reindex", "_data_x_role", "reindex", ""));
        menus.add(new ContextMenuItem("administrator.menu.deletefromindex", "_data_x_role", "deletefromindex", ""));
        menus.add(new ContextMenuItem("administrator.menu.deleteuuid", "_data_x_role", "deleteUuid", ""));
        menus.add(new ContextMenuItem("administrator.menu.setpublic", "_data_x_role", "changeFlag", ""));
        menus.add(new ContextMenuItem("administrator.menu.exportFOXML", "_data_x_role", "exportFOXML", ""));
        menus.add(new ContextMenuItem("administrator.menu.exportcd", "_data_x_role", "generateStatic",
            "'static_export_CD','img','"+i18nServlet+"','"+lctx.getLocale().getISO3Country()+"','"+lctx.getLocale().getISO3Language()+"'"));

        menus.add(new ContextMenuItem("administrator.menu.exportdvd", "_data_x_role", "generateStatic",
            "'static_export_CD','img','"+i18nServlet+"','"+lctx.getLocale().getISO3Country()+"','"+lctx.getLocale().getISO3Language()+"'"));
        menus.add(new ContextMenuItem("administrator.menu.generateDeepZoomTiles", "_data_x_role", "generateDeepZoomTiles", ""));
        menus.add(new ContextMenuItem("administrator.menu.deleteGeneratedDeepZoomTiles", "_data_x_role", "deleteGeneratedDeepZoomTiles", ""));
    
        menus.add(new ContextMenuItem("administrator.menu.showrights", "_data_x_role", "securedActionsTableForCtxMenu", 
            "'"+SecuredActions.READ.getFormalName()+"', '" + SecuredActions.ADMINISTRATE.getFormalName()+"'"));
         menus.add(new ContextMenuItem("administrator.menu.editor", "_data_x_role", "openEditor",
                 "'" + kconfig.getEditorURL() + "'"));
         }
%>
<div id="context_items">
</div>
<div style="height:0px;border-top:1px solid silver;"></div>
<ul>
<%
    for(ContextMenuItem menu : menus){
%>
<li>
    <a title="<%=menu.key%>" href="javascript:<%=menu.jsFunction%>(<%=menu.jsArgs%>);"><fmt:message bundle="${bundle}"><%=menu.key%></fmt:message></a>
</li>
<%
    }
%>
</ul>