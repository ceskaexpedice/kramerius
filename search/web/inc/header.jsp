<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<!-- search form object -->
<view:object name="searchFormViewObject" clz="cz.incad.Kramerius.views.SearchFormViewObject"></view:object>

<div class="clear" id="header">
<div style="float:left;width: 50px;margin-top: 13px; margin-left:10px"><%@ include file="logo.jsp" %></div>

<%--    <div style="float:right;position:absolute;top:0px;right:2px;"><%@ include file="themes.jsp" %></div>  --%>
<div id="main_menu" style="">
    <%@ include file="menu.jsp" %>
</div>

<div id="searchFormDiv" style="float:left;margin-top:10px">
    <%@ include file="searchForm.jsp" %>
</div>
</div>
<c:if test="${cols.current != null}">
<div id="current_vc">
     <span class="mainNav" href="javascript:leaveVirtualCollection();">
         ${cols.currentText}
     <c:if test="${cols.canLeaveCurrent=='true'}">
     <a id="leave_vc" title="<view:msg>search.leave.virtual.collection</view:msg>" href="javascript:leaveVirtualCollection();" style="float:right;margin-top:0px;margin-left:5px;" class="ui-icon ui-icon-arrowreturnthick-1-w">leave virtual collection</a>    
     </c:if>
     
</div>
</c:if>
<script type="text/javascript">
    $('#main_menu_in a').button();
    $('#searchFormDiv a').button();
    $('#leave_vc').button();
    function leaveVirtualCollection(){
    
        var page = new PageQuery(window.location.search);

        page.setValue("offset", "0");
        page.setValue("collection", "none");
        var url = "?" + page.toString();
        window.location = url;
    }
</script>