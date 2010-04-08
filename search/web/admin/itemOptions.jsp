<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<div id="menu-<c:out value="${models[status.count -1]}" />" class="item_options" >
    <img class="menu_activation" src="img/menu.png" />
    <div>
        <a title="export to pdf" href="javascript:generatePdf('<c:out value="${status.count}" />');"><img src="img/pdf.png" border="0" /></a>
    <%  if(remoteUserID!=null){  %>
        <a title="static export" href="javascript:generateStatic('<c:out value="${status.count}" />');"><img src="img/pdf.png" border="0" /></a>
    <% } %>
    </div>
</div>