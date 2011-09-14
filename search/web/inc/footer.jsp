<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div align="center" id="languages" >::
    <%
    String[] langs = kconfig.getPropertyList("interface.languages");
    String base  =  request.getRequestURL().toString();
    String queryString = request.getQueryString();
    String link = "";
        
    if (queryString == null) {
        queryString =  "";
    }
    
    for(int i=0; i<langs.length; i=i+2){
         link = base + "?language="+ langs[i+1] + "&" + queryString;
    %>
    <a href="<%=link%>"><%=langs[i]%></a> ::
    <%
    }
    %>
</div>
<div align="center" >
    ©2008-2011. 
    Developed under GNU GPL by 
    <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a>, <a href="http://www.lib.cas.cz/">KNAV</a> and <a href="http://www.mzk.cz/">MZK</a> <br/><br/>
</div>
<c:if test="${param.debug==true}">${url}</c:if>
<div id="test"></div>