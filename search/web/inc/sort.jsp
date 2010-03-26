<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<div style="float:right;">
<c:if test="${fn:containsIgnoreCase(param.sort ,'root_title')}" >
   <b>
       <c:if test="${fn:containsIgnoreCase(param.sort ,'desc')}" >
       <a href="javascript:sortBy('root_title+asc')"><fmt:message>názvu</fmt:message></a>
   </c:if>
   <c:if test="${fn:containsIgnoreCase(param.sort ,'asc')}" >
       <a href="javascript:sortBy('root_title+desc')"><fmt:message>názvu</fmt:message></a>
   </c:if>
   </b> 
</c:if> 
<c:if test="${! fn:containsIgnoreCase(param.sort ,'root_title')}" >
   <a href="javascript:sortBy('root_title+asc')"><fmt:message>názvu</fmt:message></a> 
</c:if>

<c:if test="${fn:containsIgnoreCase(param.sort ,'score') || empty param.sort}" >
   <b><fmt:message>relevance</fmt:message></b> 
</c:if> 
<c:if test="${! fn:containsIgnoreCase(param.sort ,'score') && !empty param.sort}" >
    <a href="javascript:sortBy('score+desc')"><fmt:message>relevance</fmt:message></a>  
</c:if>
</div>