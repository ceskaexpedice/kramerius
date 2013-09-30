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

<!-- virtual collections -->
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>

<c:if test="${empty param.refresh}">
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

<script type="text/javascript">
    $(document).ready(function(){
        $("#col_refresh").button();
    });
    
    function setVirtualCollection(collection){
        var page = new PageQuery(window.location.search);
        page.setValue("collection", collection);
        var url = "?" + page.toString();
        window.location = url;
    }
    
    function refreshCollections(){
        $("#collection_list").html('<div style="text-align:center; width:100%;display:block;margin-top:30px;"><img src="img/loading.gif" /></div>') ;
        $.get('inc/home/collections.jsp?refresh=true', function(data){
           $("#collection_list").html(data) ;
        }).error(function(data){
           $("#collection_list").html("Error") ;
        });
    }
    
</script>
<a id="col_refresh" style="float:right;" href="javascript:refreshCollections();" title="<view:msg>common.refresh</view:msg>"><span class="ui-icon ui-icon-refresh">refresh</span></a>
<div class="collections" id="collection_list" style="font-size:1.2em;">
</c:if>
    <ul>
<c:forEach var="col" items="${cols.virtualCollectionsLocale}">
    <li>
        <c:forEach items="${col.descriptions}" var="desc">
            <a href="javascript:setVirtualCollection('${col.pid}');">${desc.text}</a>
        </c:forEach>
    </li>
</c:forEach>
    </ul>
    <c:if test="${empty param.refresh}">
</div>
    </c:if>