<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<!-- search form object -->
<view:object name="searchFormViewObject" clz="cz.incad.Kramerius.views.SearchFormViewObject"></view:object>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>

<div class="clear" id="header"><div style="float:left;"><%@ include file="logo.jsp" %></div>
<%--    <div style="float:right;position:absolute;top:0px;right:2px;"><%@ include file="themes.jsp" %></div>  --%>
<div id="main_menu" style="">
    <%@ include file="menu.jsp" %>
</div>
<div id="searchFormDiv" style="vertical-align:middle;margin:auto;padding:7px;width:600px;">
    <%@ include file="searchForm.jsp" %>
</div>
</div>
<c:if test="${cols.current != null}">
<div id="current_vc">
     <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeVirtualCollection();">
         ${cols.current.descriptions[0].text}
     </a>
</div>
</c:if>
<script type="text/javascript">
    $('#main_menu_in a').button();
    $('#searchFormDiv a').button();
    function removeVirtualCollection(){
    
        var page = new PageQuery(window.location.search);

        page.setValue("offset", "0");
        page.setValue("collection", "none");
        var url = "?" + page.toString();
        window.location = url;
    }
</script>