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
<%@ page isELIgnored="false"%><view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<style type="text/css">
    .collections li{
        line-height: 22px;
        list-style-type: none;
        margin: 0;
        padding: 0 0 0 10px;
    }
    .collections > ul > li > a {
        font-size: 1.1em;
        font-weight: bold;
    }
</style>
<div class="collections" style="font-size:1.2em;">
        <ul>
<c:forEach var="col" items="${cols.virtualCollectionsLocale}">
    <li>
        <c:forEach items="${col.descriptions}" var="desc">
            <a href="javascript:setVirtualCollection('${col.pid}');">${desc.text}</a>
        </c:forEach>
    </li>
</c:forEach>
        </ul>
</div>

<script type="text/javascript">
    
    function setVirtualCollection(collection){
        var page = new PageQuery(window.location.search);
        page.setValue("collection", collection);
        var url = "?" + page.toString();
        window.location = url;
    }
    
</script>