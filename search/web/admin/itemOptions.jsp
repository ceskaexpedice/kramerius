<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<div id="openmenu-<c:out value="${models[status.count -1]}"/>" style="float:right;"  >
    <span class="menu_activation"><img title="<fmt:message>administrator.menu</fmt:message>" src="img/menu.png" onclick="toggleAdminOptions('<c:out value="${models[status.count -1]}" />');" /></span>
</div>
<div style="display:none;float:right;position:absolute;z-index:7;" id="menu-<c:out value="${models[status.count -1]}" />" >
    <div class="adminMenuHeader">
        <table width="100%">
            <tr>
                <td align="center"><fmt:message>administrator.menu</fmt:message></td>
                <td width="20px">
                    <a href="javascript:toggleAdminOptions('<c:out value="${models[status.count -1]}" />');" class="ui-dialog-titlebar-close ui-corner-all" role="button" unselectable="on" style="-moz-user-select: none;"><span class="ui-icon ui-icon-closethick" unselectable="on" style="-moz-user-select: none;">close</span></a>
                </td>
            </tr>
        </table>
    </div>
    <div class="adminMenuItems">
        <div align="left"><a title="Generování PDF" href="javascript:generatePdf('<c:out value="${status.count}" />');">Generování PDF...</a> </div>	
        <%if(request.getRemoteUser()!=null){%>
       	<div align="left"><a title="Export do PDF (CD)" href="javascript:generateStatic('<c:out value="${status.count}"/>','static_export_CD');">Export do PDF (CD)...</a></div>
       	<div align="left"><a title="Reindexace" href="javascript:reindex('<c:out value="${status.count}"/>');">Statický export</a></div>
    	<%}%>
    </div>
</div>