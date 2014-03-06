<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    String logo_url = kconfig.getProperty("search.logo_url", ".");
    pageContext.setAttribute("logo_url", logo_url);
%>
<a href="${logo_url}" >
    <img alt="<fmt:message bundle="${lctx}">application.title</fmt:message>" src="css/nkp/logo.png" border="0" />
    <div style="display:inline-block;margin-top: 10px;position: absolute;margin-left: 8px;"><view:msg>nkp.logo</view:msg></div>
</a>