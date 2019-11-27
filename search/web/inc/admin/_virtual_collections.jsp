<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<view:object name="buttons" clz="cz.incad.Kramerius.views.inc.MenuButtonsViewObject"></view:object>

<table id="coll_table">
    <thead>
    <th>label</th>
    </thead>
<c:forEach var="col" items="${cols.virtualCollectionsFromFedoraLocale}">
    <tr id="vc_${col.pid}">
        <c:forEach items="${col.descriptions}" var="desc">
            <td>
                <span class="val">${desc.text}</span>
            </td>
        </c:forEach>
    </tr>
</c:forEach>
</table>

