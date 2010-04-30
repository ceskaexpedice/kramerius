<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<div id="menu-<c:out value="${models[status.count -1]}" />" class="item_options" >
    <span class="menu_activation"><img src="img/menu.png" /></span><span>
        <a title="Generování PDF" href="javascript:generatePdf('<c:out value="${status.count}" />');"><img src="img/pdf.png" border="0" alt="Generování PDF" /></a>
		<%if(request.getRemoteUser()!=null){%>
       	<a title="Statický export" href="javascript:generateStatic('<c:out value="${status.count}"/>','static_export_CD');"><img src="img/pdf-cd.png" border="0"  alt="Statický export"/></a>
    	<%}%>
    </span>
</div>