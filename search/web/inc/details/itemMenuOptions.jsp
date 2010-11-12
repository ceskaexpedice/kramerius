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
<%@page
    import="cz.incad.kramerius.processes.LRProcessManager,cz.incad.kramerius.processes.DefinitionManager"%>
<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%@page import="cz.incad.Kramerius.views.item.menu.ItemMenuViewObject"%>
<%
            Injector inj = (Injector) application.getAttribute(Injector.class.getName());
            // view objekt pro stranku = veskera logika 
            ItemViewObject itemViewObject = new ItemViewObject();
            inj.injectMembers(itemViewObject);
            itemViewObject.init();

            // lokalizacni kontext
            LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);

            // ukladani nejoblibenejsich 
            itemViewObject.saveMostDesirable();
            pageContext.setAttribute("itemViewObject", itemViewObject);
            out.clear();
%>
<c:forEach var="menu" varStatus="status" items="${itemViewObject.menus}">
<c:if test="${menu.uuid == param.pid}">
<%--<c:if test="${status.count == fn:length(itemViewObject.menus)}">--%>
   <%@ include file="../../admin/itemOptions.jsp"%>
</c:if>
</c:forEach>
