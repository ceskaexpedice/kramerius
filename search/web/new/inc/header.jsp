<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<div class="clear" id="header"><div style="float:left;"><%@ include file="logo.jsp" %></div>
<div id="main_menu" style="">
    <%@ include file="menu.jsp" %>
</div>
<div id="searchFormDiv" style="vertical-align:middle;margin:auto;padding:15px;width:600px;">
    <%@ include file="searchForm.jsp" %>
</div>
</div>
<script type="text/javascript">
    $('#main_menu a').button();
    $('#searchFormDiv a').button();
</script>